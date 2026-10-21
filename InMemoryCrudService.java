import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InMemoryCrudService<T extends BaseEntity> implements CRUDService<T> {
    private final Map<Long, T> storage = new LinkedHashMap<>();

    @Override
    public T findById(long id) throws EntityNotFoundException {
        T entity = storage.get(id);
        if (entity == null) {
            throw new EntityNotFoundException(id);
        }
        return entity;
    }

    @Override
    public void save(T entity) {
        Objects.requireNonNull(entity, "Сутність не може бути null");
        storage.put(entity.getId(), entity);
    }

    @Override
    public boolean delete(long id) {
        return storage.remove(id) != null;
    }

    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator) {
        List<T> copy = new ArrayList<>(storage.values()); 
        copy.sort(comparator);
        return copy;
    }

    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    protected Collection<T> rawValues() {
        return storage.values();
    }
}
