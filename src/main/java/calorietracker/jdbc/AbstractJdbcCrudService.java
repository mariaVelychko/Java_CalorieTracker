package calorietracker.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import calorietracker.exception.DataAccessException;
import calorietracker.exception.EntityNotFoundException;
import calorietracker.model.BaseEntity;
import calorietracker.service.CRUDService;

/**
 * Template Method: ТУТ написаний «каркас» усіх операцій (відкрити з'єднання, підготувати
 * запит, виконати, перетворити рядки, закрити ресурси, обробити помилки).
 * Підклас лише підставляє «змінні кроки»:
 *   tableName(), selectSql(), insertSql(), updateSql(), mapRow(ResultSet), setParameters(ps, entity).
 *
 * Це JDBC-аналог InMemoryCrudService<T> з ЛР7: той самий інтерфейс CRUDService<T>,
 * тому код, що користується сервісом, не помічає різниці.
 *
 * Домовленість про параметри: setParameters прив'язує значення у порядку
 *   [колонки даних..., id],
 * тобто id — ЗАВЖДИ останній параметр. Тому один і той самий setParameters
 * підходить і для INSERT (…, id) VALUES (?, …, ?), і для UPDATE … SET … WHERE id = ?.
 */
public abstract class AbstractJdbcCrudService<T extends BaseEntity> implements CRUDService<T> {

    private static final int MAX_PAGE_SIZE = 1000;
    private static final int BATCH_CHUNK = 1000;

    protected final DataSource dataSource;

    protected AbstractJdbcCrudService(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource не може бути null");
    }

    // ---------- «змінні кроки», які надає підклас ----------

    protected abstract String tableName();

    /** SELECT усіх колонок БЕЗ WHERE/ORDER BY, напр. "SELECT id, name FROM t". */
    protected abstract String selectSql();

    /** INSERT, де id — останній параметр. */
    protected abstract String insertSql();

    /** UPDATE ... WHERE id = ? — id останній параметр. */
    protected abstract String updateSql();

    /** Перетворення ПОТОЧНОГО рядка ResultSet на об'єкт (курсор не пересуває!). */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /** Прив'язка полів сутності до параметрів «?»: дані, а наприкінці — id. */
    protected abstract void setParameters(PreparedStatement ps, T entity) throws SQLException;

    /** ORDER BY за замовчуванням. Це константа підкласу, НЕ ввід користувача. */
    protected String defaultOrderBy() {
        return "name, id";
    }

    // ---------- каркас операцій ----------

    @Override
    public T findById(long id) throws EntityNotFoundException {
        String sql = selectSql() + " WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {          // курсор спочатку стоїть ПЕРЕД першим рядком
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("findById(" + id + ")", e);
        }
        throw new EntityNotFoundException(id); // рядка немає — сигналізуємо винятком, не null
    }

    /** Додає нову сутність або оновлює наявну за id (як Map.put в InMemoryCrudService). */
    @Override
    public void save(T entity) {
        Objects.requireNonNull(entity, "Сутність не може бути null");
        try (Connection c = dataSource.getConnection()) {
            save(c, entity);
        } catch (SQLException e) {
            throw new DataAccessException("save(" + entity.getId() + ")", e);
        }
    }

    /**
     * Те саме, але на ЧУЖОМУ з'єднанні — щоб кілька save() можна було виконати в одній транзакції
     * (з'єднання тоді відкриває й закриває той, хто керує транзакцією).
     */
    public void save(Connection c, T entity) throws SQLException {
        int updated;
        try (PreparedStatement ps = c.prepareStatement(updateSql())) {
            setParameters(ps, entity);
            updated = ps.executeUpdate();     // скільки рядків змінено
        }
        if (updated == 0) {                    // такого id ще немає — вставляємо
            try (PreparedStatement ps = c.prepareStatement(insertSql())) {
                setParameters(ps, entity);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM " + tableName() + " WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;     // true — рядок був і видалений
        } catch (SQLException e) {
            throw new DataAccessException("delete(" + id + ")", e);
        }
    }

    /**
     * Сортування «довільним Comparator» вимагає всіх записів у пам'яті, тому для великих таблиць
     * краще findAllSortedByName() (ORDER BY у БД) або findPage(...).
     */
    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator) {
        List<T> all = findAll();
        all.sort(comparator);
        return all;
    }

    /** ORDER BY виконує БД — переоприділяє default-метод інтерфейсу. */
    @Override
    public List<T> findAllSortedByName() {
        return findAllOrderedBy(defaultOrderBy());
    }

    /**
     * ОБЕРЕЖНО: без обмеження кількості рядків. На великій таблиці це «вичитує» її всю в пам'ять
     * (OutOfMemoryError, довгий запит, велике навантаження на мережу) — див. findPage.
     */
    public List<T> findAll() {
        return query(selectSql(), ps -> { });
    }

    /**
     * Сортування в БД. orderBy підставляється в текст запиту (параметром «?» назву колонки
     * передати не можна), тому сюди можна передавати ЛИШЕ константи з коду, ніколи ввід користувача.
     */
    protected List<T> findAllOrderedBy(String orderBy) {
        return query(selectSql() + " ORDER BY " + orderBy, ps -> { });
    }

    /** Сторінка записів (нумерація сторінок з 0), стабільно впорядкована за defaultOrderBy(). */
    public List<T> findPage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "page ≥ 0, size від 1 до " + MAX_PAGE_SIZE + " (отримано " + page + ", " + size + ")");
        }
        String sql = selectSql() + " ORDER BY " + defaultOrderBy() + " LIMIT ? OFFSET ?";
        return query(sql, ps -> {
            ps.setInt(1, size);
            ps.setLong(2, (long) page * size);
        });
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new DataAccessException("count()", e);
        }
    }

    /** Видаляє всі рядки таблиці (для демо/тестів). */
    public int deleteAll() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM " + tableName())) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("deleteAll()", e);
        }
    }

    // ---------- пакетна вставка та вставка по одному (для порівняння швидкості) ----------

    /** addBatch/executeBatch в одній транзакції. Лише INSERT: id у списку мають бути новими. */
    public void insertAllBatch(List<T> entities) {
        Tx.run(dataSource, c -> {
            try (PreparedStatement ps = c.prepareStatement(insertSql())) {
                int inBatch = 0;
                for (T entity : entities) {
                    setParameters(ps, entity);
                    ps.addBatch();                    // запам'ятовуємо, не відправляємо
                    if (++inBatch % BATCH_CHUNK == 0) {
                        ps.executeBatch();            // відправляємо порцію
                    }
                }
                ps.executeBatch();                    // залишок
            }
            return null;
        });
    }

    /** Кожен INSERT — окремий виклик executeUpdate() і окремий commit (autoCommit = true). */
    public void insertAllOneByOne(List<T> entities) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(insertSql())) {
            for (T entity : entities) {
                setParameters(ps, entity);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("insertAllOneByOne", e);
        }
    }

    /** Кожен INSERT — окремий executeUpdate(), але commit один наприкінці. */
    public void insertAllOneByOneInTransaction(List<T> entities) {
        Tx.run(dataSource, c -> {
            try (PreparedStatement ps = c.prepareStatement(insertSql())) {
                for (T entity : entities) {
                    setParameters(ps, entity);
                    ps.executeUpdate();
                }
            }
            return null;
        });
    }

    // ---------- спільні помічники ----------

    /** Виконує SELECT і мапить КОЖЕН рядок через mapRow — єдине місце циклу по ResultSet. */
    protected List<T> query(String sql, SqlConsumer binder) {
        List<T> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("SELECT", e);
        }
        return result;
    }
}
