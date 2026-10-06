import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MealLog log = new MealLog();

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
    }

    private static void fillLog(Scanner scanner, MealLog log, int count) {
        int entered = 0;
        while (entered < count) {
            System.out.println("\n--- Порція " + (entered + 1) + " з " + count + " ---");
            try {
                addMealFromInput(scanner, log);
                entered++;
            } catch (InvalidMealTypeException e) {             
                System.out.println("Помилка: " + e.getMessage() + ". Допустимі значення: "
                        + String.join(", ", e.getAllowedTypes()) + ". Введіть порцію ще раз.");
            } catch (InvalidNutritionValueException e) {      
                System.out.println("Помилка: " + e.getMessage() + " (введено: "
                        + e.getInvalidValue() + "). Введіть порцію ще раз.");
            } catch (DomainException e) {                     
                System.out.println("Помилка даних: " + e.getMessage() + ". Введіть порцію ще раз.");
            } catch (ArrayIndexOutOfBoundsException e) {      
                System.out.println("Щоденник заповнений (максимум " + MealLog.CAPACITY
                        + " записів). Решту порцій не збережено. [" + e.getMessage() + "]");
                break;
            }
        }
    }

    private static void addMealFromInput(Scanner scanner, MealLog log) throws DomainException {
        System.out.print("Введіть назву продукту: ");
        String productName = scanner.nextLine();

        int weightGrams = readInt(scanner, "Введіть вагу спожитої порції (г): ");
        double caloriesPer100g = readDouble(scanner, "Введіть калорійність продукту на 100 г (ккал): ");
        double proteinPer100g = readDouble(scanner, "Введіть вміст білка на 100 г (г): ");

        System.out.print("Введіть прийом їжі (сніданок/обід/вечеря/перекус): ");
        String mealType = scanner.nextLine();

        Product product = log.addMeal(productName, weightGrams, caloriesPer100g, proteinPer100g, mealType);

        System.out.println();
        System.out.println("===== Результат обліку =====");
        System.out.printf("Продукт: %s%n", product.getProductName());
        System.out.printf("Прийом їжі: %s%n", product.getMealType());
        System.out.printf("Вага порції: %d г%n", product.getWeightGrams());
        System.out.printf("Отримано калорій: %.2f ккал%n", product.getTotalCalories());
        System.out.printf("Отримано білка: %.2f г%n", product.getTotalProtein());
        System.out.printf("Висновок: %s%n", product.getComment());
    }

    private static void printReport(MealLog log) {
        System.out.println("\n===== Підсумок щоденника =====");
        for (int i = 0; i < log.getSize(); i++) {
            Product p = log.getMeal(i);
            System.out.printf("%d) %s (%s) — %.2f ккал%n",
                    i + 1, p.getProductName(), p.getMealType(), p.getTotalCalories());
        }
        System.out.printf("Всього: %.2f ккал%n", log.totalCalories());
        System.out.printf("Середня калорійність порції: %d ккал%n", log.averageCalories());
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
                System.out.println("Помилка: очікується число, а введено \"" + bad
                        + "\". Перевірте роздільник дробової частини (кома або крапка залежно від локалі).");
            }
        }
    }
    
    private static void demoHierarchy() {
        System.out.println("\n===== Демо: catch базового класу DomainException =====");
        for (int i = 0; i < 2; i++) {
            try {
                if (i == 0) {
                    new Product("Тест", -5, 100, 10, "обід");         // InvalidNutritionValueException
                } else {
                    new Product("Тест", 100, 100, 10, "полуденок");   // InvalidMealTypeException
                }
            } catch (DomainException e) {
                System.out.println(e.getClass().getSimpleName() + " -> " + e.getMessage());
            }
        }
    }
}
