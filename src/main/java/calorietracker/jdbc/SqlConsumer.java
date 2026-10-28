package calorietracker.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Прив'язка параметрів до PreparedStatement (звичайний Consumer не може кидати SQLException). */
@FunctionalInterface
public interface SqlConsumer {
    void accept(PreparedStatement ps) throws SQLException;
}
