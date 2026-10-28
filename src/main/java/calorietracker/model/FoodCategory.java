package calorietracker.model;

/**
 * Друга сутність (ЛР10): категорія їжі («Цитрусові», «Ягоди»…).
 * Зв'язок «один до багатьох»: одна категорія — багато фруктів (fruit_product.category_id).
 */
public final class FoodCategory extends BaseEntity {

    private final String description; // може бути null — у БД колонка без NOT NULL

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
