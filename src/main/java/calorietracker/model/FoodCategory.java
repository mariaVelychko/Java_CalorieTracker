package calorietracker.model;

public final class FoodCategory extends BaseEntity {

    private final String description; 

    public FoodCategory(long id, String name, String description) {
        super(id, name);
        this.description = description;
    }

    public FoodCategory(long id, String name) {
        this(id, name, null);
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return super.toString() + (description == null ? "" : " — " + description);
    }
}
