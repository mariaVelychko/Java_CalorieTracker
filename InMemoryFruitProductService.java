import java.util.Comparator;
import java.util.List;

public class InMemoryFruitProductService
        extends InMemoryCrudService<FruitProduct>
        implements FruitProductService {

    @Override
    public List<FruitProduct> findAllSortedByNameAndBySugar() {
        Comparator<FruitProduct> byNameThenSugar = Comparator
                .<FruitProduct, String>comparing(BaseEntity::getName)
                .thenComparing(FruitProduct::getSugarGrams);
        return findAllSorted(byNameThenSugar);
    }
}
