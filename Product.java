public class Product {
    private String name;
    private int weightGrams;
    private double caloriesPer100g;
    private double proteinPer100g;

    public Product(String name, int weightGrams, double caloriesPer100g, double proteinPer100g) {
        this.name = name;
        this.weightGrams = weightGrams;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinPer100g = proteinPer100g;
    }

    public String getName() {
        return name;
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

    public double getTotalCalories() {
        return caloriesPer100g * weightGrams / 100.0;
    }

    @Override
    public String toString() {
        return String.format("%-15s | %4d г | %7.2f ккал/100г | %6.2f г білка/100г | усього: %7.2f ккал",
                name, weightGrams, caloriesPer100g, proteinPer100g, getTotalCalories());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product other = (Product) obj;
        return weightGrams == other.weightGrams
                && Double.compare(caloriesPer100g, other.caloriesPer100g) == 0
                && Double.compare(proteinPer100g, other.proteinPer100g) == 0
                && name.equalsIgnoreCase(other.name);
    }
}
