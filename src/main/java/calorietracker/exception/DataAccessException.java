package calorietracker.exception;

import java.sql.SQLException;

/**
 * Неперевірений (unchecked) виняток для збоїв доступу до БД.
 *
 * Чому unchecked: інтерфейс CRUDService з ЛР7 не оголошує SQLException (там сховище
 * було в пам'яті). Щоб JDBC-реалізація підставлялась БЕЗ зміни інтерфейсу й коду,
 * що ним користується, SQLException загортається в RuntimeException.
 * Очікувана ситуація «id не знайдено» залишилась перевіреним EntityNotFoundException.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, SQLException cause) {
        super(message + ": " + cause.getMessage(), cause);
    }
}
