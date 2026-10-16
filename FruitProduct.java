import java.util.Objects;

public final class FruitProduct extends MealProduct {

    private final double sugarGrams;

    public FruitProduct(String name, int weightGrams,
                        double caloriesPer100g,
                        double proteinPer100g,
                        String mealType,
                        double sugarGrams) {
        this(name, weightGrams, caloriesPer100g, proteinPer100g,
             mealType, sugarGrams, true);
    }

    public FruitProduct(String name, int weightGrams,
                        double caloriesPer100g,
                        double proteinPer100g,
                        String mealType,
                        double sugarGrams,
                        boolean fresh) {
        super(name, weightGrams, caloriesPer100g, proteinPer100g, mealType);
        this.sugarGrams = sugarGrams;
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
}
