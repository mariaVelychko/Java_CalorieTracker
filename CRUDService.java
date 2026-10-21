import java.util.Comparator;
import java.util.List;

public interface CRUDService<T extends BaseEntity> {

    T findById(long id) throws EntityNotFoundException;

    void save(T entity);

    boolean delete(long id);

    List<T> findAllSorted(Comparator<? super T> comparator);

    default List<T> findAllSortedByName() {
        return findAllSorted(Comparator.comparing(BaseEntity::getName));
    }
}
