package calorietracker.model;

import java.util.Objects;

public final class FruitProduct extends MealProduct {

    private final double sugarGrams;
    private final boolean fresh;
    private final Long categoryId;

    public FruitProduct(long id, String name, int weightGrams,
                        double caloriesPer100g,
                        double proteinPer100g,
                        String mealType,
                        double sugarGrams) {
        this(id, name, weightGrams, caloriesPer100g, proteinPer100g,
             mealType, sugarGrams, true);
    }

    public FruitProduct(long id, String name, int weightGrams,
                        double caloriesPer100g,
                        double proteinPer100g,
                        String mealType,
                        double sugarGrams,
                        boolean fresh) {
        this(id, name, weightGrams, caloriesPer100g, proteinPer100g,
             mealType, sugarGrams, fresh, null);
    }

    public FruitProduct(long id, String name, int weightGrams,
                        double caloriesPer100g,
                        double proteinPer100g,
                        String mealType,
                        double sugarGrams,
                        boolean fresh,
                        Long categoryId) {
        super(id, name, weightGrams, caloriesPer100g, proteinPer100g, mealType);
        this.sugarGrams = sugarGrams;
        this.fresh = fresh;
        this.categoryId = categoryId;
    }

    @Override
    public String getFoodDescription() {
        return "Фрукт із вмістом цукру " + sugarGrams + " г";
    }

    @Override
    public String toString() {
        return super.toString() + ", цукор: " + sugarGrams + " г";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FruitProduct other)) return false;
        return super.equals(o)
                && Double.compare(sugarGrams, other.sugarGrams) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sugarGrams);
    }

    public double getSugarGrams() {
        return sugarGrams;
    }

    public boolean isFresh() {
        return fresh;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public FruitProduct withCategory(Long newCategoryId) {
        return new FruitProduct(getId(), getName(), getWeightGrams(), getCaloriesPer100g(),
                getProteinPer100g(), getMealType(), sugarGrams, fresh, newCategoryId);
    }
}
