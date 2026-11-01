package calorietracker.advice;

import java.util.Objects;
import calorietracker.model.Product;
public class LegacyAdviceAdapter implements MealAdviceStrategy {
    private final LegacyNutritionService service;
    public LegacyAdviceAdapter(LegacyNutritionService service) { this.service = Objects.requireNonNull(service); }
    public String evaluate(Product product) {
        int code = service.classifyServing(product.getTotalCalories() * 4.184, product.getTotalProtein() * 1000);
        switch (code) {
            case 2: return "[Зовнішній сервіс] Калорійна порція.";
            case 1: return "[Зовнішній сервіс] Високобілкова порція.";
            case 0: return "[Зовнішній сервіс] Звичайна порція.";
            default: throw new IllegalStateException("Невідомий код зовнішнього сервісу: " + code);
        }
    }
    public String shortLabel() { return "Адаптована оцінка"; }
}
