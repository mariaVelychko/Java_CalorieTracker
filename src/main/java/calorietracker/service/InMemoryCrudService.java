package calorietracker.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import calorietracker.exception.EntityNotFoundException;
import calorietracker.model.BaseEntity;

/**
 * Узагальнена реалізація сховища в пам'яті. Той самий клас без жодних змін
 * працює з будь-якою сутністю, що успадковує BaseEntity (демонстрація — у Main).
 *
 * Сховище: LinkedHashMap<Long, T>. Обґрунтування — у README (таблиця порівняння
 * ArrayList / HashMap / LinkedHashMap / TreeMap).
 */
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
        // put: якщо ключ (id) уже є — замінює значення й повертає старе;
        // якщо ключа немає — додає нове й повертає null. Саме так одним
        // викликом реалізується і "додати", і "оновити".
        storage.put(entity.getId(), entity);
    }

    @Override
    public boolean delete(long id) {
        return storage.remove(id) != null;
    }

    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator) {
        List<T> copy = new ArrayList<>(storage.values()); // нова колекція — сховище не чіпаємо
        copy.sort(comparator);
        return copy;
    }

    /** Непотрібне контрактом, але зручне для демонстрації "до сортування". */
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    protected Collection<T> rawValues() {
        return storage.values();
    }
}
