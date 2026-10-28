package calorietracker.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlConsumer {
    void accept(PreparedStatement ps) throws SQLException;
}
