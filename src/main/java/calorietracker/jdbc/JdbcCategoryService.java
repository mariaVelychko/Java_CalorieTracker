package calorietracker.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import calorietracker.model.FoodCategory;

public class JdbcCategoryService extends AbstractJdbcCrudService<FoodCategory> {

    public JdbcCategoryService(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String tableName() {
        return "food_category";
    }

    @Override
    protected String selectSql() {
        return "SELECT id, name, description FROM food_category";
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO food_category (name, description, id) VALUES (?, ?, ?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE food_category SET name = ?, description = ? WHERE id = ?";
    }

    @Override
    protected FoodCategory mapRow(ResultSet rs) throws SQLException {
        return new FoodCategory(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description")); 
    }

    @Override
    protected void setParameters(PreparedStatement ps, FoodCategory category) throws SQLException {
        ps.setString(1, category.getName());
        ps.setString(2, category.getDescription());
        ps.setLong(3, category.getId());
    }
}
