package calorietracker.advice;

// Штучно змодельований сторонній API. Не реалізує MealAdviceStrategy.
// Приймає енергію в кДж і білок у міліграмах, повертає числовий код.
public final class LegacyNutritionService {
    public int classifyServing(double energyKilojoules, double proteinMilligrams) {
        if (energyKilojoules > 2928.8) return 2;
        if (proteinMilligrams >= 30000) return 1;
        return 0;
    }
}
