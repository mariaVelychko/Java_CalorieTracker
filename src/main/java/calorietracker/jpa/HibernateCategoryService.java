package calorietracker.jpa;

import calorietracker.exception.EntityNotFoundException;
import calorietracker.model.FoodCategory;
import jakarta.persistence.EntityManager;
import java.util.List;

public class HibernateCategoryService extends HibernateCrudService<FoodCategory> {
    public HibernateCategoryService() {
        super(FoodCategory.class);
    }

    public FoodCategory findByIdWithFruits(long id) throws EntityNotFoundException {
        EntityManager em = jpa.createEntityManager();
        try {
            List<FoodCategory> result = em.createQuery(
                            "select c from FoodCategory c left join fetch c.fruits where c.id = :id",
                            FoodCategory.class)
                    .setParameter("id", id)
                    .getResultList();
            if (result.isEmpty()) {
                throw new EntityNotFoundException(id);
            }
            return result.get(0);
        } finally {
            em.close();
        }
    }

    public int countFruitsNPlusOne() {
        EntityManager em = jpa.createEntityManager();
        try {
            List<FoodCategory> categories = em.createQuery(
                    "select c from FoodCategory c order by c.name", FoodCategory.class).getResultList();
            int total = 0;
            for (FoodCategory category : categories) {
                total += category.getFruits().size();
            }
            return total;
        } finally {
            em.close();
        }
    }

    public int countFruitsJoinFetch() {
        EntityManager em = jpa.createEntityManager();
        try {
            List<FoodCategory> categories = em.createQuery(
                    "select distinct c from FoodCategory c left join fetch c.fruits order by c.name",
                    FoodCategory.class).getResultList();
            int total = 0;
            for (FoodCategory category : categories) {
                total += category.getFruits().size();
            }
            return total;
        } finally {
            em.close();
        }
    }
}
