import java.util.Objects;

public abstract sealed class FoodItem
        permits MealProduct, SupplementProduct {

    private final String name;
    private final int weightGrams;
    private final double caloriesPer100g;
    private final double proteinPer100g;
    private final String mealType;

    protected FoodItem(String name, int weightGrams,
                       double caloriesPer100g,
                       double proteinPer100g,
                       String mealType) {
        System.out.println("[FoodItem] Виклик базового конструктора");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва продукту не може бути порожньою");
        }
        if (weightGrams < 1 || weightGrams > 5000) {
            throw new IllegalArgumentException("Вага має бути від 1 до 5000 г");
        }
        if (!(caloriesPer100g >= 0 && caloriesPer100g <= 900)) {
            throw new IllegalArgumentException("Калорійність має бути від 0 до 900 ккал/100 г");
        }
        if (!(proteinPer100g >= 0 && proteinPer100g <= 100)) {
            throw new IllegalArgumentException("Білок має бути від 0 до 100 г/100 г");
        }

        this.name = name.trim();
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

    public String getName() {
        return name;
    }

    public int getWeightGrams() {
        return weightGrams;
    }

    public String getMealType() {
        return mealType;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) — %.2f ккал, %.2f г білка",
                name, mealType, getTotalCalories(), getTotalProtein());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodItem other)) return false;
        return weightGrams == other.weightGrams
                && Double.compare(caloriesPer100g, other.caloriesPer100g) == 0
                && Double.compare(proteinPer100g, other.proteinPer100g) == 0
                && name.equals(other.name)
                && mealType.equals(other.mealType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weightGrams, caloriesPer100g,
                proteinPer100g, mealType);
    }
}
