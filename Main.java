import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
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
            System.out.println("Помилка обчислення: у щоденнику немає жодної порції, середню калорійність порахувати неможливо (" + e.getMessage() + ").");
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
        } catch (DomainException e) { System.err.println(e.getMessage()); }
    }
    private static void fillLog(Scanner scanner, MealLog log, int count) {
        int entered = 0;
        while (entered < count) {
            System.out.println("\n--- Порція " + (entered + 1) + " з " + count + " ---");
            try { addMealFromInput(scanner, log); entered++; }
            catch (InvalidMealTypeException e) {
                System.out.println("Помилка: " + e.getMessage() + ". Допустимі значення: " + String.join(", ", e.getAllowedTypes()) + ". Введіть порцію ще раз.");
            } catch (InvalidNutritionValueException e) {
                System.out.println("Помилка: " + e.getMessage() + " (введено: " + e.getInvalidValue() + "). Введіть порцію ще раз.");
            } catch (DomainException e) {
                System.out.println("Помилка даних: " + e.getMessage() + ". Введіть порцію ще раз.");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Щоденник заповнений (максимум " + MealLog.CAPACITY + " записів). Решту порцій не збережено."); break;
            }
        }
    }
    private static void addMealFromInput(Scanner scanner, MealLog log) throws DomainException {
        System.out.print("Введіть назву продукту: "); String name = scanner.nextLine();
        int weight = readInt(scanner, "Введіть вагу спожитої порції (г): ");
        double calories = readDouble(scanner, "Введіть калорійність продукту на 100 г (ккал): ");
        double protein = readDouble(scanner, "Введіть вміст білка на 100 г (г): ");
        System.out.print("Введіть прийом їжі (сніданок/обід/вечеря/перекус): ");
        Product product = log.addMeal(name, weight, calories, protein, scanner.nextLine());
        System.out.println("\n===== Результат обліку =====");
        System.out.printf("Продукт: %s%nПрийом їжі: %s%nВага порції: %d г%nОтримано калорій: %.2f ккал%nОтримано білка: %.2f г%n",
            product.getProductName(), product.getMealType(), product.getWeightGrams(), product.getTotalCalories(), product.getTotalProtein());
        System.out.println("Висновок: " + MealAdviceFactory.create(AdviceType.CALORIE).evaluate(product));
    }
    private static void printReport(MealLog log) {
        System.out.println("\n===== Підсумок щоденника =====");
        for (int i = 0; i < log.getSize(); i++) System.out.printf("%d) %s%n", i + 1, log.getMeal(i).toReportLine());
        System.out.printf("Всього: %.2f ккал%nСередня калорійність порції: %d ккал%n", log.totalCalories(), log.averageCalories());
    }
    private static void demoPolymorphicStrategies(Product product) {
        System.out.println("\n===== ЛР5: Factory + поліморфізм + Adapter =====");
        MealAdviceStrategy[] strategies = Arrays.stream(AdviceType.values()).map(MealAdviceFactory::create).toArray(MealAdviceStrategy[]::new);
        for (MealAdviceStrategy strategy : strategies) System.out.println(strategy.shortLabel() + ": " + strategy.evaluate(product));
    }
    private static void demoStrategyRuntimeSwitch(Product product) {
        System.out.println("\n===== Зміна Strategy під час виконання =====");
        MealAdviser adviser = new MealAdviser(MealAdviceFactory.create(AdviceType.CALORIE));
        System.out.println("До зміни -> " + adviser.giveAdvice(product));
        adviser.setStrategy(MealAdviceFactory.create(AdviceType.PROTEIN));
        System.out.println("Після зміни -> " + adviser.giveAdvice(product));
        adviser.setStrategy(MealAdviceFactory.create(AdviceType.LEGACY));
        System.out.println("Через Adapter -> " + adviser.giveAdvice(product));
    }
    private static void demoSorting(MealLog log) {
        if (log.getSize() == 0) return;
        Product[] sorted = new Product[log.getSize()];
        for (int i = 0; i < sorted.length; i++) sorted[i] = log.getMeal(i);
        Arrays.sort(sorted);
        System.out.println("\n===== Порції за калорійністю =====");
        for (Product p : sorted) System.out.println(p.toReportLine());
    }
    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try { int value = scanner.nextInt(); scanner.nextLine(); return value; }
            catch (InputMismatchException e) { System.out.println("Помилка: очікується ціле число, а введено: " + scanner.nextLine()); }
        }
    }
    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try { double value = scanner.nextDouble(); scanner.nextLine(); return value; }
            catch (InputMismatchException e) { System.out.println("Помилка: очікується число. Перевірте роздільник дробової частини. Введено: " + scanner.nextLine()); }
        }
    }
    private static void demoHierarchy() {
        System.out.println("\n===== catch базового класу DomainException =====");
        for (int i = 0; i < 2; i++) {
            try { new Product("Тест", i == 0 ? -5 : 100, 100, 10, i == 0 ? "обід" : "полуденок"); }
            catch (DomainException e) { System.out.println(e.getClass().getSimpleName() + " -> " + e.getMessage()); }
        }
    }
    private static void demoObserverUnsubscribe() throws DomainException {
        System.out.println("\n===== Observer: два підписники та відписка =====");
        MealLog demo = new MealLog();
        ConsoleMealObserver journal = new ConsoleMealObserver();
        demo.addListener(journal); demo.addListener(new CalorieSummaryObserver());
        demo.addMeal("Яблуко", 100, 52, 0.3, "перекус");
        demo.removeListener(journal);
        System.out.println("Журнал відписано: наступну подію отримує лише підсумок.");
        demo.addMeal("Сир", 100, 120, 18, "сніданок");
    }
}
