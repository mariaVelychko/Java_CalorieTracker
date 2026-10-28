package calorietracker.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import calorietracker.exception.DataAccessException;

/**
 * Допоміжний клас для транзакцій: одна точка, де є setAutoCommit(false), commit() і rollback().
 */
public final class Tx {

    /** Робота, яка виконується всередині транзакції на одному Connection. */
    @FunctionalInterface
    public interface Work<R> {
        R run(Connection connection) throws SQLException;
    }

    private Tx() {
    }

    public static <R> R run(DataSource dataSource, Work<R> work) {
        try (Connection c = dataSource.getConnection()) {
            boolean previousAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);              // з цього моменту зміни НЕ видимі іншим, доки немає commit()
            try {
                R result = work.run(c);
                c.commit();                       // усі зміни фіксуються разом
                return result;
            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(c, e);            // будь-який збій — скасовуємо ВСЕ, що зробили в цій транзакції
                if (e instanceof SQLException sql) {
                    throw new DataAccessException("Транзакцію скасовано (rollback)", sql);
                }
                throw (RuntimeException) e;
            } finally {
                c.setAutoCommit(previousAutoCommit); // важливо для з'єднань з пулу
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
