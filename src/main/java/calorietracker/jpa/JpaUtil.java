package calorietracker.jpa;

import calorietracker.jdbc.DBConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

public final class JpaUtil {
    private final EntityManagerFactory emf;

    private JpaUtil() {
        System.setProperty("org.jboss.logging.provider", "slf4j");
        DBConnection.getInstance();
        emf = Persistence.createEntityManagerFactory("calorie-unit");
        Runtime.getRuntime().addShutdownHook(new Thread(emf::close));
    }

    private static final class Holder {
        private static final JpaUtil INSTANCE = new JpaUtil();
    }

    public static JpaUtil getInstance() {
        return Holder.INSTANCE;
    }

    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public void commit(EntityManager em) {
        em.getTransaction().commit();
    }

    public void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    public Statistics getStatistics() {
        return emf.unwrap(SessionFactory.class).getStatistics();
    }
}
