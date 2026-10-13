import java.util.Objects;
public class BalancedAdviceStrategy implements MealAdviceStrategy {
    private final MealAdviceStrategy calorieAdvice, proteinAdvice;
    public BalancedAdviceStrategy(MealAdviceStrategy calorieAdvice, MealAdviceStrategy proteinAdvice) {
        this.calorieAdvice = Objects.requireNonNull(calorieAdvice);
        this.proteinAdvice = Objects.requireNonNull(proteinAdvice);
    }
    public String evaluate(Product product) {
        return calorieAdvice.evaluate(product) + " " + proteinAdvice.evaluate(product);
    }
    public String shortLabel() { return "Комбінована оцінка"; }
}
