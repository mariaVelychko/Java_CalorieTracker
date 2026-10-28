package calorietracker.service;

import java.util.List;
import calorietracker.model.FruitProduct;

public interface FruitProductService extends CRUDService<FruitProduct> {
    List<FruitProduct> findAllSortedByNameAndBySugar();
}
