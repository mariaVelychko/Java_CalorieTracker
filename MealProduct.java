public non-sealed abstract class MealProduct extends FoodItem {

    protected MealProduct(String name, int weightGrams,
                          double caloriesPer100g,
                          double proteinPer100g,
                          String mealType) {
        super(name, weightGrams, caloriesPer100g,
              proteinPer100g, mealType);
        System.out.println("[MealProduct] Виклик конструктора MealProduct");
    }
}
