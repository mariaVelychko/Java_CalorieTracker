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

public abstract class AbstractJdbcCrudService<T extends BaseEntity> implements CRUDService<T> {

    private static final int MAX_PAGE_SIZE = 1000;
    private static final int BATCH_CHUNK = 1000;

    protected final DataSource dataSource;

    protected AbstractJdbcCrudService(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource не може бути null");
    }


    protected abstract String tableName();

    protected abstract String selectSql();

    protected abstract String insertSql();

    protected abstract String updateSql();

    protected abstract T mapRow(ResultSet rs) throws SQLException;

    protected abstract void setParameters(PreparedStatement ps, T entity) throws SQLException;

    protected String defaultOrderBy() {
        return "name, id";
    }


    @Override
    public T findById(long id) throws EntityNotFoundException {
        String sql = selectSql() + " WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {          
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("findById(" + id + ")", e);
        }
        throw new EntityNotFoundException(id);
    }

    @Override
    public void save(T entity) {
        Objects.requireNonNull(entity, "Сутність не може бути null");
        try (Connection c = dataSource.getConnection()) {
            save(c, entity);
        } catch (SQLException e) {
            throw new DataAccessException("save(" + entity.getId() + ")", e);
        }
    }

    public void save(Connection c, T entity) throws SQLException {
        int updated;
        try (PreparedStatement ps = c.prepareStatement(updateSql())) {
            setParameters(ps, entity);
            updated = ps.executeUpdate();   
        }
        if (updated == 0) {                    
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
            return ps.executeUpdate() > 0;     
        } catch (SQLException e) {
            throw new DataAccessException("delete(" + id + ")", e);
        }
    }

    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator) {
        List<T> all = findAll();
        all.sort(comparator);
        return all;
    }

    @Override
    public List<T> findAllSortedByName() {
        return findAllOrderedBy(defaultOrderBy());
    }

    public List<T> findAll() {
        return query(selectSql(), ps -> { });
    }


    protected List<T> findAllOrderedBy(String orderBy) {
        return query(selectSql() + " ORDER BY " + orderBy, ps -> { });
    }

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

    public int deleteAll() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM " + tableName())) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("deleteAll()", e);
        }
    }

    public void insertAllBatch(List<T> entities) {
        Tx.run(dataSource, c -> {
            try (PreparedStatement ps = c.prepareStatement(insertSql())) {
                int inBatch = 0;
                for (T entity : entities) {
                    setParameters(ps, entity);
                    ps.addBatch();                    
                    if (++inBatch % BATCH_CHUNK == 0) {
                        ps.executeBatch();          
                    }
                }
                ps.executeBatch();                   
            }
            return null;
        });
    }

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
