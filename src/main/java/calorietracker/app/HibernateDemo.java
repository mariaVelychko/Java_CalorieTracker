package calorietracker.app;

import calorietracker.exception.EntityNotFoundException;
import calorietracker.jpa.HibernateCategoryService;
import calorietracker.jpa.HibernateFruitProductService;
import calorietracker.jpa.JpaUtil;
import calorietracker.model.FoodCategory;
import calorietracker.model.FoodItem;
import calorietracker.model.FruitProduct;
import calorietracker.service.EntityUtils;
import jakarta.persistence.EntityManager;
import org.hibernate.LazyInitializationException;
import org.hibernate.stat.Statistics;

public final class HibernateDemo {
    private HibernateDemo() {
    }

    public static void main(String[] args) {
        runAll();
    }

    public static void runAll() {
        FoodItem.traceConstructors = false;
        try {
            run();
        } catch (RuntimeException | ExceptionInInitializerError e) {
            System.out.println("Демо Hibernate не вдалося виконати: " + rootMessage(e));
        } finally {
            FoodItem.traceConstructors = true;
        }
    }

    private static void run() {
        HibernateFruitProductService fruits = new HibernateFruitProductService();
        HibernateCategoryService categories = new HibernateCategoryService();
        clean();

        System.out.println("\n===== ЛР11: CRUD через Hibernate =====");
        fruits.save(new FruitProduct(1, "Яблуко", 150, 52, 0.3, "перекус", 10));
        System.out.println("створено:  " + describe(fruits, 1));
        fruits.save(new FruitProduct(1, "Яблуко", 200, 55, 0.3, "перекус", 14));
        System.out.println("оновлено:  " + describe(fruits, 1));
        System.out.println("delete(1) -> " + fruits.delete(1));
        try {
            fruits.findById(1);
        } catch (EntityNotFoundException e) {
            System.out.println("findById(1) після видалення -> " + e.getMessage());
        }

        System.out.println("\n===== ЛР11: той самий клієнтський код, третя реалізація (Hibernate) =====");
        JdbcDemo.demoSameClientCode(fruits);

        System.out.println("\n===== ЛР11: зв'язок «категорія — фрукти» =====");
        FoodCategory citrus = new FoodCategory(1, "Цитрусові", "Апельсини, лимони");
        citrus.addFruit(new FruitProduct(10, "Апельсин", 200, 47, 0.9, "перекус", 9));
        citrus.addFruit(new FruitProduct(11, "Лимон", 100, 29, 1.1, "сніданок", 2.5));
        categories.save(citrus);
        FoodCategory berries = new FoodCategory(2, "Ягоди");
        berries.addFruit(new FruitProduct(12, "Малина", 100, 52, 1.2, "перекус", 4.4));
        categories.save(berries);
        System.out.println("Категорію збережено разом із фруктами (каскад PERSIST/MERGE)");

        try {
            FoodCategory loaded = categories.findByIdWithFruits(1);
            System.out.println(loaded + ", фруктів: " + loaded.getFruits().size());
            FruitProduct lemon = null;
            for (FruitProduct f : loaded.getFruits()) {
                if (f.getId() == 11) {
                    lemon = f;
                }
            }
            loaded.removeFruit(lemon);
            fruits.save(lemon);
            System.out.println("Лимон після removeFruit: category_id = " + fruits.findById(11).getCategoryId());
        } catch (EntityNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            categories.delete(2);
        } catch (RuntimeException e) {
            System.out.println("Видалення категорії з фруктами -> " + rootMessage(e));
        }

        System.out.println("\n===== ЛР11: запити JPQL =====");
        fruits.save(new FruitProduct(20, "Яблуко", 150, 52, 0.3, "перекус", 14));
        fruits.save(new FruitProduct(21, "Яблуко", 180, 48, 0.4, "вечеря", 9));
        System.out.println("findAllSortedByNameAndBySugar():");
        EntityUtils.printAll(fruits.findAllSortedByNameAndBySugar());
        System.out.println("findByCategory(2):");
        EntityUtils.printAll(fruits.findByCategory(2));
        for (int i = 1; i <= 7; i++) {
            fruits.save(new FruitProduct(100 + i, "Фрукт-" + i, 100, 50, 1, "перекус", i));
        }
        for (int page = 0; page < 3; page++) {
            StringBuilder sb = new StringBuilder("Сторінка " + page + " (по 4): ");
            for (FruitProduct f : fruits.findPage(page, 4)) {
                sb.append(f.getName()).append("; ");
            }
            System.out.println(sb);
        }

        System.out.println("\n===== ЛР11: LazyInitializationException =====");
        try {
            FoodCategory detached = categories.findById(2);
            System.out.println(detached.getFruits().size());
        } catch (LazyInitializationException e) {
            System.out.println("Помилка: " + rootMessage(e));
        } catch (EntityNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            System.out.println("Виправлення (JOIN FETCH): фруктів у категорії «Ягоди» = "
                    + categories.findByIdWithFruits(2).getFruits().size());
        } catch (EntityNotFoundException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n===== ЛР11: проблема N+1 =====");
        int categoryCount = categories.findAllSortedByName().size();
        Statistics stats = JpaUtil.getInstance().getStatistics();
        stats.clear();
        categories.countFruitsNPlusOne();
        long before = stats.getPrepareStatementCount();
        stats.clear();
        categories.countFruitsJoinFetch();
        long after = stats.getPrepareStatementCount();
        System.out.println("Категорій: " + categoryCount);
        System.out.println("SQL-запитів ДО виправлення (цикл по category.getFruits()): " + before);
        System.out.println("SQL-запитів ПІСЛЯ виправлення (JOIN FETCH): " + after);

        clean();
    }

    private static String describe(HibernateFruitProductService fruits, long id) {
        try {
            FruitProduct f = fruits.findById(id);
            return f + ", вага " + f.getWeightGrams() + " г";
        } catch (EntityNotFoundException e) {
            return e.getMessage();
        }
    }

    private static void clean() {
        JpaUtil jpa = JpaUtil.getInstance();
        EntityManager em = jpa.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("delete from FruitProduct").executeUpdate();
            em.createQuery("delete from FoodCategory").executeUpdate();
            jpa.commit(em);
        } catch (RuntimeException e) {
            jpa.rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        String message = String.valueOf(t.getMessage());
        int newline = message.indexOf('\n');
        message = newline > 0 ? message.substring(0, newline) : message;
        return message.length() > 200 ? message.substring(0, 200) + "…" : message;
    }
}
