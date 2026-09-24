import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Скільки продуктів ви хочете внести? ");
        int n = scanner.nextInt();
        scanner.nextLine(); 

        Product[] products = new Product[n];

        for (int i = 0; i < n; i++) {
            System.out.println("\n--- Продукт #" + (i + 1) + " ---");

            System.out.print("Назва продукту: ");
            String name = scanner.nextLine();

            System.out.print("Вага порції (г): ");
            int weight = scanner.nextInt();

            System.out.print("Калорійність на 100г (ккал): ");
            double calories = scanner.nextDouble();

            System.out.print("Білок на 100г (г): ");
            double protein = scanner.nextDouble();
            scanner.nextLine();

            products[i] = new Product(name, weight, calories, protein);
        }

        System.out.println("\n===== Усі внесені продукти =====");
        for (Product p : products) {
            System.out.println(p);
        }

        System.out.print("\nВведіть межу калорійності (ккал/100г) для підрахунку: ");
        double limit = scanner.nextDouble();
        scanner.nextLine();

        int countAboveLimit = 0;
        for (Product p : products) {
            if (p.getCaloriesPer100g() > limit) {
                countAboveLimit++;
            }
        }
        System.out.printf("Продуктів з калорійністю понад %.2f ккал/100г: %d%n", limit, countAboveLimit);

        System.out.println("\n===== До сортування (за калорійністю на 100г) =====");
        for (Product p : products) {
            System.out.println(p);
        }

        bubbleSortByCalories(products);

        System.out.println("\n===== Після сортування (за зростанням калорійності на 100г) =====");
        for (Product p : products) {
            System.out.println(p);
        }

        System.out.println("\n===== Пошук продукту в масиві =====");
        System.out.print("Назва продукту для пошуку: ");
        String searchName = scanner.nextLine();
        System.out.print("Вага порції (г): ");
        int searchWeight = scanner.nextInt();
        System.out.print("Калорійність на 100г (ккал): ");
        double searchCalories = scanner.nextDouble();
        System.out.print("Білок на 100г (г): ");
        double searchProtein = scanner.nextDouble();

        Product sample = new Product(searchName, searchWeight, searchCalories, searchProtein);
        int index = linearSearch(products, sample);

        if (index != -1) {
            System.out.println("Знайдено! Індекс у масиві: " + index);
            System.out.println(products[index]);
        } else {
            System.out.println("Такого продукту в масиві немає.");
        }

        scanner.close();
    }

    private static void bubbleSortByCalories(Product[] products) {
        int n = products.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (products[j].getCaloriesPer100g() > products[j + 1].getCaloriesPer100g()) {
                    Product temp = products[j];
                    products[j] = products[j + 1];
                    products[j + 1] = temp;
                }
            }
        }
    }

    private static int linearSearch(Product[] products, Product sample) {
        for (int i = 0; i < products.length; i++) {
            if (products[i].equals(sample)) {
                return i;
            }
        }
        return -1;
    }
}
