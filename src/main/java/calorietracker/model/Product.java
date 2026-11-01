package calorietracker.model;

import java.util.List;
import java.util.Locale;
import calorietracker.exception.DomainException;
import calorietracker.exception.InvalidMealTypeException;
import calorietracker.exception.InvalidNutritionValueException;
public class Product implements Comparable<Product>, Reportable {
    public static final List<String> MEAL_TYPES = List.of("сніданок", "обід", "вечеря", "перекус");
    private final String productName, mealType;
    private final int weightGrams;
    private final double caloriesPer100g, proteinPer100g;
    public Product(String productName, int weightGrams, double caloriesPer100g,
                   double proteinPer100g, String mealType) throws DomainException {
        if (productName == null || productName.isBlank())
            throw new DomainException("Назва продукту не може бути порожньою", productName);
        checkRange("вага порції (г)", weightGrams, 1, 5000);
        checkRange("калорійність на 100 г", caloriesPer100g, 0, 900);
        checkRange("білок на 100 г", proteinPer100g, 0, 100);
        String normalized = mealType == null ? "" : mealType.trim().toLowerCase(Locale.ROOT);
        if (!MEAL_TYPES.contains(normalized)) throw new InvalidMealTypeException(mealType, MEAL_TYPES);
        this.productName = productName.trim(); this.weightGrams = weightGrams;
        this.caloriesPer100g = caloriesPer100g; this.proteinPer100g = proteinPer100g;
        this.mealType = normalized;
    }
    private static void checkRange(String field, double value, double min, double max)
            throws InvalidNutritionValueException {
        if (!(value >= min && value <= max)) throw new InvalidNutritionValueException(field, value, min, max);
    }
    public double getTotalCalories() { return caloriesPer100g * weightGrams / 100.0; }
    public double getTotalProtein() { return proteinPer100g * weightGrams / 100.0; }
    public String getProductName() { return productName; }
    public int getWeightGrams() { return weightGrams; }
    public String getMealType() { return mealType; }
    public int compareTo(Product other) { return Double.compare(getTotalCalories(), other.getTotalCalories()); }
    public String toReportLine() {
        return String.format("%s (%s) — %.2f ккал, %.2f г білка", productName, mealType, getTotalCalories(), getTotalProtein());
    }
}
