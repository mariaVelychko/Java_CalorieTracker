/**
 * Абстрактний клас для стратегій, яким потрібне однакове форматування тексту поради.
 * Сюди винесено спільну логіку (format), щоб не дублювати її в кожній реалізації.
 */
public abstract class AbstractAdviceStrategy implements MealAdviceStrategy {

    protected String format(String category, String explanation) {
        return String.format("[%s] %s", category, explanation);
    }
}
