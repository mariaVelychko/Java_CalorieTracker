# ЛР10. Maven і JDBC: сховище в реляційній БД

Продовження ЛР1-9. Сховище в пам'яті з ЛР7 (`InMemoryCrudService`) замінено на сховище в БД
**без зміни інтерфейсів**: `JdbcFruitProductService` реалізує той самий `FruitProductService`,
тож клієнтський код (`JdbcDemo.demoSameClientCode`) працює з обома реалізаціями однаково.
Виконано рівні 1-3.

- **Сутності:** `FruitProduct` (фрукт) і нова `FoodCategory` (категорія). Одна категорія — багато фруктів.
- **СУБД:** H2 у файловому режимі (дані в `./data/calorie_tracker.mv.db`, переживають перезапуск).

## Схема БД

```
┌──────────────────────┐ 1         N ┌─────────────────────────────┐
│ food_category        │─────────────│ fruit_product               │
├──────────────────────┤             ├─────────────────────────────┤
│ id          BIGINT PK│◄────────────│ category_id  BIGINT  FK, NULL│
│ name        VARCHAR  │  ON DELETE  │ id           BIGINT  PK      │
│             NOT NULL │  RESTRICT   │ name         VARCHAR NOT NULL│
│             UNIQUE   │             │ weight_grams INT  CHECK 1..5000
│ description VARCHAR  │             │ calories_per_100g NUMERIC(6,2)
│             (NULL ok)│             │ protein_per_100g  NUMERIC(5,2)
└──────────────────────┘             │ meal_type    CHECK IN (4 типи)
                                     │ sugar_grams  NUMERIC CHECK >= 0
                                     │ fresh        BOOLEAN NOT NULL
                                     └─────────────────────────────┘
```

| Таблиця | Зв'язок | Правило |
| --- | --- | --- |
| `fruit_product.category_id` → `food_category.id` | багато фруктів → одна категорія | `ON DELETE RESTRICT`, колонка може бути `NULL` (фрукт без категорії) |

Індекси: `idx_fruit_category` (колонка FK — пошук фруктів категорії та перевірка FK при видаленні
категорії) і `idx_fruit_name` (сортування/пошук за назвою). Первинні ключі індексуються автоматично.

### Чому `ON DELETE RESTRICT`

Фрукти містять харчові дані, які користувач вносив вручну. `CASCADE` мовчки знищив би їх разом
із категорією (одна помилкова дія — втрачена купа записів), `SET NULL` залишив би «осиротілі»
фрукти без категорії, про що ніхто не помітив би. `RESTRICT` змушує спочатку свідомо перенести
або видалити фрукти (є `FoodCatalogService.moveFruits`), а вже потім видаляти категорію. Це видно в демо:
спроба видалити непорожню категорію закінчується помилкою БД.

## Структура пакетів

```
src/main/java/calorietracker/
├── app/        Main, JdbcDemo                       — точки входу
├── model/      BaseEntity, FoodItem, MealProduct, FruitProduct, ProteinProduct,
│               SupplementProduct, FoodCategory, FoodItemFactory, Product, MealLog …
├── exception/  DomainException, InvalidNutritionValueException, InvalidMealTypeException,
│               EntityNotFoundException, DataAccessException
├── advice/     Strategy / Factory / Adapter порад (MealAdviceStrategy, MealAdviceFactory, …)
├── observer/   MealLogListener, MealAddedEvent, ConsoleMealObserver, CalorieSummaryObserver
├── service/    CRUDService, InMemoryCrudService, FruitProductService, FoodCatalogService, EntityUtils
└── jdbc/       DBConnection, SimpleDataSource, DatabaseInitializer, Tx,
                AbstractJdbcCrudService, JdbcCategoryService, JdbcFruitProductService
src/main/resources/   init.sql, application.properties
src/test/java/calorietracker/jdbc/   JdbcServicesTest
```

Ієрархія `FoodItem` (sealed) лежить в одному пакеті `model` — Java вимагає цього для sealed-класів
без модулів.

## Запуск з нуля

Потрібні: **JDK 21** і **Maven 3.9+**. Окремо встановлювати СУБД не треба: H2 — це бібліотека,
яка сама створить файл БД.

```bash
git clone <твій репозиторій>
cd Java_CalorieTracker
git checkout lr10/maven-jdbc

mvn clean test          # збірка + 19 тестів на H2 in-memory
mvn compile exec:java   # повна програма (Main): спочатку інтерактивна частина, наприкінці — демо БД
mvn compile exec:java -Dexec.mainClass=calorietracker.app.JdbcDemo   # лише демо БД, без введення з клавіатури
```

Або через jar:

```bash
mvn package -DskipTests
java -cp "target/calorie-tracker-1.0-SNAPSHOT.jar:$HOME/.m2/repository/com/h2database/h2/2.3.232/h2-2.3.232.jar" calorietracker.app.JdbcDemo
```
(на Windows роздільник у `-cp` — `;`). Зручніше `mvn exec:java`, бо він сам збирає classpath.

Створювати БД вручну **не потрібно**: при першому зверненні до `DBConnection` виконується
`init.sql` (таблиці створюються, якщо їх немає). Повторний запуск безпечний.

> Якщо в консолі Windows кирилиця «крякозябрами», запускай з `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8`
> (у `mvn exec:java` — через змінну середовища `JAVA_TOOL_OPTIONS`).

### Налаштування підключення

`src/main/resources/application.properties` (URL, користувач, пароль не зашиті в код):

```properties
db.url=jdbc:h2:file:./data/calorie_tracker;DB_CLOSE_DELAY=-1
db.user=sa
db.password=
```

Змінні середовища `DB_URL`, `DB_USER`, `DB_PASSWORD` мають пріоритет — так пароль до реальної СУБД
не потрапляє в git. Каталог `data/` і `target/` додано в `.gitignore`.

**Перехід на PostgreSQL:** замінити залежність `h2` на `org.postgresql:postgresql` (scope `runtime`),
задати `DB_URL=jdbc:postgresql://localhost:5432/calorie`, `DB_USER`, `DB_PASSWORD`. `init.sql`
написаний у діалекті, спільному для H2 і PostgreSQL. Код сервісів не змінюється.

## Що де лежить

| Вимога | Місце в коді |
| --- | --- |
| Р1: Maven, GAV, Java 21, UTF-8 | `pom.xml` |
| Р1: драйвер як залежність (scope runtime) | `pom.xml`, `com.h2database:h2` |
| Р1: DDL з PK, NOT NULL, NUMERIC, UNIQUE/CHECK | `src/main/resources/init.sql` |
| Р1: Singleton `DBConnection`, параметри в properties | `DBConnection`, `application.properties` |
| Р1: JDBC-сервіс, PreparedStatement + try-with-resources, `EntityNotFoundException` | `AbstractJdbcCrudService`, `JdbcFruitProductService` |
| Р2: друга таблиця, FK, ON DELETE | `init.sql`, `FoodCategory`, `JdbcCategoryService` |
| Р2: `findAllSortedByNameAndBySugar()` через `ORDER BY` | `JdbcFruitProductService` |
| Р2: `mapRow` в одному місці | `JdbcFruitProductService.mapRow`, `JdbcCategoryService.mapRow` |
| Р2: `init.sql` автоматично й ідемпотентно | `DatabaseInitializer`, `IF NOT EXISTS` |
| Р2: транзакція з `commit`/`rollback` | `Tx`, `FoodCatalogService.createCategoryWithFruits` |
| Р2: SQL-ін'єкція | розділ нижче; «добрий» метод `findByName` |
| Р3: Template Method | `AbstractJdbcCrudService` (+ обидва JDBC-сервіси як підкласи) |
| Р3: Singleton + `DataSource` | `DBConnection`, `SimpleDataSource` |
| Р3: пакетна вставка + вимірювання | `insertAllBatch`, `JdbcDemo.demoBatch`, розділ нижче |
| Р3: пагінація | `findPage` |
| Тести на H2 in-memory | `src/test/java/JdbcServicesTest.java` |

## Template Method (`AbstractJdbcCrudService<T extends BaseEntity>`)

Каркас (однаковий для всіх сутностей) написаний один раз: відкрити з'єднання → `prepareStatement` →
прив'язати параметри → виконати → перетворити рядки → закрити ресурси → обгорнути `SQLException` у
`DataAccessException`. Підклас лише надає «змінні кроки»:

| Що надає підклас | Навіщо |
| --- | --- |
| `tableName()`, `selectSql()`, `insertSql()`, `updateSql()` | тексти SQL |
| `mapRow(ResultSet)` | рядок БД → об'єкт |
| `setParameters(PreparedStatement, T)` | об'єкт → параметри `?` (дані, а **id — останній**) |

Каркасні методи: `findById`, `save` (UPDATE, а якщо змінено 0 рядків — INSERT), `delete`, `query(sql, binder)`
(єдиний цикл по `ResultSet`), `findPage`, `insertAllBatch`. `JdbcCategoryService` (друга сутність)
складається лише з SQL і двох методів мапінгу.

`AbstractJdbcCrudService` реалізує `CRUDService<T>`, тому `findAllSorted(Comparator)` і
`findAllSortedByName()` лишаються в контракті; `findAllSortedByName()` перевизначено так, щоб
сортувала БД (`ORDER BY name, id`), а не Java.

### Сортування: ORDER BY у БД, а не Comparator

БД може використати індекс, не передає зайві рядки по мережі й не тримає всю таблицю в пам'яті
застосунку. Різниця стає помітною на великих таблицях. Без `ORDER BY` порядок рядків **не гарантований**
(залежить від плану виконання), тому доповнюємо ключем `id` для стабільності.
Назву колонки сортування **не можна** передати як `?` (параметр — це значення, а не ідентифікатор),
тому `findAllOrderedBy(String)` приймає лише константи з коду, ніколи ввід користувача; для вибору
з кількох полів потрібен білий список (`switch`/`enum`).

## Транзакція

`FoodCatalogService.createCategoryWithFruits` змінює дві таблиці (`food_category` + `fruit_product`).
`Tx.run` вмикає `setAutoCommit(false)`, виконує роботу на одному `Connection`, робить `commit()`, а при
будь-якому винятку — `rollback()` (поруч, у тому самому `catch`, де відомо, що щось пішло не так).
Без `rollback` половина змін могла б залишитись або «висіти» у відкритій транзакції.

Випадок відкату з демо: категорія «Тропічні» і фрукт «Манго» вже вставлені, другий фрукт порушує
`CHECK (sugar_grams >= 0)` → `rollback` → після нього ні категорії, ні «Манго» в БД немає
(`Після відкату: категорій: 1, фруктів: 2` — стільки ж, скільки було до).

## SQL-ін'єкція: поганий і хороший метод

«Поганий» метод **не лежить у коді** — лише тут:

```java
// ПОГАНО — не робити! Значення склеюється в текст запиту.
String sql = "SELECT id FROM fruit_product WHERE name = '" + name + "'";
try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) { ... }
```

«Хороший» (в коді: `JdbcFruitProductService.findByName`):

```java
try (PreparedStatement ps = c.prepareStatement("SELECT ... FROM fruit_product WHERE name = ?")) {
    ps.setString(1, name);
    ...
}
```

Реальний результат запуску на H2 (у таблиці 2 фрукти):

| Ввід `name` | Поганий метод | Хороший метод |
| --- | --- | --- |
| `Яблуко` | 1 рядок | 1 рядок |
| `' OR '1'='1` | **2 рядки — віддав усю таблицю** (SQL став `... WHERE name = '' OR '1'='1'`) | 0 рядків |
| `x'; DROP TABLE fruit_product; --` | запит виконався, **таблицю `fruit_product` видалено** (наступний запит: `Table "FRUIT_PRODUCT" not found`) | 0 рядків, таблиця на місці |

Чому `PreparedStatement` безпечний: текст запиту з `?` розбирається БД **до** підстановки значень, тому
значення завжди лишається даними і ніколи не стає частиною SQL-команди. Ручне екранування лапок
недостатнє: легко пропустити спецсимвол, а діалекти БД екранують по-різному. Тест
`JdbcServicesTest.sqlInjectionStringIsJustData` фіксує поведінку «доброго» методу.

## Singleton з одним спільним `Connection`: чому це проблема

Якщо `DBConnection` тримає **один** `Connection`:

- **Багатопотоковість:** `Connection` не призначений для одночасного використання з кількох потоків —
  запити перемішуються, `ResultSet` одного потоку закривається викликом іншого.
- **Транзакції різних клієнтів:** `setAutoCommit(false)` одного користувача діє на спільне з'єднання,
  тож `commit()`/`rollback()` захоплюють зміни **чужих** запитів.
- **Розрив зв'язку:** після обриву (таймаут, перезапуск БД) єдине з'єднання «мертве» для всіх, і
  відновлювати його треба вручну.
- **try-with-resources:** закриття з'єднання одним викликом ламає всіх інших.

**Обраний варіант (b):** Singleton тримає не `Connection`, а `DataSource` (`SimpleDataSource`: незмінні
`url/user/password`, кожен `getConnection()` повертає нове з'єднання). Він потокобезпечний, бо не має
змінного стану, а кожен потік працює зі своїм з'єднанням і закриває його сам. Перевірка: у
`JdbcDemo.demoSingleton` 8 потоків одночасно беруть `getInstance()` та роблять запити — екземпляр один,
помилок немає. Сам `getInstance()` реалізований через *initialization-on-demand holder*: ліниво й
потокобезпечно (гарантує завантажувач класів JVM).

Щоб отримати справжній пул (HikariCP), достатньо замінити реалізацію `DataSource` у конструкторі
`DBConnection` — сервіси приймають інтерфейс `DataSource`, і більше нічого змінювати не треба.
Недоліки самого Singleton (прихована залежність, глобальний стан) пом'якшено тим, що сервіси отримують
`DataSource` через конструктор, а тести створюють власний `DataSource` і не чіпають `DBConnection`.

## Пакетна вставка: вимірювання

10 000 фруктів у файловий H2 (`JdbcDemo.demoBatch`, два запуски поспіль, мс):

| Спосіб | Запуск 1 | Запуск 2 |
| --- | --- | --- |
| 1) по одному, `autoCommit = true` (кожен INSERT — окремий commit) | 697 | 840 |
| 2) по одному, одна транзакція | 216 | 180 |
| 3) `addBatch`/`executeBatch` + одна транзакція | 168 | 195 |

Висновки (чесно): найбільше виграє **одна транзакція замість commit на кожен рядок** (у 3-5 разів). Між
способами 2 і 3 на вбудованій H2 різниця в межах похибки: немає мережі, тому економити нема на чому.
Основна перевага `executeBatch` проявляється на серверній СУБД (PostgreSQL/MySQL), де кожен окремий
`executeUpdate` — це мережева затримка (round trip), а пакет відправляється за один раз. Результати
залежать від комп'ютера й змінюються від запуску до запуску; свої цифри бери зі свого запуску.

## Пагінація

`findPage(page, size)`: `SELECT ... ORDER BY name, id LIMIT ? OFFSET ?` (сторінки з 0, `size` від 1 до 1000).
`findAll()` без обмежень небезпечний на великій таблиці: усі рядки потрапляють у пам'ять JVM
(`OutOfMemoryError`), запит довго виконується і навантажує мережу та БД. Обмеження `LIMIT/OFFSET`: чим
більший `OFFSET`, тим повільніше (БД усе одно має пройти пропущені рядки), а якщо дані змінюються між
запитами, рядки можуть зсунутись (дублі/пропуски між сторінками). `ORDER BY` обов'язковий, інакше
сторінки не мають гарантованого порядку. Для дуже великих таблиць краще keyset-пагінація
(`WHERE id > ? ORDER BY id LIMIT ?`).

## Тести

`JdbcServicesTest` (JUnit 4, H2 in-memory, кожен тест — окрема порожня БД, схема з того самого `init.sql`):
CRUD, `EntityNotFoundException`, сортування, NULL (`wasNull`), FK/CHECK/UNIQUE/RESTRICT, ідемпотентність
`init.sql`, commit і rollback, пагінація, batch, SQL-ін'єкція. Тести на in-memory H2 швидкі й
ізольовані, але діалект H2 не ідентичний PostgreSQL/MySQL (типи, `LIMIT`, автоінкремент, регістр
ідентифікаторів, `IF NOT EXISTS` для індексів тощо), тому перед релізом варто прогнати їх і на цільовій СУБД.

## Зміни в існуючому коді (ЛР10)

- `FoodItem`: додано `getCaloriesPer100g()`, `getProteinPer100g()` (потрібні для запису в БД) і прапорець
  `traceConstructors`, щоб службові повідомлення з конструкторів ЛР6 не засмічували вивід при читанні рядків із БД.
- `FruitProduct`: зберігає `fresh` (раніше параметр ігнорувався) і новий необов'язковий `categoryId`; додано `withCategory(...)`.
- `Main`: наприкінці викликає `JdbcDemo.runAll()`.
- Усі класи розкладено по пакетах `calorietracker.*` (було ~50 файлів в одній теці).
- Структура проєкту перенесена в `src/main/java/calorietracker/...`, `src/main/resources`, `src/test/java`.

---

# Попередня робота: ЛР7-9. Типізований CRUD-сервіс

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
