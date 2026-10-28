package calorietracker.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import javax.sql.DataSource;
import calorietracker.model.FruitProduct;
import calorietracker.service.FruitProductService;

public class JdbcFruitProductService extends AbstractJdbcCrudService<FruitProduct>
        implements FruitProductService {

    private static final String COLUMNS =
            "id, name, weight_grams, calories_per_100g, protein_per_100g, meal_type, "
                    + "sugar_grams, fresh, category_id";

    public JdbcFruitProductService(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String tableName() {
        return "fruit_product";
    }

    @Override
    protected String selectSql() {
        return "SELECT " + COLUMNS + " FROM fruit_product";
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO fruit_product (name, weight_grams, calories_per_100g, protein_per_100g, "
                + "meal_type, sugar_grams, fresh, category_id, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE fruit_product SET name = ?, weight_grams = ?, calories_per_100g = ?, "
                + "protein_per_100g = ?, meal_type = ?, sugar_grams = ?, fresh = ?, category_id = ? "
                + "WHERE id = ?";
    }

    @Override
    protected FruitProduct mapRow(ResultSet rs) throws SQLException {
        long categoryId = rs.getLong("category_id");
        Long category = rs.wasNull() ? null : categoryId;
        return new FruitProduct(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getInt("weight_grams"),
                rs.getDouble("calories_per_100g"),
                rs.getDouble("protein_per_100g"),
                rs.getString("meal_type"),
                rs.getDouble("sugar_grams"),
                rs.getBoolean("fresh"),
                category);
    }

    @Override
    protected void setParameters(PreparedStatement ps, FruitProduct f) throws SQLException {
        ps.setString(1, f.getName());
        ps.setInt(2, f.getWeightGrams());
        ps.setDouble(3, f.getCaloriesPer100g());
        ps.setDouble(4, f.getProteinPer100g());
        ps.setString(5, f.getMealType());
        ps.setDouble(6, f.getSugarGrams());
        ps.setBoolean(7, f.isFresh());
        if (f.getCategoryId() == null) {
            ps.setNull(8, Types.BIGINT);     
        } else {
            ps.setLong(8, f.getCategoryId());
        }
        ps.setLong(9, f.getId());
    }

    @Override
    public List<FruitProduct> findAllSortedByNameAndBySugar() {
        return findAllOrderedBy("name, sugar_grams, id");
    }

    public List<FruitProduct> findByName(String name) {
        return query(selectSql() + " WHERE name = ? ORDER BY id", ps -> ps.setString(1, name));
    }

    public List<FruitProduct> findByCategory(long categoryId) {
        return query(selectSql() + " WHERE category_id = ? ORDER BY name, id",
                ps -> ps.setLong(1, categoryId));
    }
}
