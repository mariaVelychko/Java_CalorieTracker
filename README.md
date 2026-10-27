# ЛР7-9. Типізований CRUD-сервіс: узагальнення, колекції, сортування

Продовження ЛР1-6 (валідація, винятки, Strategy/Factory/Observer/Adapter, ієрархія
`FoodItem`). Сутності ЛР6 тепер успадковують спільний `BaseEntity` і керуються через
узагальнений CRUD-сервіс.

## Сутності

- **`BaseEntity`** — новий спільний базовий клас: `long id` (незмінний, `final`,
  задається лише в конструкторі), `String name`, геттери, `equals`/`hashCode` за `id`.
- **`FoodItem`** (ЛР6) тепер `extends BaseEntity`: поле `name` і його перевірка
  перенесені в `BaseEntity`, `FoodItem` додає свої поля (вага, калорії, білок, прийом їжі).
- Конкретні сутності для CRUD-демонстрації: **`FruitProduct`** і **`ProteinProduct`**
  (обидва — нащадки `FoodItem` з ЛР6, тепер з `id`).

## Каркас контракту (обов'язкові сигнатури)

```java
public interface CRUDService<T extends BaseEntity> {
    T findById(long id);               // кидає EntityNotFoundException, якщо сутність не знайдено
    void save(T entity);               // додає нову сутність або оновлює наявну (за id)
    boolean delete(long id);           // true, якщо сутність видалено; false, якщо її не було
    List<T> findAllSortedByName();     // відсортований за іменем
}
```

**Навіщо `T extends BaseEntity`:** без цього обмеження всередині сервісу `T` мав би лише
методи `Object`, і виклик `entity.getId()` у `save`/`findAllSorted` не скомпілювався б —
довелося б або приводити тип (`((BaseEntity) entity).getId()`, небезпечно й негарно), або
взагалі не мати змоги звернутись до id.

## Рівень 1

- **`EntityNotFoundException`** — **перевірений** (`extends Exception`), за тим самим
  принципом, що й `DomainException` з ЛР3: відсутність сутності за `id` — очікувана й
  відновлювана ситуація (можна показати повідомлення й спробувати інший id), тому
  компілятор має змусити викликача явно це обробити. Повернення `null` із `findById`
  натомість змусило б **кожен** виклик пам'ятати про перевірку на `null`, а забута
  перевірка дала б `NullPointerException` десь далі від справжньої причини помилки.
- **`InMemoryCrudService<T extends BaseEntity>`** — єдина реалізація, яку
  використано **без жодних змін** для двох різних сутностей: `FruitProduct` і
  `ProteinProduct` (`Main.demoCrudService`).
- Продемонстровано: додавання, оновлення наявної сутності (кількість записів не
  зростає), успішний і неуспішний `delete`, пошук неіснуючого `id` з `catch`.

## Рівень 2

### Вибір колекції для сховища

| Операція | ArrayList | HashMap | **LinkedHashMap (обрано)** | TreeMap |
| --- | --- | --- | --- | --- |
| `findById` (пошук за id) | O(n) — лінійний перебір | O(1) у середньому | O(1) у середньому | O(log n) |
| `save` (додати/оновити) | O(n) (треба знайти наявний запис) | O(1) у середньому | O(1) у середньому | O(log n) |
| `delete` | O(n) | O(1) у середньому | O(1) у середньому | O(log n) |
| Відсортований список | потрібне окреме сортування | потрібне окреме сортування | потрібне окреме сортування | уже відсортований за ключем |
| Порядок обходу | порядок вставки | **не гарантований** | **порядок вставки** | за ключем (id) |

**Обрано `LinkedHashMap<Long, T>`:** `findById`/`save`/`delete` працюють за `O(1)`
у середньому, як і звичайний `HashMap` (на відміну від `ArrayList`, де довелося б
перебирати список). На відміну від `HashMap`, `LinkedHashMap` **зберігає порядок
додавання** — це дає передбачуваний вивід у `findAll()` "до сортування" і не залежить
від хешів. `TreeMap` дав би впорядкованість за ключем (`id`) "безкоштовно", але нам
потрібне сортування **за іменем**, а не за `id`, тож ця впорядкованість не допомогла б,
а лише додала б `O(log n)` на кожну операцію замість `O(1)`.

### `equals`/`hashCode` і узгодженість з сортуванням (`BaseEntity`, п. 10)

`BaseEntity.equals`/`hashCode` перевизначено **за `id`**: дві сутності з однаковим `id`
вважаються "однією й тією самою", незалежно від решти полів.

**Важливий нюанс:** `FoodItem` (успадкований з ЛР6) **навмисно залишено** зі своєю
старою структурною реалізацією `equals`/`hashCode` (порівняння за значеннями всіх
полів, без `id`) — це видно в `Main.demoFoodItemHierarchy()`: два `FruitProduct` з
**різними** `id` (10 і 11), але однаковими іншими полями, визнаються `equals`. Це
**навмисно НЕ узгоджено** з ідентичністю `BaseEntity` за `id`.

Компаратор для сортування (`Comparator.comparing(BaseEntity::getName)`) теж **не
узгоджений** з `equals` жодного з двох рівнів: дві сутності з однаковим **іменем**, але
різними `id` і різними іншими полями (наш `FruitProduct` "Яблуко" id=1 і "Яблуко" id=3)
дають `compare(...) == 0`, хоча вони **не** `equals()` одна одній на жодному з рівнів.
Це свідомий і типовий випадок ("сортування за іменем" ≠ "рівність за id"), і саме тому
**`TreeSet<T>` з компаратором за іменем не годиться як основне сховище**: `TreeSet`
вважає елементи однаковими, якщо компаратор повертає `0`, тому другий `FruitProduct`
з іменем "Яблуко" просто **не додався б** у `TreeSet` — множина тихо втратила б запис.

## Рівень 3

### `findAllSorted` і `findAllSortedByName` як `default`-метод

```java
List<T> findAllSorted(Comparator<? super T> comparator);

default List<T> findAllSortedByName() {
    return findAllSorted(Comparator.comparing(BaseEntity::getName));
}
```

**Чому `Comparator<? super T>`, а не `Comparator<T>`:** `Comparator.comparing(BaseEntity::getName)`
має тип `Comparator<BaseEntity>` — компаратор, написаний для **супертипу** `T`, а не рівно
для `T`. Якби параметр мав тип `Comparator<T>`, передати `Comparator<BaseEntity>` у
`findAllSorted` для `T = FruitProduct` було б неможливо (дженерики інваріантні: `Comparator<BaseEntity>`
— не те саме, що `Comparator<FruitProduct>`). `? super T` каже: "підійде компаратор для `T`
або будь-якого його **батьківського** типу" — саме цього і потребує код вище.

### `printAll` і wildcard (`EntityUtils`, п. 12)

```java
public static void printAll(Collection<? extends BaseEntity> items) { ... }
```

`List<FruitProduct>` **не можна** передати туди, де очікується `List<BaseEntity>`: дженерики
в Java **не коваріантні** — `List<FruitProduct>` НЕ є підтипом `List<BaseEntity>`, хоча сам
`FruitProduct` є підтипом `BaseEntity`. Якби це дозволили без wildcard, можна було б
записати в такий список `new SupplementProduct(...)` (теж `BaseEntity`, але не `FruitProduct`)
— і зламати типову безпеку вихідного `List<FruitProduct>`.

`Collection<? extends BaseEntity>` каже: "список **якогось конкретного**, але невідомого
нам підтипу `BaseEntity`" — туди можна передати і `List<FruitProduct>`, і `List<ProteinProduct>`,
бо `printAll` лише **читає** елементи (викликає `toString()`/геттери), нічого не додає.
Саме тому `EntityUtils.printAll(fruitService.findAllSortedByName())` компілюється.

## Entity-специфічний інтерфейс: чому не в загальному `CRUDService<T>`

```java
public interface FruitProductService extends CRUDService<FruitProduct> {
    List<FruitProduct> findAllSortedByNameAndBySugar();
}
```

Метод `findAllSortedByNameAndBySugar()` залежить від поля `sugarGrams`, яке є **лише** в
`FruitProduct` — не в кожного `T` є "вміст цукру" (наприклад, у `ProteinProduct` його
немає). Якби цей метод додали в загальний `CRUDService<T>`, усі сутності (включно з
`ProteinProduct`) були б змушені його реалізувати, хоча для них він не має сенсу. Окремий
інтерфейс `FruitProductService extends CRUDService<FruitProduct>` додає цей метод **лише**
там, де він справді застосовний.

**Навіщо саме інтерфейс, а не тільки клас:** код, що користується сервісом, залежить від
**інтерфейсу** `FruitProductService`/`CRUDService<T>`, а не від `InMemoryFruitProductService`
напряму. Якби сховище згодом перенесли в базу даних (JDBC/Hibernate), достатньо було б
написати нову реалізацію (наприклад, `JdbcFruitProductService implements FruitProductService`)
— решта коду, що працює через інтерфейс, не змінилася б узагалі.

### Другорядне сортування: `Comparable` чи `Comparator`?

Обрано **`Comparator`** для **обох** рівнів сортування (і `findAllSortedByName`, і
`findAllSortedByNameAndBySugar`), а не `Comparable`:

- `Comparable` задає лише **один** "природний" порядок на весь клас. Нашим сутностям
  потрібно принаймні два: за іменем і за іменем+цукром — `Comparable` сам по собі
  цього не дає.
- `Comparator` дозволяє **композицію** через `thenComparing(...)` без написання окремого
  класу чи зміни самої сутності, і саме такий компаратор приймає `findAllSorted(...)`.

```java
Comparator<FruitProduct> byNameThenSugar = Comparator
        .<FruitProduct, String>comparing(BaseEntity::getName)
        .thenComparing(FruitProduct::getSugarGrams);
```

## Де що реалізовано

| Вимога | Місце в коді |
| --- | --- |
| Рівень 1: `BaseEntity` (id, name, геттери) | `BaseEntity` |
| Рівень 1: `EntityNotFoundException` | `EntityNotFoundException` |
| Рівень 1: `CRUDService<T extends BaseEntity>` | `CRUDService` |
| Рівень 1: `InMemoryCrudService<T>`, одна реалізація для двох сутностей | `InMemoryCrudService`, `Main.demoCrudService` |
| Рівень 2: `findAllSortedByName()` через `Comparator` | `CRUDService.findAllSortedByName` (default) |
| Рівень 2: entity-специфічний інтерфейс + метод з другим полем | `FruitProductService`, `InMemoryFruitProductService` |
| Рівень 2: сортування повертає нову колекцію, сховище не змінюється | `InMemoryCrudService.findAllSorted` (`new ArrayList<>(...)`) |
| Рівень 2: таблиця колекцій | розділ вище |
| Рівень 2: `equals`/`hashCode` за id в `BaseEntity` | `BaseEntity` |
| Рівень 3: `findAllSorted(Comparator<? super T>)` + default `findAllSortedByName` | `CRUDService` |
| Рівень 3: `printAll(Collection<? extends BaseEntity>)` | `EntityUtils.printAll` |

(Опис ЛР1-6 — валідація, винятки, Strategy/Factory/Observer/Adapter, `FoodItem` — без
змін по суті, див. історію комітів і попередні README в git log.)

## Як запустити

```bash
javac -encoding UTF-8 *.java
java Main
```
