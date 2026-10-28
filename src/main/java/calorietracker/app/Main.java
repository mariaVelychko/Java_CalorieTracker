package calorietracker.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import calorietracker.advice.AdviceType;
import calorietracker.advice.MealAdviceFactory;
import calorietracker.advice.MealAdviceStrategy;
import calorietracker.advice.MealAdviser;
import calorietracker.exception.DomainException;
import calorietracker.exception.EntityNotFoundException;
import calorietracker.exception.InvalidMealTypeException;
import calorietracker.exception.InvalidNutritionValueException;
import calorietracker.model.FoodItem;
import calorietracker.model.FoodItemFactory;
import calorietracker.model.FoodItemType;
import calorietracker.model.FruitProduct;
import calorietracker.model.MealLog;
import calorietracker.model.Product;
import calorietracker.model.ProteinProduct;
import calorietracker.model.SupplementProduct;
import calorietracker.observer.CalorieSummaryObserver;
import calorietracker.observer.ConsoleMealObserver;
import calorietracker.service.EntityUtils;
import calorietracker.service.InMemoryCrudService;
import calorietracker.service.InMemoryFruitProductService;

public class Main {

    public static void main(String[] args) {
        MealLog log = new MealLog();
        log.addListener(new ConsoleMealObserver());
        log.addListener(new CalorieSummaryObserver());

        Scanner scanner = new Scanner(System.in);

        try {
            int count = readInt(scanner, "Скільки порцій занести в щоденник? ");
            fillLog(scanner, log, count);
            printReport(log);
        } catch (ArithmeticException e) {
            System.out.println("Помилка обчислення: у щоденнику немає жодної порції, "
                    + "середню калорійність порахувати неможливо (" + e.getMessage() + ").");
        } catch (NoSuchElementException e) {
            System.out.println("Введення перервано: дані закінчилися раніше, ніж очікувалось.");
        } catch (Exception e) {
            System.out.println("Непередбачена помилка: " + e);
        } finally {
            scanner.close();
            System.out.println("Роботу програми завершено, Scanner закрито.");
        }

        demoHierarchy();

        try {
            Product sample = new Product("Демо-продукт", 250, 300, 20, "обід");
            demoPolymorphicStrategies(sample);
            demoStrategyRuntimeSwitch(sample);
            demoSorting(log);
            demoObserverUnsubscribe();
        } catch (DomainException e) {
            System.err.println(e.getMessage());
        }

        demoFoodItemHierarchy();
        demoCrudService();
        JdbcDemo.runAll(); 
    }

    private static void fillLog(Scanner scanner, MealLog log, int count) {
        int entered = 0;

        while (entered < count) {
            System.out.println("\n--- Порція " + (entered + 1) + " з " + count + " ---");

            try {
                addMealFromInput(scanner, log);
                entered++;
            } catch (InvalidMealTypeException e) {
                System.out.println("Помилка: " + e.getMessage()
                        + ". Допустимі значення: "
                        + String.join(", ", e.getAllowedTypes())
                        + ". Введіть порцію ще раз.");
            } catch (InvalidNutritionValueException e) {
                System.out.println("Помилка: " + e.getMessage()
                        + " (введено: " + e.getInvalidValue()
                        + "). Введіть порцію ще раз.");
            } catch (DomainException e) {
                System.out.println("Помилка даних: " + e.getMessage()
                        + ". Введіть порцію ще раз.");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Щоденник заповнений (максимум "
                        + MealLog.CAPACITY + " записів). Решту порцій не збережено.");
                break;
            }
        }
    }

    private static void addMealFromInput(Scanner scanner, MealLog log) throws DomainException {
        System.out.print("Введіть назву продукту: ");
        String productName = scanner.nextLine();

        int weightGrams = readInt(scanner, "Введіть вагу спожитої порції (г): ");
        double caloriesPer100g = readDouble(scanner,
                "Введіть калорійність продукту на 100 г (ккал): ");
        double proteinPer100g = readDouble(scanner,
                "Введіть вміст білка на 100 г (г): ");

        System.out.print("Введіть прийом їжі (сніданок/обід/вечеря/перекус): ");
        String mealType = scanner.nextLine();

        Product product = log.addMeal(productName, weightGrams,
                caloriesPer100g, proteinPer100g, mealType);

        System.out.println();
        System.out.println("===== Результат обліку =====");
        System.out.printf("Продукт: %s%n", product.getProductName());
        System.out.printf("Прийом їжі: %s%n", product.getMealType());
        System.out.printf("Вага порції: %d г%n", product.getWeightGrams());
        System.out.printf("Отримано калорій: %.2f ккал%n", product.getTotalCalories());
        System.out.printf("Отримано білка: %.2f г%n", product.getTotalProtein());
        System.out.println("Висновок: "
                + MealAdviceFactory.create(AdviceType.CALORIE).evaluate(product));
    }

    private static void printReport(MealLog log) {
        System.out.println("\n===== Підсумок щоденника =====");

        for (int i = 0; i < log.getSize(); i++) {
            System.out.printf("%d) %s%n", i + 1, log.getMeal(i).toReportLine());
        }

        System.out.printf("Всього: %.2f ккал%n", log.totalCalories());
        System.out.printf("Середня калорійність порції: %d ккал%n", log.averageCalories());
    }

    private static void demoPolymorphicStrategies(Product product) {
        System.out.println("\n===== ЛР5: Factory + поліморфізм + Adapter =====");

        MealAdviceStrategy[] strategies = {
                MealAdviceFactory.create(AdviceType.CALORIE),
                MealAdviceFactory.create(AdviceType.PROTEIN),
                MealAdviceFactory.create(AdviceType.BALANCED),
                MealAdviceFactory.create(AdviceType.LEGACY)
        };

        for (MealAdviceStrategy strategy : strategies) {
            System.out.println(strategy.shortLabel() + ": " + strategy.evaluate(product));
        }
    }

    private static void demoStrategyRuntimeSwitch(Product product) {
        System.out.println("\n===== Зміна Strategy під час виконання =====");

        MealAdviser adviser = new MealAdviser(
                MealAdviceFactory.create(AdviceType.CALORIE));

        System.out.println("До зміни -> " + adviser.giveAdvice(product));

        adviser.setStrategy(MealAdviceFactory.create(AdviceType.PROTEIN));
        System.out.println("Після зміни -> " + adviser.giveAdvice(product));

        adviser.setStrategy(MealAdviceFactory.create(AdviceType.LEGACY));
        System.out.println("Через Adapter -> " + adviser.giveAdvice(product));
    }

    private static void demoSorting(MealLog log) {
        if (log.getSize() == 0) {
            return;
        }

        Product[] sorted = new Product[log.getSize()];

        for (int i = 0; i < log.getSize(); i++) {
            sorted[i] = log.getMeal(i);
        }

        Arrays.sort(sorted);

        System.out.println("\n===== Порції за калорійністю =====");

        for (Product product : sorted) {
            System.out.println(product.toReportLine());
        }
    }

    private static void demoObserverUnsubscribe() throws DomainException {
        System.out.println("\n===== Observer: два підписники та відписка =====");

        MealLog demo = new MealLog();
        ConsoleMealObserver journal = new ConsoleMealObserver();

        demo.addListener(journal);
        demo.addListener(new CalorieSummaryObserver());

        demo.addMeal("Яблуко", 100, 52, 0.3, "перекус");

        demo.removeListener(journal);

        System.out.println("Журнал відписано: наступну подію отримує лише підсумок.");

        demo.addMeal("Сир", 100, 120, 18, "сніданок");
    }

    private static void demoHierarchy() {
        System.out.println("\n===== catch базового класу DomainException =====");

        for (int i = 0; i < 2; i++) {
            try {
                if (i == 0) {
                    new Product("Тест", -5, 100, 10, "обід");
                } else {
                    new Product("Тест", 100, 100, 10, "полуденок");
                }
            } catch (DomainException e) {
                System.out.println(e.getClass().getSimpleName()
                        + " -> " + e.getMessage());
            }
        }
    }

    private static void demoFoodItemHierarchy() {
        System.out.println("\n===== ЛР6: ієрархія FoodItem =====");

        List<FoodItem> items = new ArrayList<>();
        items.add(new FruitProduct(1, "Яблуко", 150, 52, 0.3, "перекус", 10));
        items.add(new ProteinProduct(2, "Куряче філе", 250, 165, 31, "обід", "тваринний"));
        items.add(new SupplementProduct(3, "Протеїн", 30, 380, 80, "перекус", 30));

        System.out.println("\n===== Поліморфний виклик =====");

        for (FoodItem item : items) {
            System.out.println(item.getFoodDescription());
            System.out.println(item);
        }

        System.out.println("\n===== instanceof з pattern matching =====");

        for (FoodItem item : items) {
            if (item instanceof FruitProduct fruit) {
                System.out.println("Це фрукт, цукор: "
                        + fruit.getSugarGrams() + " г");
            } else if (item instanceof ProteinProduct protein) {
                System.out.println("Це білковий продукт: "
                        + protein.getProteinType());
            } else if (item instanceof SupplementProduct supplement) {
                System.out.println("Це добавка, норма: "
                        + supplement.getDailyNormGrams() + " г");
            }
        }

        System.out.println("\n===== Factory + sealed switch =====");

        FoodItem created = FoodItemFactory.create(4, FoodItemType.FRUIT,
                "Банан", 120, 89, 1.1, "перекус");

        System.out.println(created);
        System.out.println(describeFood(created));

        System.out.println("\n===== equals і hashCode (структурна рівність FoodItem) =====");

        FruitProduct apple1 = new FruitProduct(10, "Яблуко", 150, 52, 0.3, "перекус", 10);
        FruitProduct apple2 = new FruitProduct(11, "Яблуко", 150, 52, 0.3, "перекус", 10);
        ProteinProduct chicken = new ProteinProduct(12, "Куряче філе", 250, 165, 31,
                "обід", "тваринний");

        System.out.println("apple1.getId() = " + apple1.getId() + ", apple2.getId() = " + apple2.getId());
        System.out.println("apple1.equals(apple2): " + apple1.equals(apple2)
                + "  <- true попри різні id, бо FoodItem.equals порівнює ЗНАЧЕННЯ полів, не id");
        System.out.println("apple2.equals(apple1): " + apple2.equals(apple1));
        System.out.println("apple1.equals(chicken): " + apple1.equals(chicken));
        System.out.println("hashCode apple1: " + apple1.hashCode());
        System.out.println("hashCode apple2: " + apple2.hashCode());
    }

    private static void demoCrudService() {
        System.out.println("\n===== ЛР7-9: CRUD-сервіс, generics, сортування =====");

        InMemoryFruitProductService fruitService = new InMemoryFruitProductService();
        InMemoryCrudService<ProteinProduct> proteinService = new InMemoryCrudService<>();

        fruitService.save(new FruitProduct(1, "Яблуко", 150, 52, 0.3, "перекус", 10));
        fruitService.save(new FruitProduct(2, "Банан", 120, 89, 1.1, "перекус", 12));
        fruitService.save(new FruitProduct(3, "Яблуко", 180, 48, 0.4, "вечеря", 9)); // те саме ім'я, інший цукор

        System.out.println("\n--- Оновлення наявної сутності (save з тим самим id) ---");
        System.out.println("Записів до повторного save: " + fruitService.findAll().size());
        fruitService.save(new FruitProduct(1, "Яблуко", 200, 55, 0.3, "перекус", 14));
        System.out.println("Записів після повторного save(id=1): " + fruitService.findAll().size());

        System.out.println("\n--- findAll() до сортування ---");
        EntityUtils.printAll(fruitService.findAll());

        System.out.println("\n--- findAllSortedByName() ---");
        EntityUtils.printAll(fruitService.findAllSortedByName());

        System.out.println("\n--- findAllSortedByNameAndBySugar() (вторинне сортування за цукром) ---");
        EntityUtils.printAll(fruitService.findAllSortedByNameAndBySugar());

        System.out.println("\n--- findAll() ПІСЛЯ сортувань: порядок і склад сховища не змінились ---");
        EntityUtils.printAll(fruitService.findAll());

        System.out.println("\n--- delete: успішний і неуспішний ---");
        System.out.println("delete(2) -> " + fruitService.delete(2)); // є
        System.out.println("delete(999) -> " + fruitService.delete(999)); // немає

        System.out.println("\n--- findById: неіснуючий id кидає EntityNotFoundException ---");
        try {
            fruitService.findById(999);
        } catch (EntityNotFoundException e) {
            System.out.println("Спіймано: " + e.getMessage());
        }

        System.out.println("\n--- Той самий клас InMemoryCrudService<T> для ProteinProduct ---");
        proteinService.save(new ProteinProduct(100, "Куряче філе", 250, 165, 31, "обід", "тваринний"));
        proteinService.save(new ProteinProduct(101, "Тофу", 200, 144, 15, "обід", "рослинний"));
        EntityUtils.printAll(proteinService.findAllSortedByName());
    }

private static String describeFood(FoodItem item) {
    return switch (item) {
        case FruitProduct fruit -> "Це фрукт: " + fruit.getName();
        case ProteinProduct protein -> "Це білковий продукт: " + protein.getName();
        case SupplementProduct supplement -> "Це добавка: " + supplement.getName();
        default -> "Невідомий тип їжі";
    };
}

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e) {
                String bad = scanner.nextLine();
                System.out.println("Помилка: очікується ціле число, а введено \"" + bad + "\". Спробуйте ще раз.");
}
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                double value = scanner.nextDouble();
                scanner.nextLine();
                return value;
          } catch (InputMismatchException e) {
                String bad = scanner.nextLine();
                System.out.println("Помилка: очікується число, а введено \"" + bad + "\". Перевірте роздільник дробової частини.");
}
        }
    }
}
