import java.util.Objects;

public final class FoodItemFactory {

    private FoodItemFactory() {
    }

    public static FoodItem create(FoodItemType type,
                                  String name, int weightGrams,
                                  double caloriesPer100g,
                                  double proteinPer100g,
                                  String mealType) {
        Objects.requireNonNull(type, "Тип їжі не може бути null");

        return switch (type) {
            case FRUIT -> new FruitProduct(name, weightGrams,
                    caloriesPer100g, proteinPer100g, mealType, 10);
            case PROTEIN -> new ProteinProduct(name, weightGrams,
                    caloriesPer100g, proteinPer100g, mealType, "тваринний");
            case SUPPLEMENT -> new SupplementProduct(name, weightGrams,
                    caloriesPer100g, proteinPer100g, mealType, 30);
        };
    }
}
