public class InvalidNutritionValueException extends DomainException {
    private final String fieldName;
    private final double min;
    private final double max;

    public InvalidNutritionValueException(String fieldName, double value, double min, double max) {
        super(String.format("Поле «%s» має бути в межах від %.0f до %.0f", fieldName, min, max), value == Math.rint(value) ? (Object) (long) value : (Object) value);
        this.fieldName = fieldName;
        this.min = min;
        this.max = max;
    }

    public String getFieldName() {
        return fieldName;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
