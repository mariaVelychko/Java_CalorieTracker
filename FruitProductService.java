import java.util.List;

public interface FruitProductService extends CRUDService<FruitProduct> {
    List<FruitProduct> findAllSortedByNameAndBySugar();
}
