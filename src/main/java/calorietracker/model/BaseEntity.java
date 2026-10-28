package calorietracker.model;

/**
 * Базовий клас для всіх сутностей, якими керує CRUD-сервіс.
 * Поле id незмінне після створення (final, задається лише в конструкторі).
 */
public abstract class BaseEntity {
    private final long id;
    private final String name;

    protected BaseEntity(long id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва сутності не може бути порожньою");
        }
        this.id = id;
        this.name = name.trim();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Ідентичність за id: дві сутності з однаковим id вважаються "однією й тією самою". */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "#" + id + " " + name;
    }
}
