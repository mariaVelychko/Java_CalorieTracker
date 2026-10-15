# ЛР5. Рефакторинг із патернами Factory, Observer, Adapter — облік спожитих калорій

Продовження ЛР1–ЛР4 (`Main`, `Product`, `MealLog`, власні винятки, Strategy). Проведено рефакторинг наявного коду: централізовано створення стратегій, додано сповіщення про додавання порції та інтеграцію із зовнішнім несумісним сервісом.

## Factory Method

У ЛР4 конкретні стратегії створювалися напряму через `new` у кількох місцях `Main`, а `BalancedAdviceStrategy` сам створювала свої залежності. Це ускладнювало підтримку: щоб додати нову стратегію, потрібно було змінювати кілька файлів.

Тепер створення об’єктів винесено в `MealAdviceFactory`. Фабрика приймає `AdviceType` або рядковий код і повертає готовий об’єкт типу `MealAdviceStrategy`.

```
Main / MealAdviser
       │
       ▼
MealAdviceFactory.create(AdviceType)
       │
       ├── CalorieAdviceStrategy
       ├── ProteinAdviceStrategy
       ├── BalancedAdviceStrategy
       └── LegacyAdviceAdapter
```

**До:**

```java
MealAdviser adviser = new MealAdviser(new CalorieAdviceStrategy());
adviser.setStrategy(new ProteinAdviceStrategy());
```

**Після:**

```java
MealAdviser adviser = new MealAdviser(MealAdviceFactory.create(AdviceType.CALORIE));
adviser.setStrategy(MealAdviceFactory.create(AdviceType.PROTEIN));
```

Клієнтський код більше не залежить від конкретних конструкторів стратегій. Щоб додати новий варіант, достатньо створити клас, додати значення в `AdviceType` та гілку у фабриці; `Main` і `MealAdviser` змінювати не потрібно.

У проєкті використано **Simple Factory** — статичний метод із параметром. Це відповідає умові ЛР5 про окремий Factory-клас або Factory-метод. Класичний GoF Factory Method із Creator та перевизначенням методу у підкласах не вводився, щоб не ускладнювати невеликий проєкт.

## Observer

У предметній області додавання порції — це зміна стану щоденника. Після успішного додавання запису інші частини програми можуть реагувати: вести журнал, перераховувати підсумок тощо.

Реалізовано патерн Observer:

```
MealLog (Subject)
├── зберігає список MealLogListener
├── addListener(...) / removeListener(...)
└── після успішного addMeal(...) викликає onMealAdded(...)

MealLogListener (Observer)
└── onMealAdded(MealAddedEvent event)
        │
        ├── ConsoleMealObserver      — журнал доданих порцій
        └── CalorieSummaryObserver   — кількість порцій і сумарні калорії
```

`MealLog` знає підписників лише через інтерфейс `MealLogListener`, а не через конкретні класи. Тому нового спостерігача можна додати без зміни коду суб’єкта: достатньо реалізувати інтерфейс і викликати `addListener(...)`.

Подія створюється тільки після успішної валідації та збереження порції. Якщо дані некоректні або щоденник переповнений, запис не додається і спостерігачі не отримують сповіщення.

У `Main.demoObserverUnsubscribe()` показано два різні спостерігачі, підписані на один `MealLog`, а також відписку `ConsoleMealObserver`: наступну подію отримує лише `CalorieSummaryObserver`.

## Adapter

`LegacyNutritionService` моделює сторонній або застарілий API. Він не реалізує `MealAdviceStrategy`, приймає інші одиниці вимірювання та повертає числовий код:

- енергія — у кілоджоулях;
- білок — у міліграмах;
- результат — `int` (`0`, `1`, `2`).

Наш код очікує `MealAdviceStrategy` з методом `evaluate(Product)`. Щоб не змінювати сторонній сервіс, додано `LegacyAdviceAdapter`.

```
Main / MealAdviser
       │
       ▼
MealAdviceStrategy (очікуваний інтерфейс)
       ▲
       │ implements
LegacyAdviceAdapter
       │ містить посилання через композицію
       ▼
LegacyNutritionService (несумісний API)
```

Адаптер виконує переклад:

| Наш інтерфейс | Зовнішній сервіс |
| --- | --- |
| `evaluate(Product)` | `classifyServing(double, double)` |
| калорії в ккал | енергія в кДж |
| білок у грамах | білок у міліграмах |
| текстова порада | числовий код `0/1/2` |

Це саме Adapter, а не переписування сервісу: `LegacyNutritionService` залишається без змін і може використовуватися іншими клієнтами. Адаптер містить посилання на сервіс через композицію, а не успадковується від нього.

## Взаємодія патернів

- **Factory** відповідає на питання: *який об’єкт створити?*
- **Strategy** відповідає на питання: *яку поведінку оцінки використати?*
- **Adapter** дозволяє підключити несумісний зовнішній сервіс як одну зі стратегій.
- **Observer** реагує на зміну стану `MealLog` незалежно від вибраної стратегії.

Наприклад, фабрика може створити `LegacyAdviceAdapter`, а `MealAdviser` використає його як звичайну стратегію. При цьому `MealLog` продовжує сповіщати своїх підписників після додавання порції.

## Де що реалізовано

| Вимога | Місце в коді |
| --- | --- |
| Рівень 1: Factory-клас або Factory-метод із параметром | `MealAdviceFactory.create(AdviceType)` |
| Рівень 1: заміна прямих `new` у клієнтському коді | `Main`, `BalancedAdviceStrategy` |
| Рівень 1: пояснення проблеми рефакторингу | розділ «Factory Method» |
| Рівень 2: інтерфейс Observer | `MealLogListener` |
| Рівень 2: Subject зі списком підписників | `MealLog` |
| Рівень 2: два різні спостерігачі | `ConsoleMealObserver`, `CalorieSummaryObserver` |
| Рівень 3: несумісний зовнішній API | `LegacyNutritionService` |
| Рівень 3: Adapter без зміни несумісного класу | `LegacyAdviceAdapter` |
| Демонстрація Observer | `Main.demoObserverUnsubscribe()` |
| Демонстрація Adapter | `Main.demoPolymorphicStrategies()`, `Main.demoStrategyRuntimeSwitch()` |

Повний опис ЛР1–ЛР4 — валідація `Product`, ієрархія винятків, `MealLog`, Strategy, `Comparable` та `Reportable` — без концептуальних змін, див. історію комітів.

## Як запустити

Потрібен JDK 11 або новіший.

```bash
mkdir out
javac -encoding UTF-8 -d out *.java
java -cp out Main
```

Запуск автоматичних перевірок:

```bash
java -cp out Lab5Tests
```

Запуск із демонстраційними даними:

```bash
java -cp out Main < demo-input.txt
```

У Windows можна використати `run.bat` для запуску програми та `test.bat` для запуску тестів.
