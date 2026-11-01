package calorietracker.exception;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, SQLException cause) {
        super(message + ": " + cause.getMessage(), cause);
    }
}
