public class MealLog {
    public static final int CAPACITY = 5;

    private final Product[] meals = new Product[CAPACITY];
    private int size = 0;

    public Product addMeal(String name, int weight, double caloriesPer100g,
                           double proteinPer100g, String mealType) throws DomainException {
        Product product;
        try {
            product = new Product(name, weight, caloriesPer100g, proteinPer100g, mealType);
        } catch (DomainException e) {
            System.out.println("[LOG] Запис відхилено (" + e.getClass().getSimpleName()
                    + "), некоректне значення: " + e.getInvalidValue());
            throw e; // re-throw
        }
        meals[size] = product;
        size++;
        return product;
    }

    public int getSize() {
        return size;
    }

    public Product getMeal(int index) {
        return meals[index];
    }

    public double totalCalories() {
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += meals[i].getTotalCalories();
        }
        return sum;
    }

    public int averageCalories() {
        int total = (int) Math.round(totalCalories());
        return total / size;
    }
}
