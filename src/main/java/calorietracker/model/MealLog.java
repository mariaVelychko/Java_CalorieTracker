package calorietracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import calorietracker.exception.DomainException;
import calorietracker.observer.MealAddedEvent;
import calorietracker.observer.MealLogListener;
public class MealLog {
    public static final int CAPACITY = 5;
    private final Product[] meals = new Product[CAPACITY];
    private int size;
    private final List<MealLogListener> listeners = new ArrayList<>();
    public void addListener(MealLogListener listener) {
        Objects.requireNonNull(listener);
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    public void removeListener(MealLogListener listener) { listeners.remove(listener); }
    public Product addMeal(String name, int weight, double caloriesPer100g,
                           double proteinPer100g, String mealType) throws DomainException {
        Product product;
        try { product = new Product(name, weight, caloriesPer100g, proteinPer100g, mealType); }
        catch (DomainException e) {
            System.out.println("[LOG] Запис відхилено (" + e.getClass().getSimpleName()
                + "), некоректне значення: " + e.getInvalidValue());
            throw e;
        }
        meals[size] = product; 
        size++;
        notifyListeners(new MealAddedEvent(product, size, totalCalories()));
        return product;
    }
    private void notifyListeners(MealAddedEvent event) {
        for (MealLogListener listener : new ArrayList<>(listeners)) {
            try { listener.onMealAdded(event); }
            catch (RuntimeException e) {
                System.err.println("[Observer] Помилка " + listener.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
    public int getSize() { return size; }
    public Product getMeal(int index) { return meals[index]; }
    public double totalCalories() {
        double sum = 0;
        for (int i = 0; i < size; i++) sum += meals[i].getTotalCalories();
        return sum;
    }
    public int averageCalories() { return (int)Math.round(totalCalories()) / size; }
}
