import java.util.List;

/**
 * Прийом їжі не належить до допустимого списку (сніданок/обід/вечеря/перекус).
 */
public class InvalidMealTypeException extends DomainException {
    private final List<String> allowedTypes;

    public InvalidMealTypeException(String value, List<String> allowedTypes) {
        super("Невідомий прийом їжі: \"" + value + "\"", value);
        this.allowedTypes = allowedTypes;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }
}
