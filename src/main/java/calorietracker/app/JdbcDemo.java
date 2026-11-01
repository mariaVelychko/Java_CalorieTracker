package calorietracker.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import calorietracker.exception.DataAccessException;
import calorietracker.exception.EntityNotFoundException;
import calorietracker.jdbc.DBConnection;
import calorietracker.jdbc.JdbcCategoryService;
import calorietracker.jdbc.JdbcFruitProductService;
import calorietracker.model.FoodCategory;
import calorietracker.model.FoodItem;
import calorietracker.model.FruitProduct;
import calorietracker.service.EntityUtils;
import calorietracker.service.FoodCatalogService;
import calorietracker.service.FruitProductService;
import calorietracker.service.InMemoryFruitProductService;

public final class JdbcDemo {

    private JdbcDemo() {
    }

    public static void main(String[] args) {
        runAll();
    }

    public static void runAll() {
        FoodItem.traceConstructors = false; 
        try {
            DBConnection db = DBConnection.getInstance();
            demoSingleton(db);
            run(db.getDataSource());
        } catch (RuntimeException e) {
            System.out.println("Демо JDBC не вдалося виконати: " + e.getMessage());
        } finally {
            FoodItem.traceConstructors = true;
        }
    }

    public static void run(DataSource ds) {
        JdbcCategoryService categories = new JdbcCategoryService(ds);
        JdbcFruitProductService fruits = new JdbcFruitProductService(ds);
        FoodCatalogService catalog = new FoodCatalogService(ds, categories, fruits);

        fruits.deleteAll();
        categories.deleteAll();

        System.out.println("\n===== ЛР10: той самий клієнтський код — інша реалізація сервісу =====");
        System.out.println("--- реалізація 1: у пам'яті (ЛР7) ---");
        demoSameClientCode(new InMemoryFruitProductService());
        System.out.println("\n--- реалізація 2: у БД через JDBC (ЛР10) ---");
        demoSameClientCode(fruits);

        demoConstraints(categories, fruits);
        demoTransaction(catalog, categories, fruits);
        demoInjectionSafe(fruits);
        demoPagination(fruits);
        demoBatch(fruits);

        fruits.deleteAll();
        categories.deleteAll();
    }

    public static void demoSameClientCode(FruitProductService service) {
        service.save(new FruitProduct(1, "Яблуко", 150, 52, 0.3, "перекус", 10));
        service.save(new FruitProduct(2, "Банан", 120, 89, 1.1, "перекус", 12));
        service.save(new FruitProduct(3, "Яблуко", 180, 48, 0.4, "вечеря", 9));
        service.save(new FruitProduct(1, "Яблуко", 200, 55, 0.3, "перекус", 14)); // оновлення id=1

        System.out.println("findAllSortedByName():");
        EntityUtils.printAll(service.findAllSortedByName());
        System.out.println("findAllSortedByNameAndBySugar():");
        EntityUtils.printAll(service.findAllSortedByNameAndBySugar());

        System.out.println("delete(2) -> " + service.delete(2) + ", delete(999) -> " + service.delete(999));
        try {
            service.findById(999);
        } catch (EntityNotFoundException e) {
            System.out.println("findById(999) -> EntityNotFoundException: " + e.getMessage());
        }
        service.delete(1);
        service.delete(3);
    }

    private static void demoConstraints(JdbcCategoryService categories, JdbcFruitProductService fruits) {
        System.out.println("\n===== ЛР10: обмеження БД (FOREIGN KEY, CHECK, UNIQUE) =====");
        categories.save(new FoodCategory(1, "Цитрусові", "Апельсини, лимони, мандарини"));
        categories.save(new FoodCategory(2, "Кісточкові")); // description = NULL
        fruits.save(new FruitProduct(10, "Апельсин", 200, 47, 0.9, "перекус", 9, true, 1L));

        try {
            fruits.save(new FruitProduct(11, "Привид", 100, 10, 1, "перекус", 1, true, 999L));
        } catch (DataAccessException e) {
            System.out.println("FK: категорії 999 не існує -> " + shortMessage(e));
        }
        try {
            fruits.save(new FruitProduct(12, "Кислий", 100, 10, 1, "перекус", -5, true, 1L));
        } catch (DataAccessException e) {
            System.out.println("CHECK: від'ємний цукор -> " + shortMessage(e));
        }
        try {
            categories.save(new FoodCategory(3, "Цитрусові"));
        } catch (DataAccessException e) {
            System.out.println("UNIQUE: дубль назви категорії -> " + shortMessage(e));
        }
        try {
            categories.delete(1);
        } catch (DataAccessException e) {
            System.out.println("ON DELETE RESTRICT: категорія має фрукти -> " + shortMessage(e));
        }
        fruits.delete(10);
        System.out.println("Після видалення фруктів категорію можна видалити: delete(1) -> " + categories.delete(1));
        categories.delete(2);
    }

    private static void demoTransaction(FoodCatalogService catalog,
                                        JdbcCategoryService categories,
                                        JdbcFruitProductService fruits) {
        System.out.println("\n===== ЛР10: транзакція (commit і rollback) =====");

        int saved = catalog.createCategoryWithFruits(
                new FoodCategory(20, "Ягоди", "Малина, чорниця"),
                List.of(new FruitProduct(201, "Малина", 100, 52, 1.2, "перекус", 4.4),
                        new FruitProduct(202, "Чорниця", 100, 57, 0.7, "перекус", 10)));
        System.out.println("COMMIT: збережено категорію «Ягоди» і " + saved + " фрукти. Категорій: "
                + categories.count() + ", фруктів: " + fruits.count());

        try {
            catalog.createCategoryWithFruits(
                    new FoodCategory(21, "Тропічні"),
                    List.of(new FruitProduct(203, "Манго", 150, 60, 0.8, "перекус", 14),
                            new FruitProduct(204, "Зіпсований", 150, 60, 0.8, "перекус", -1))); // порушить CHECK
        } catch (DataAccessException e) {
            System.out.println("ROLLBACK: " + shortMessage(e));
        }
        System.out.println("Після відкату: категорій: " + categories.count() + ", фруктів: " + fruits.count());
        try {
            categories.findById(21);
        } catch (EntityNotFoundException e) {
            System.out.println("Категорія «Тропічні» НЕ збереглась (відкат скасував і її): " + e.getMessage());
        }
        try {
            fruits.findById(203);
        } catch (EntityNotFoundException e) {
            System.out.println("Фрукт «Манго» (id=203), що вже був вставлений до помилки, теж скасований: "
                    + e.getMessage());
        }

        try {
            categories.save(new FoodCategory(22, "Червоні ягоди"));
            System.out.println("moveFruits(20 -> 22): перенесено фруктів: " + catalog.moveFruits(20, 22));
            catalog.moveFruits(22, 21);
        } catch (EntityNotFoundException e) {
            System.out.println("moveFruits у неіснуючу категорію -> " + e.getMessage());
        }
        fruits.deleteAll();
        categories.deleteAll();
    }

    private static void demoInjectionSafe(JdbcFruitProductService fruits) {
        System.out.println("\n===== ЛР10: PreparedStatement проти SQL-ін'єкції =====");
        fruits.save(new FruitProduct(1, "Яблуко", 150, 52, 0.3, "перекус", 10));
        fruits.save(new FruitProduct(2, "Банан", 120, 89, 1.1, "перекус", 12));

        String[] inputs = {"Яблуко", "' OR '1'='1", "x'; DROP TABLE fruit_product; --"};
        for (String input : inputs) {
            System.out.println("findByName(\"" + input + "\") -> " + fruits.findByName(input).size()
                    + " рядків; фруктів у таблиці: " + fruits.count());
        }
        fruits.deleteAll();
    }

    private static void demoPagination(JdbcFruitProductService fruits) {
        System.out.println("\n===== ЛР10: посторінкове читання (LIMIT/OFFSET) =====");
        List<FruitProduct> data = generateFruits(1, 7);
        fruits.insertAllBatch(data);
        int size = 3;
        for (int page = 0; page * size < fruits.count(); page++) {
            StringBuilder sb = new StringBuilder("Сторінка " + page + " (розмір " + size + "): ");
            for (FruitProduct f : fruits.findPage(page, size)) {
                sb.append(f.getName()).append("; ");
            }
            System.out.println(sb);
        }
        fruits.deleteAll();
    }

    private static void demoBatch(JdbcFruitProductService fruits) {
        System.out.println("\n===== ЛР10: пакетна вставка проти вставки по одному =====");
        int n = 10_000;
        List<FruitProduct> data = generateFruits(1, n);

        long t1 = time(() -> fruits.insertAllOneByOne(data));
        System.out.printf("1) по одному, autoCommit=true  (%d рядків): %d мс%n", n, t1);
        fruits.deleteAll();

        long t2 = time(() -> fruits.insertAllOneByOneInTransaction(data));
        System.out.printf("2) по одному, одна транзакція  (%d рядків): %d мс%n", n, t2);
        fruits.deleteAll();

        long t3 = time(() -> fruits.insertAllBatch(data));
        System.out.printf("3) addBatch/executeBatch + одна транзакція (%d рядків): %d мс%n", n, t3);
        System.out.println("У таблиці: " + fruits.count() + " рядків");
        fruits.deleteAll();
    }

    private static void demoSingleton(DBConnection db) {
        System.out.println("\n===== ЛР10: Singleton DBConnection у багатопотоковому режимі =====");
        Set<DBConnection> instances = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<Long>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                results.add(pool.submit(() -> {
                    instances.add(DBConnection.getInstance());
                    JdbcFruitProductService s = new JdbcFruitProductService(DBConnection.getInstance().getDataSource());
                    return s.count();
                }));
            }
            for (Future<Long> f : results) {
                f.get(30, TimeUnit.SECONDS);
            }
            System.out.println("Різних екземплярів DBConnection на 8 потоків: " + instances.size()
                    + " (має бути 1); паралельні запити виконано без помилок");
        } catch (Exception e) {
            System.out.println("Збій при перевірці потоків: " + e);
        } finally {
            pool.shutdown();
        }
    }

    public static List<FruitProduct> generateFruits(int fromId, int count) {
        List<FruitProduct> list = new ArrayList<>(count);
        String[] meals = {"сніданок", "обід", "вечеря", "перекус"};
        for (int i = 0; i < count; i++) {
            int id = fromId + i;
            list.add(new FruitProduct(id, "Фрукт-" + String.format("%05d", id), 100 + i % 400,
                    30 + i % 200, (i % 30) / 10.0, meals[i % 4], (i % 150) / 10.0, i % 2 == 0, null));
        }
        return list;
    }

    private static long time(Runnable action) {
        long start = System.nanoTime();
        action.run();
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static String shortMessage(Exception e) {
        String m = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        int nl = m.indexOf('\n');
        m = nl > 0 ? m.substring(0, nl) : m;
        return m.length() > 160 ? m.substring(0, 160) + "…" : m;
    }
}
