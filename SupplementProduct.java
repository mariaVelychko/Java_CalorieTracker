import java.util.Objects;

public final class SupplementProduct extends FoodItem {

    private final double dailyNormGrams;

    public SupplementProduct(String name, int weightGrams,
                             double caloriesPer100g,
                             double proteinPer100g,
                             String mealType,
                             double dailyNormGrams) {
        super(name, weightGrams, caloriesPer100g, proteinPer100g, mealType);
        this.dailyNormGrams = dailyNormGrams;
    }

    @Override
    public String getFoodDescription() {
        return "Добавка, добова норма: " + dailyNormGrams + " г";
    }

    @Override
    public String toString() {
        return super.toString() + ", добова норма: " + dailyNormGrams + " г";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplementProduct other)) return false;
        return super.equals(o)
                && Double.compare(dailyNormGrams, other.dailyNormGrams) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dailyNormGrams);
    }

    public double getDailyNormGrams() {
        return dailyNormGrams;
    }
}
