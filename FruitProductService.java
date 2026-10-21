import java.util.List;

/**
 * Інтерфейс конкретної сутності. Метод findAllSortedByNameAndBySugar() навмисно
 * НЕ в загальному CRUDService<T> — не кожен T має "вміст цукру". Розширюючи
 * CRUDService<FruitProduct>, цей інтерфейс додає лише те, що справді властиве
 * фруктам, не змушуючи всі інші сутності (наприклад, ProteinProduct) мати
 * метод, який для них не має сенсу.
 */
public interface FruitProductService extends CRUDService<FruitProduct> {
    List<FruitProduct> findAllSortedByNameAndBySugar();
}
