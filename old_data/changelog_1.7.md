# Changelog 1.7.0

## Исправления

- **Фикс краша при междимерной телепортации мобов**: `Entity.changeDimension()` удаляет исходную сущность и возвращает новую (или null). Теперь `teleportNearbyEntities()` корректно обрабатывает оба случая: внутри одного измерения используется `entity.teleportTo()`, при переходе между измерениями — `changeDimension()` с проверкой на null.

## Новые возможности

- **Свиток телепорта (одноразовый)**: Новый предмет `mr_portal:teleport_scroll` — расходуемый свиток телепортации. Стакается до 16 штук. Выглядит как зачарованная бумага (enchant glint). При использовании (ПКМ) открывает меню вейпоинтов. После телепортации один свиток расходуется. Не требует эндер-жемчуга. Не работает для телепортации между измерениями (как и обычный посох).
- **Рецепт свитка**: 4 эндер-жемчуга по сторонам + бумага в центре → 2 свитка телепорта.
- **Бинд открытия GUI (V)**: Клавиша V (настраивается) открывает меню вейпоинтов без ПКМ по предмету. Приоритет: обычный/бесконечный посох → свиток телепорта. Работает если в инвентаре есть хотя бы один предмет мода.

## Изменения

- **Блокировка телепорта между измерениями в выживании**: Кнопка «Телепорт» теперь отключена, если выбранная точка находится в другом измерении, а у игрока обычный посох или свиток. Бесконечный посох (креативный режим) по-прежнему позволяет телепортироваться между измерениями.

## Технические изменения

- PendingTeleportManager: рефакторинг — выделен общий метод `startSession()`, используемый из `beginTeleport()` и нового `beginScrollTeleport()`. Метод `findTeleportScroll()` ищет свиток в руках и инвентаре. Исправлена `teleportNearbyEntities()` для раздельной обработки same-dim и cross-dim.
- MRPortalNetworking: новый пакет `OPEN_BY_KEYBIND_C2S`. Обработчик `TELEPORT_REQUEST` теперь читает `boolean useScroll` и маршрутизирует на `beginScrollTeleport` или `beginTeleport`. Обработчик `OPEN_BY_KEYBIND_C2S` проверяет инвентарь игрока (приоритет: посох → свиток).
- WaypointScreen: поле `useScroll`, метод `setUseScroll()`. Обновлён `updateButtonState()` — блокировка кнопки при cross-dimension в выживании. `tryTeleport()` — сообщение о невозможности телепорта между измерениями.
- MRPortalClientNetworking: обработчик OPEN_SCREEN определяет scroll-режим. Методы `sendTeleportRequest(UUID, boolean)` и `sendOpenByKeybind()`.
- MRPortalClient: регистрация keybind V через KeyBindingHelper, обработка через ClientTickEvents.
- TeleportScrollItem: новый класс предмета.
- MRPortalItems: регистрация TELEPORT_SCROLL, хелперы `isAnyModItem()`, `isTeleportScroll()`.

## Новые файлы

- src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java
- src/main/resources/data/mr_portal/recipes/teleport_scroll.json
- src/main/resources/assets/mr-portal/models/item/teleport_scroll.json

## Изменённые файлы

- src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java
- src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java
- src/main/java/com/mentality/mrportal/item/MRPortalItems.java
- src/client/java/com/mentality/mrportal/MRPortalClient.java
- src/client/java/com/mentality/mrportal/client/screen/MRPortalClientNetworking.java
- src/client/java/com/mentality/mrportal/client/screen/WaypointScreen.java
- src/main/resources/assets/mr-portal/lang/en_us.json
- src/main/resources/assets/mr-portal/lang/ru_ru.json
- gradle.properties
