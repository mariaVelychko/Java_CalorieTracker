public abstract class AbstractAdviceStrategy implements MealAdviceStrategy {

    protected String format(String category, String explanation) {
        return String.format("[%s] %s", category, explanation);
    }
}
