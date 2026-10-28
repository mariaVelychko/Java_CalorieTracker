package calorietracker.service;

import java.util.Comparator;
import java.util.List;
import calorietracker.exception.EntityNotFoundException;
import calorietracker.model.BaseEntity;

/**
 * Узагальнений контракт CRUD-операцій. Обмеження T extends BaseEntity потрібне,
 * щоб усередині сервісу можна було викликати entity.getId() — без нього
 * компілятор бачив би в T лише методи Object і getId() був би недоступний.
 */
public interface CRUDService<T extends BaseEntity> {

    T findById(long id) throws EntityNotFoundException;

    void save(T entity);

    boolean delete(long id);

    /** Рівень 3: сортування за довільним компаратором — основа для інших методів сортування. */
    List<T> findAllSorted(Comparator<? super T> comparator);

    /**
     * Рівень 3: default-метод, побудований поверх findAllSorted(...).
     * Параметр у findAllSorted має тип Comparator<? super T>, а не Comparator<T>,
     * щоб можна було передати компаратор, написаний для BaseEntity (суперкласу T) —
     * наприклад, Comparator.comparing(BaseEntity::getName) — а не лише компаратор,
     * написаний рівно під T.
     */
    default List<T> findAllSortedByName() {
        return findAllSorted(Comparator.comparing(BaseEntity::getName));
    }
}
