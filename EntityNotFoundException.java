/**
 * Перевірений (checked) виняток — за тим самим принципом, що й DomainException
 * з ЛР3: відсутність сутності за id — очікувана й відновлювана ситуація
 * (викликач може показати повідомлення й попросити інший id), тому компілятор
 * має змусити цей випадок явно обробити.
 */
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
