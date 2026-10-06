public interface MealAdviceStrategy {

    String evaluate(Product product);

    default String shortLabel() {
        return "Порада";
    }
}
