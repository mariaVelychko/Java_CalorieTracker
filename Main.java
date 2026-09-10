import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть назву продукту: ");
        String productName = scanner.nextLine(); 

        System.out.print("Введіть вагу спожитої порції (г): ");
        int weightGrams = scanner.nextInt(); 
        System.out.print("Введіть калорійність продукту на 100 г (ккал): ");
        double caloriesPer100g = scanner.nextDouble(); 

        System.out.print("Введіть вміст білка на 100 г (г): ");
        double proteinPer100g = scanner.nextDouble();

        scanner.nextLine();

        System.out.print("Введіть прийом їжі (сніданок/обід/вечеря/перекус): ");
        String mealType = scanner.nextLine(); 

        double totalCalories = caloriesPer100g * weightGrams / 100.0;
        double totalProtein = proteinPer100g * weightGrams / 100.0;

        String comment;
        double mealLimit = 700.0; 

        if (totalCalories > mealLimit) {
            comment = "Це калорійний прийом їжі — варто врахувати це у денному раціоні.";
        } else if (totalCalories > mealLimit / 2) {
            comment = "Помірна калорійність прийому їжі.";
        } else {
            comment = "Легкий прийом їжі за калорійністю.";
        }

        System.out.println();
        System.out.println("===== Результат обліку =====");
        System.out.printf("Продукт: %s%n", productName);
        System.out.printf("Прийом їжі: %s%n", mealType);
        System.out.printf("Вага порції: %d г%n", weightGrams);
        System.out.printf("Отримано калорій: %.2f ккал%n", totalCalories);
        System.out.printf("Отримано білка: %.2f г%n", totalProtein);
        System.out.printf("Висновок: %s%n", comment);

        scanner.close();
    }
}
