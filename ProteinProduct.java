import java.util.Objects;

public final class ProteinProduct extends MealProduct {

    private final String proteinType;

    public ProteinProduct(long id, String name, int weightGrams,
                          double caloriesPer100g,
                          double proteinPer100g,
                          String mealType,
                          String proteinType) {
        super(id, name, weightGrams, caloriesPer100g, proteinPer100g, mealType);
        this.proteinType = proteinType;
    }

    @Override
    public String getFoodDescription() {
        return "Білковий продукт: " + proteinType;
    }

    @Override
    public String toString() {
        return super.toString() + ", тип білка: " + proteinType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProteinProduct other)) return false;
        return super.equals(o)
                && proteinType.equals(other.proteinType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), proteinType);
    }

    public String getProteinType() {
        return proteinType;
    }
}
