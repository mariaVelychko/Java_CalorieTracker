import java.util.Objects;

public final class FoodItemFactory {

    private FoodItemFactory() {
    }

    public static FoodItem create(long id, FoodItemType type,
                                  String name, int weightGrams,
                                  double caloriesPer100g,
                                  double proteinPer100g,
                                  String mealType) {
        Objects.requireNonNull(type, "Тип їжі не може бути null");

        return switch (type) {
            case FRUIT -> new FruitProduct(id, name, weightGrams,
                    caloriesPer100g, proteinPer100g, mealType, 10);
            case PROTEIN -> new ProteinProduct(id, name, weightGrams,
                    caloriesPer100g, proteinPer100g, mealType, "тваринний");
            case SUPPLEMENT -> new SupplementProduct(id, name, weightGrams,
                    caloriesPer100g, proteinPer100g, mealType, 30);
        };
    }
}
