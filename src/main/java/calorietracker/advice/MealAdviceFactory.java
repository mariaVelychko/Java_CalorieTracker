package calorietracker.advice;

import java.util.Objects;
// Simple Factory: відповідає вимозі окремого Factory-класу з параметром типу.
public final class MealAdviceFactory {
    private MealAdviceFactory() { }
    public static MealAdviceStrategy create(AdviceType type) {
        Objects.requireNonNull(type, "Тип стратегії не може бути null");
        switch (type) {
            case CALORIE: return new CalorieAdviceStrategy();
            case PROTEIN: return new ProteinAdviceStrategy();
            case BALANCED: return new BalancedAdviceStrategy(create(AdviceType.CALORIE), create(AdviceType.PROTEIN));
            case LEGACY: return new LegacyAdviceAdapter(new LegacyNutritionService());
            default: throw new IllegalArgumentException("Невідомий тип стратегії: " + type);
        }
    }
    public static MealAdviceStrategy create(String code) {
        if (code == null) throw new IllegalArgumentException("Код стратегії не може бути null");
        try { return create(AdviceType.valueOf(code.trim().toUpperCase(java.util.Locale.ROOT))); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Невідомий код стратегії: " + code, e); }
    }
}
