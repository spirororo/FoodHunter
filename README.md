# FoodHunter
## Икромова Хабибахон Умаржоновна, Б9123-09.03.03пикд

API — TheMealDB
https://www.themealdb.com/api.php

Бесплатное API для поиска рецептов, ключ не нужен.

Эндпоинты:
  GET /api/json/v1/1/search.php?s=  — поиск блюд по названию
  GET /api/json/v1/1/lookup.php?i=  — получение полной информации о блюде по id

-------------------------------------------------------

## Что сделано в ДЗ №4

Hilt (DI)
  - @HiltAndroidApp на классе FoodApp
  - @AndroidEntryPoint на StartActivity
  - @HiltViewModel на FoodViewModel
  - Один общий DI-модуль AppModule — и сеть и база данных
  - Все зависимости инжектятся через конструктор, ничего руками не создаётся

Room (таблица watch_history)
  - Сценарий: История просмотров — при открытии детальной страницы блюдо записывается в Room, история переживает перезапуск
  - Таблица: watch_history с полями dishId, dishName, dishThumb, openedAt
  - Entity: HistoryItem
  - DAO: HistoryDao — insert (replace), delete, observe (Flow), clear all
  - Database: AppDatabase

Как проверить
  1. Запустить приложение
  2. Найти блюдо (например "chicken")
  3. Открыть любое блюдо — оно появится на вкладке "История"
  4. Закрыть приложение полностью (убить из recents)
  5. Открыть снова — история на месте, всё сохранилось в Room

-------------------------------------------------------

Скриншоты


<img width="1080" height="2400" alt="Screenshot_20260318_011127" src="https://github.com/user-attachments/assets/56689c37-0eb9-470c-b2aa-881ad5f6b695" />
<img width="1080" height="2400" alt="Screenshot_20260318_011115" src="https://github.com/user-attachments/assets/35b253be-1846-40db-8f03-938ceb8293f8" />
<img width="1080" height="2400" alt="Screenshot_20260318_011050" src="https://github.com/user-attachments/assets/87f236bb-4cf1-44e3-87ed-9eb6b0ffcf78" />

-------------------------------------------------------

Чек-лист

Из ДЗ №3:
  + 2 экрана: Home (поиск) и Dish/{id} (детали)
  + Navigation Compose с нижней навигацией
  + ViewModel + StateFlow + sealed interface для UI-состояний
  + Retrofit + coroutines + viewModelScope
  + Состояния: Loading / Error + Retry / Empty / Success

ДЗ №4:
  + Hilt — DI подключён, всё через inject
  + Room — таблица watch_history, история просмотров переживает перезапуск
  + Приложение из ДЗ №3 работает
