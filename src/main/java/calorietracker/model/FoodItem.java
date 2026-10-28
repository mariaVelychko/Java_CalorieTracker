package calorietracker.model;

import java.util.Objects;

public abstract sealed class FoodItem extends BaseEntity
        permits MealProduct, SupplementProduct {

    public static volatile boolean traceConstructors = true;

    private final int weightGrams;
    private final double caloriesPer100g;
    private final double proteinPer100g;
    private final String mealType;

    protected FoodItem(long id, String name, int weightGrams,
                       double caloriesPer100g,
                       double proteinPer100g,
                       String mealType) {
        super(id, name); 
        if (traceConstructors) System.out.println("[FoodItem] Виклик базового конструктора");

        if (weightGrams < 1 || weightGrams > 5000) {
            throw new IllegalArgumentException("Вага має бути від 1 до 5000 г");
        }
        if (!(caloriesPer100g >= 0 && caloriesPer100g <= 900)) {
            throw new IllegalArgumentException("Калорійність має бути від 0 до 900 ккал/100 г");
        }
        if (!(proteinPer100g >= 0 && proteinPer100g <= 100)) {
            throw new IllegalArgumentException("Білок має бути від 0 до 100 г/100 г");
        }

        this.weightGrams = weightGrams;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinPer100g = proteinPer100g;
        this.mealType = mealType;
    }

    public abstract String getFoodDescription();

    public final double getTotalCalories() {
        return caloriesPer100g * weightGrams / 100.0;
    }

    public final double getTotalProtein() {
        return proteinPer100g * weightGrams / 100.0;
    }

    public int getWeightGrams() {
        return weightGrams;
    }


    public double getCaloriesPer100g() {
        return caloriesPer100g;
    }

    public double getProteinPer100g() {
        return proteinPer100g;
    }

    public String getMealType() {
        return mealType;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) — %.2f ккал, %.2f г білка",
                getName(), mealType, getTotalCalories(), getTotalProtein());
    }

    /**
     * Навмисно ЗАЛИШЕНО структурну рівність з ЛР6 (за значеннями полів),
     * а не id-рівність з BaseEntity. Це означає, що FoodItem.equals()
     * НЕ узгоджений з ідентичністю за id — дивись пояснення в README.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodItem other)) return false;
        return weightGrams == other.weightGrams
                && Double.compare(caloriesPer100g, other.caloriesPer100g) == 0
                && Double.compare(proteinPer100g, other.proteinPer100g) == 0
                && getName().equals(other.getName())
                && mealType.equals(other.mealType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), weightGrams, caloriesPer100g,
                proteinPer100g, mealType);
    }
}
