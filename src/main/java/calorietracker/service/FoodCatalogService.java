package calorietracker.service;

import java.util.List;
import javax.sql.DataSource;
import calorietracker.exception.EntityNotFoundException;
import calorietracker.jdbc.JdbcCategoryService;
import calorietracker.jdbc.JdbcFruitProductService;
import calorietracker.jdbc.Tx;
import calorietracker.model.FoodCategory;
import calorietracker.model.FruitProduct;

/**
 * Операції, що змінюють КІЛЬКА таблиць, і тому виконуються в одній транзакції.
 */
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

    /**
     * Створює категорію разом з її фруктами: два INSERT у дві таблиці.
     * Усе або нічого (атомарність): якщо хоч один фрукт не пройшов (CHECK/FK у БД),
     * rollback скасовує й уже збережену категорію.
     *
     * @return кількість збережених фруктів
     */
    public int createCategoryWithFruits(FoodCategory category, List<FruitProduct> items) {
        return Tx.run(dataSource, c -> {
            categories.save(c, category);                    // таблиця 1
            for (FruitProduct item : items) {
                fruits.save(c, item.withCategory(category.getId())); // таблиця 2 (може кинути SQLException)
            }
            return items.size();
        });
    }

    /** Переносить усі фрукти з однієї категорії в іншу (зміна в fruit_product, перевірка обох категорій). */
    public int moveFruits(long fromCategoryId, long toCategoryId) throws EntityNotFoundException {
        categories.findById(fromCategoryId);
        categories.findById(toCategoryId);
        return Tx.run(dataSource, c -> {
            try (var ps = c.prepareStatement(
                    "UPDATE fruit_product SET category_id = ? WHERE category_id = ?")) {
                ps.setLong(1, toCategoryId);
                ps.setLong(2, fromCategoryId);
                return ps.executeUpdate();
            }
        });
    }
}
