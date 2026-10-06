/**
 * Комбінована оцінка: об'єднує результат двох інших стратегій.
 * Реалізує MealAdviceStrategy напряму ("чистий" інтерфейс), бо не потребує
 * спільного форматування з AbstractAdviceStrategy — лише делегує іншим стратегіям.
 */
public class BalancedAdviceStrategy implements MealAdviceStrategy {
    private final MealAdviceStrategy calorieAdvice = new CalorieAdviceStrategy();
    private final MealAdviceStrategy proteinAdvice = new ProteinAdviceStrategy();

    @Override
    public String evaluate(Product product) {
        return calorieAdvice.evaluate(product) + " " + proteinAdvice.evaluate(product);
    }

    @Override
    public String shortLabel() {
        return "Комбінована оцінка";
    }
}
