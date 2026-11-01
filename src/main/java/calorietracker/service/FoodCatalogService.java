package calorietracker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import calorietracker.exception.DataAccessException;
import calorietracker.exception.EntityNotFoundException;
import calorietracker.jdbc.JdbcCategoryService;
import calorietracker.jdbc.JdbcFruitProductService;
import calorietracker.model.FoodCategory;
import calorietracker.model.FruitProduct;

public class FoodCatalogService {
    private final DataSource dataSource;
    private final JdbcCategoryService categories;
    private final JdbcFruitProductService fruits;

    public FoodCatalogService(DataSource dataSource,
                              JdbcCategoryService categories,
                              JdbcFruitProductService fruits) {
        this.dataSource = dataSource;
        this.categories = categories;
        this.fruits = fruits;
    }

    public int createCategoryWithFruits(FoodCategory category, List<FruitProduct> items) {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                categories.save(c, category);
                for (FruitProduct item : items) {
                    fruits.save(c, item.withCategory(category.getId()));
                }
                c.commit();
                return items.size();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Транзакцію скасовано (rollback)", e);
        }
    }

    public int moveFruits(long fromCategoryId, long toCategoryId) throws EntityNotFoundException {
        categories.findById(fromCategoryId);
        categories.findById(toCategoryId);
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE fruit_product SET category_id = ? WHERE category_id = ?")) {
            ps.setLong(1, toCategoryId);
            ps.setLong(2, fromCategoryId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("moveFruits", e);
        }
    }
}
