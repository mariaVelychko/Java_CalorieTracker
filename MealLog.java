/**
 * Щоденник харчування. Записи зберігаються у масиві фіксованого розміру.
 */
public class MealLog {
    public static final int CAPACITY = 5;

    private final Product[] meals = new Product[CAPACITY];
    private int size = 0;

    /**
     * Створює продукт і додає його в щоденник.
     * Доменний виняток логується тут і прокидається далі (re-throw) —
     * рішення про реакцію приймає код вищого рівня (Main).
     * Якщо щоденник заповнений, масив кине ArrayIndexOutOfBoundsException.
     */
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
        meals[size] = product; // при size == CAPACITY -> ArrayIndexOutOfBoundsException
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

    /** Середня калорійність порції (цілочисельне ділення: при size == 0 -> ArithmeticException). */
    public int averageCalories() {
        int total = (int) Math.round(totalCalories());
        return total / size;
    }
}
