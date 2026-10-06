# ЛР4. Інтерфейси, абстрактний клас, Strategy — облік спожитих калорій

Продовження ЛР1–ЛР3 (`Main`, `Product`, `MealLog`, власні винятки). Додано варіативну поведінку
«оцінка/порада по прийому їжі» через інтерфейс-стратегію, абстрактний клас зі спільною логікою
та патерн Strategy.

## Варіативна поведінка

У предметній області порада по прийому їжі може будуватись по-різному: за калоріями, за білком
або комбіновано. Це винесено в інтерфейс `MealAdviceStrategy`.

```
MealAdviceStrategy (інтерфейс)
├── default shortLabel()
└── evaluate(Product) — абстрактний метод
        │
        ▼
AbstractAdviceStrategy (абстрактний клас, спільний метод format())
├── CalorieAdviceStrategy   — оцінка за калоріями
└── ProteinAdviceStrategy   — оцінка за білком, перевизначає shortLabel()

BalancedAdviceStrategy — реалізує MealAdviceStrategy напряму ("чистий" інтерфейс),
                          бо лише делегує двом іншим стратегіям і не потребує format()
```

**Чому десь абстрактний клас, а десь "чистий" інтерфейс:** `CalorieAdviceStrategy` і
`ProteinAdviceStrategy` мають спільне форматування тексту — воно винесено в
`AbstractAdviceStrategy.format()`, щоб не дублювати. `BalancedAdviceStrategy` нічого спільного
з ними не форматує, а просто викликає інші стратегії, тому їй абстрактний клас не потрібен.

## Strategy (контекстний клас)

`MealAdviser` приймає стратегію в конструкторі та дозволяє замінити її через `setStrategy(...)`
**під час виконання програми**, без створення нового об'єкта (`Main.demoStrategyRuntimeSwitch`).

## Кілька інтерфейсів в одному класі

`Product` реалізує одразу два незалежні інтерфейси:
- `Comparable<Product>` — сортування порцій за калорійністю;
- `Reportable` — власний формат рядка звіту (`toReportLine()`).

Це окремі, незалежні одна від одної поведінки (сортування і формат виводу).

## Де що реалізовано

| Вимога | Місце в коді |
| --- | --- |
| Рівень 1: інтерфейс з методом | `MealAdviceStrategy.evaluate(...)` |
| Рівень 1: щонайменше 2 реалізації | `CalorieAdviceStrategy`, `ProteinAdviceStrategy`, `BalancedAdviceStrategy` |
| Рівень 1: поліморфний масив | `Main.demoPolymorphicStrategies` |
| Рівень 2: default-метод | `MealAdviceStrategy.shortLabel()`, перевизначено в `ProteinAdviceStrategy` |
| Рівень 2: абстрактний клас + інтерфейс | `AbstractAdviceStrategy` |
| Рівень 2: кілька інтерфейсів в одному класі | `Product implements Comparable<Product>, Reportable` |
| Рівень 3: Strategy зі зміною під час виконання | `MealAdviser`, `Main.demoStrategyRuntimeSwitch` |

(Повний опис ЛР1–ЛР3 — валідація `Product`, ієрархія винятків — без змін, див. історію комітів.)

## Як запустити

```bash
javac -encoding UTF-8 *.java
java Main
```
