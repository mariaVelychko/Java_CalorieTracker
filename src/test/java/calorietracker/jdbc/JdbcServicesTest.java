package calorietracker.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import calorietracker.app.JdbcDemo;
import calorietracker.exception.DataAccessException;
import calorietracker.exception.EntityNotFoundException;
import calorietracker.model.FoodCategory;
import calorietracker.model.FoodItem;
import calorietracker.model.FruitProduct;
import calorietracker.service.FoodCatalogService;
import calorietracker.service.FruitProductService;
import calorietracker.service.InMemoryFruitProductService;

public class JdbcServicesTest {

    private JdbcCategoryService categories;
    private JdbcFruitProductService fruits;
    private FoodCatalogService catalog;

    @BeforeClass
    public static void silenceConstructors() {
        FoodItem.traceConstructors = false;
    }

    @Before
    public void setUp() {
        DataSource ds = new SimpleDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseInitializer.run(ds);
        categories = new JdbcCategoryService(ds);
        fruits = new JdbcFruitProductService(ds);
        catalog = new FoodCatalogService(ds, categories, fruits);
    }

    @After
    public void tearDown() {
        fruits.deleteAll();
    }

    private static FruitProduct fruit(long id, String name, double sugar) {
        return new FruitProduct(id, name, 100, 50, 1, "перекус", sugar);
    }

    @Test
    public void saveInsertsThenUpdatesById() throws Exception {
        fruits.save(fruit(1, "Яблуко", 10));
        fruits.save(fruit(1, "Яблуко", 14)); // той самий id → оновлення, а не новий рядок
        assertEquals(1, fruits.count());
        assertEquals(14.0, fruits.findById(1).getSugarGrams(), 0.0001);
    }

    @Test
    public void findByIdThrowsWhenMissing() {
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class, () -> fruits.findById(42));
        assertEquals(42, e.getId());
    }

    @Test
    public void deleteReturnsTrueOnlyWhenRowExisted() {
        fruits.save(fruit(1, "Яблуко", 10));
        assertTrue(fruits.delete(1));
        assertFalse(fruits.delete(1));
    }

    @Test
    public void sortedByNameThenSugarUsesOrderBy() {
        fruits.save(fruit(1, "Яблуко", 14));
        fruits.save(fruit(2, "Банан", 12));
        fruits.save(fruit(3, "Яблуко", 9));
        List<FruitProduct> sorted = fruits.findAllSortedByNameAndBySugar();
        assertEquals(List.of(2L, 3L, 1L), sorted.stream().map(FruitProduct::getId).toList());
    }

    @Test
    public void nullCategoryRoundTripsAsNull_notZero() throws Exception {
        fruits.save(fruit(1, "Яблуко", 10));
        assertNull(fruits.findById(1).getCategoryId());
    }

    @Test
    public void nullDescriptionRoundTrips() throws Exception {
        categories.save(new FoodCategory(1, "Ягоди"));
        assertNull(categories.findById(1).getDescription());
    }

    @Test
    public void foreignKeyRejectsUnknownCategory() {
        FruitProduct orphan = new FruitProduct(1, "Привид", 100, 10, 1, "перекус", 1, true, 999L);
        assertThrows(DataAccessException.class, () -> fruits.save(orphan));
    }

    @Test
    public void onDeleteRestrictBlocksDeletingNonEmptyCategory() {
        categories.save(new FoodCategory(1, "Цитрусові"));
        fruits.save(new FruitProduct(1, "Апельсин", 100, 47, 1, "перекус", 9, true, 1L));
        assertThrows(DataAccessException.class, () -> categories.delete(1));
        fruits.delete(1);
        assertTrue(categories.delete(1));
    }

    @Test
    public void checkConstraintRejectsNegativeSugar() {
        assertThrows(DataAccessException.class, () -> fruits.save(fruit(1, "Кислий", -1)));
    }

    @Test
    public void uniqueCategoryName() {
        categories.save(new FoodCategory(1, "Ягоди"));
        assertThrows(DataAccessException.class, () -> categories.save(new FoodCategory(2, "Ягоди")));
    }

    @Test
    public void initScriptIsIdempotent() {
        categories.save(new FoodCategory(1, "Ягоди"));
        DataSource same = new SimpleDataSource("jdbc:h2:mem:idem;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseInitializer.run(same);
        DatabaseInitializer.run(same); // повторний запуск не падає
        new JdbcCategoryService(same).save(new FoodCategory(1, "Ягоди"));
        DatabaseInitializer.run(same); // і не стирає дані
        assertEquals(1, new JdbcCategoryService(same).count());
    }

    @Test
    public void transactionCommitsBothTables() throws Exception {
        int saved = catalog.createCategoryWithFruits(new FoodCategory(1, "Ягоди"),
                List.of(fruit(1, "Малина", 4), fruit(2, "Чорниця", 10)));
        assertEquals(2, saved);
        assertEquals(1, categories.count());
        assertEquals(2, fruits.count());
        assertEquals(Long.valueOf(1), fruits.findById(1).getCategoryId());
    }

    @Test
    public void transactionRollsBackEverythingOnFailure() {
        assertThrows(DataAccessException.class, () -> catalog.createCategoryWithFruits(
                new FoodCategory(1, "Тропічні"),
                List.of(fruit(1, "Манго", 14), fruit(2, "Зіпсований", -1))));
        assertEquals(0, categories.count()); // категорія теж відкотилась
        assertEquals(0, fruits.count());     // і перший фрукт теж
    }

    @Test
    public void moveFruitsChangesCategory() throws Exception {
        categories.save(new FoodCategory(1, "A"));
        categories.save(new FoodCategory(2, "B"));
        fruits.save(new FruitProduct(1, "Х", 100, 10, 1, "перекус", 1, true, 1L));
        assertEquals(1, catalog.moveFruits(1, 2));
        assertEquals(1, fruits.findByCategory(2).size());
        assertThrows(EntityNotFoundException.class, () -> catalog.moveFruits(1, 99));
    }

    @Test
    public void sqlInjectionStringIsJustData() {
        fruits.save(fruit(1, "Яблуко", 10));
        assertEquals(0, fruits.findByName("' OR '1'='1").size());
        assertEquals(0, fruits.findByName("x'; DROP TABLE fruit_product; --").size());
        assertEquals(1, fruits.count()); // таблиця на місці
        assertEquals(1, fruits.findByName("Яблуко").size());
    }

    @Test
    public void paginationReturnsStableNonOverlappingPages() {
        fruits.insertAllBatch(JdbcDemo.generateFruits(1, 7));
        assertEquals(3, fruits.findPage(0, 3).size());
        assertEquals(3, fruits.findPage(1, 3).size());
        assertEquals(1, fruits.findPage(2, 3).size());
        assertEquals(0, fruits.findPage(3, 3).size());
        assertEquals(4L, fruits.findPage(1, 3).get(0).getId());
        assertThrows(IllegalArgumentException.class, () -> fruits.findPage(0, 0));
    }

    @Test
    public void batchInsertStoresAllRows() {
        fruits.insertAllBatch(JdbcDemo.generateFruits(1, 2500)); // більше за BATCH_CHUNK=1000
        assertEquals(2500, fruits.count());
    }

    @Test
    public void saveNullIsRejected() {
        assertThrows(NullPointerException.class, () -> fruits.save(null));
    }

    @Test
    public void sameClientCodeWorksForBothImplementations() {
        for (FruitProductService service : List.<FruitProductService>of(new InMemoryFruitProductService(), fruits)) {
            service.save(fruit(1, "Яблуко", 14));
            service.save(fruit(2, "Банан", 12));
            assertNotNull(service.findAllSortedByName());
            assertEquals("Банан", service.findAllSortedByName().get(0).getName());
            service.delete(1);
            service.delete(2);
        }
    }
}
