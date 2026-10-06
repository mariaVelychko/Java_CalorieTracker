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
