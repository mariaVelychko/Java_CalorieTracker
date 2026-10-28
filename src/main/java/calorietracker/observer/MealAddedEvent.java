package calorietracker.observer;

import calorietracker.model.Product;

// Незмінний знімок даних після успішного додавання порції.
public final class MealAddedEvent {
    private final Product product;
    private final int size;
    private final double totalCalories;
    public MealAddedEvent(Product product, int size, double totalCalories) {
        this.product = product; this.size = size; this.totalCalories = totalCalories;
    }
    public Product getProduct() { return product; }
    public int getSize() { return size; }
    public double getTotalCalories() { return totalCalories; }
}
