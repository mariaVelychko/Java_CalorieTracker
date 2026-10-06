public class MealAdviser {
    private MealAdviceStrategy strategy;

    public MealAdviser(MealAdviceStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(MealAdviceStrategy strategy) {
        this.strategy = strategy;
    }

    public String giveAdvice(Product product) {
        return strategy.shortLabel() + ": " + strategy.evaluate(product);
    }
}
