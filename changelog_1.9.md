# Changelog 1.9.0

## Исправления

- **Слишком большой хитбокс портала**: Телепорт больше не срабатывает заранее при простом приближении. Проверка входа переписана с большого цилиндра вокруг центра портала на пересечение с тонкой плоскостью портала с учетом поворота `yaw`.
- **Обычный посох в инвентаре**: Логика поиска посоха теперь учитывает не только руки, но и инвентарь, что делает поведение биндов последовательным.

## Новые возможности

- **Избранная метка**: Теперь можно отметить одну точку телепорта как избранную. Избранная точка сохраняется на сервере, выделяется звездой в списке и отображается в боковой панели.
- **Быстрый портал к избранной точке**: Добавлен второй настраиваемый бинд. По умолчанию используется клавиша `ё` (тот же физический keycode, что и grave accent).
- **Приоритет предметов для быстрого открытия**: При активации быстрого портала используется порядок: бесконечный посох, обычный посох, свиток телепорта.

## Поведение

- Если избранная точка находится в другом измерении, быстрый бинд сработает только при наличии бесконечного посоха или прав креатива.
- Если избранная точка не выбрана, игрок получает отдельное сообщение об этом.

## Технические изменения

- `WaypointData` расширен флагом `favorite`, обновлены сериализация в NBT, сетевой буфер и серверное JSON-хранилище.
- В `ServerWaypointStore` добавлены методы `setFavoriteWaypoint`, `clearFavoriteWaypoint` и `getFavoriteWaypoint`.
- В `MRPortalNetworking` добавлены новые пакеты: `set_favorite_waypoint` и `quick_teleport_by_keybind`.
- В `PendingTeleportManager` проверка попадания в портал теперь использует AABB сущности, плоскость портала и ограниченную толщину вместо круглой зоны без направления.

## Изменённые файлы

- src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java
- src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java
- src/main/java/com/mentality/mrportal/waypoint/WaypointData.java
- src/main/java/com/mentality/mrportal/waypoint/ServerWaypointStore.java
- src/client/java/com/mentality/mrportal/MRPortalClient.java
- src/client/java/com/mentality/mrportal/client/screen/MRPortalClientNetworking.java
- src/client/java/com/mentality/mrportal/client/screen/WaypointScreen.java
- src/main/resources/assets/mr_portal/lang/en_us.json
- src/main/resources/assets/mr_portal/lang/ru_ru.json
- gradle.properties
