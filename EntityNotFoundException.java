public class EntityNotFoundException extends Exception {
    private final long id;

    public EntityNotFoundException(long id) {
        super("Сутність з id=" + id + " не знайдена");
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
