/**
 * Базовий виняток предметної області «Облік калорій» (checked).
 * Несе додаткове поле invalidValue — значення, яке порушило доменне правило.
 */
public class DomainException extends Exception {
    private final Object invalidValue;

    public DomainException(String message, Object invalidValue) {
        super(message);
        this.invalidValue = invalidValue;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}
