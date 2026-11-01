package calorietracker.jpa;

import calorietracker.model.FruitProduct;
import calorietracker.service.FruitProductService;
import jakarta.persistence.EntityManager;
import java.util.List;

public class HibernateFruitProductService extends HibernateCrudService<FruitProduct>
        implements FruitProductService {
    public HibernateFruitProductService() {
        super(FruitProduct.class);
    }

    @Override
    public List<FruitProduct> findAllSortedByNameAndBySugar() {
        EntityManager em = jpa.createEntityManager();
        try {
            return em.createQuery("select f from FruitProduct f order by f.name, f.sugarGrams, f.id",
                    FruitProduct.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<FruitProduct> findByCategory(long categoryId) {
        EntityManager em = jpa.createEntityManager();
        try {
            return em.createQuery(
                            "select f from FruitProduct f where f.category.id = :categoryId order by f.name, f.id",
                            FruitProduct.class)
                    .setParameter("categoryId", categoryId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<FruitProduct> findPage(int page, int size) {
        EntityManager em = jpa.createEntityManager();
        try {
            return em.createQuery("select f from FruitProduct f order by f.name, f.id", FruitProduct.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
