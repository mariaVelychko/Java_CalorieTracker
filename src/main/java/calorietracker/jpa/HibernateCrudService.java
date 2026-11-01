package calorietracker.jpa;

import calorietracker.exception.EntityNotFoundException;
import calorietracker.model.BaseEntity;
import calorietracker.service.CRUDService;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HibernateCrudService<T extends BaseEntity> implements CRUDService<T> {
    protected final Class<T> type;
    protected final JpaUtil jpa = JpaUtil.getInstance();

    public HibernateCrudService(Class<T> type) {
        this.type = type;
    }

    @Override
    public T findById(long id) throws EntityNotFoundException {
        EntityManager em = jpa.createEntityManager();
        try {
            T entity = em.find(type, id);
            if (entity == null) {
                throw new EntityNotFoundException(id);
            }
            return entity;
        } finally {
            em.close();
        }
    }

    @Override
    public void save(T entity) {
        Objects.requireNonNull(entity, "Сутність не може бути null");
        EntityManager em = jpa.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(entity);
            jpa.commit(em);
        } catch (RuntimeException e) {
            jpa.rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(long id) {
        EntityManager em = jpa.createEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(type, id);
            if (entity != null) {
                em.remove(entity);
            }
            jpa.commit(em);
            return entity != null;
        } catch (RuntimeException e) {
            jpa.rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator) {
        List<T> all = findAllSortedByName();
        all.sort(comparator);
        return all;
    }

    @Override
    public List<T> findAllSortedByName() {
        EntityManager em = jpa.createEntityManager();
        try {
            return em.createQuery("select e from " + type.getSimpleName() + " e order by e.name, e.id", type)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
