package calorietracker.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import calorietracker.exception.DataAccessException;

public final class Tx {

    @FunctionalInterface
    public interface Work<R> {
        R run(Connection connection) throws SQLException;
    }

    private Tx() {
    }

    public static <R> R run(DataSource dataSource, Work<R> work) {
        try (Connection c = dataSource.getConnection()) {
            boolean previousAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);            
            try {
                R result = work.run(c);
                c.commit();                       
                return result;
            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(c, e);           
                if (e instanceof SQLException sql) {
                    throw new DataAccessException("Транзакцію скасовано (rollback)", sql);
                }
                throw (RuntimeException) e;
            } finally {
                c.setAutoCommit(previousAutoCommit); 
            }
        } catch (SQLException e) {
            throw new DataAccessException("Помилка роботи з'єднання", e);
        }
    }

    private static void rollbackQuietly(Connection c, Exception original) {
        try {
            c.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
