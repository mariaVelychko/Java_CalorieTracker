import java.util.List;
import java.util.Locale;

public class Product {
    public static final List<String> MEAL_TYPES = List.of("сніданок", "обід", "вечеря", "перекус");

    private static final int MIN_WEIGHT_GRAMS = 1;
    private static final int MAX_WEIGHT_GRAMS = 5000;
    private static final double MAX_CALORIES_PER_100G = 900.0;
    private static final double MAX_PROTEIN_PER_100G = 100.0;
    private static final double MEAL_LIMIT = 700.0;

    private final String productName;
    private final int weightGrams;
    private final double caloriesPer100g;
    private final double proteinPer100g;
    private final String mealType;

    public Product(String productName, int weightGrams, double caloriesPer100g,
                   double proteinPer100g, String mealType) throws DomainException {
        if (productName == null || productName.isBlank()) {
            throw new DomainException("Назва продукту не може бути порожньою", productName);
        }
        checkRange("вага порції (г)", weightGrams, MIN_WEIGHT_GRAMS, MAX_WEIGHT_GRAMS);
        checkRange("калорійність на 100 г", caloriesPer100g, 0, MAX_CALORIES_PER_100G);
        checkRange("білок на 100 г", proteinPer100g, 0, MAX_PROTEIN_PER_100G);

        this.productName = productName.trim();
        this.weightGrams = weightGrams;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinPer100g = proteinPer100g;
        this.mealType = normalizeMealType(mealType);
    }

    private static void checkRange(String field, double value, double min, double max)
            throws InvalidNutritionValueException {
        if (!(value >= min && value <= max)) {
            throw new InvalidNutritionValueException(field, value, min, max);
        }
    }

    private static String normalizeMealType(String mealType) throws InvalidMealTypeException {
        String normalized = mealType == null ? "" : mealType.trim().toLowerCase(Locale.ROOT);
        if (!MEAL_TYPES.contains(normalized)) {
            throw new InvalidMealTypeException(mealType, MEAL_TYPES);
        }
        return normalized;
    }

    public double getTotalCalories() {
        return caloriesPer100g * weightGrams / 100.0;
    }

    public double getTotalProtein() {
        return proteinPer100g * weightGrams / 100.0;
    }

    public String getComment() {
        double total = getTotalCalories();
        if (total > MEAL_LIMIT) {
            return "Це калорійний прийом їжі — варто врахувати це у денному раціоні.";
        } else if (total > MEAL_LIMIT / 2) {
            return "Помірна калорійність прийому їжі.";
        }
        return "Легкий прийом їжі за калорійністю.";
    }

    public String getProductName() {
        return productName;
    }

    public int getWeightGrams() {
        return weightGrams;
    }

    public String getMealType() {
        return mealType;
    }
}
