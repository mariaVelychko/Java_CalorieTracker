/**
 * Оцінка порції за калорійністю. Успадковує спільне форматування від AbstractAdviceStrategy.
 */
public class CalorieAdviceStrategy extends AbstractAdviceStrategy {
    private static final double LIGHT_LIMIT = 350.0;
    private static final double HEAVY_LIMIT = 700.0;

    @Override
    public String evaluate(Product product) {
        double total = product.getTotalCalories();
        if (total > HEAVY_LIMIT) {
            return format("Калорії", "Калорійний прийом їжі — варто врахувати це у денному раціоні.");
        } else if (total > LIGHT_LIMIT) {
            return format("Калорії", "Помірна калорійність прийому їжі.");
        }
        return format("Калорії", "Легкий прийом їжі за калорійністю.");
    }
}
