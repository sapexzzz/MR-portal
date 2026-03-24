# Changelog 2.0.0

## Новые возможности

- **Звезда избранного прямо в списке**: Переключатель избранной метки перенесён в начало строки точки телепорта. Теперь используется пустая звезда `☆` для обычной точки и жёлтая заполненная `★` для избранной.
- **Задержка быстрого избранного портала**: Быстрый бинд избранного портала больше не открывает портал мгновенно. По умолчанию сначала проходит 30 тиков (1.5 секунды), в течение которых в центре будущего портала видны искры, и только затем начинается открытие портала.
- **Настраиваемое смещение выходного портала**: Новый параметр `portalExitBehindDistance` (по умолчанию 0.2) управляет тем, насколько далеко за спиной игрока появляется портал назначения после телепорта.
- **Настраиваемая задержка быстрого избранного бинда**: Новый параметр `quickFavoritePortalDelayTicks` (по умолчанию 30) управляет задержкой перед стартом открытия портала после нажатия быстрого бинда.

## Исправления

- **Лишний звук у выходного портала**: Портал назначения больше не воспроизводит звук открытия при появлении после телепорта.
- **Межмировые метки для обычных предметов**: Для обычного посоха и свитка экран выбора и логика телепорта теперь ограничены только метками текущего измерения. Бесконечный посох по-прежнему может работать между измерениями.
- **Быстрый бинд избранного и другие измерения**: Если избранная метка находится в другом измерении, быстрый бинд теперь корректно показывает сообщение об ограничении вместо попытки открыть такой портал обычным предметом.

## Технические изменения

- `PendingTeleportManager` получил очередь `PendingFavoriteActivation` для отложенного открытия избранного портала с предварительными искрами.
- `PortalSession` расширен отдельной точкой выхода `destinationExitPos`, чтобы визуальный портал можно было смещать назад, не сдвигая саму точку появления игрока.
- В сетевые пакеты создания портала добавлены флаги `playChime` и `playOpenSound`, чтобы отдельно управлять звуком появления исходного и выходного порталов.
- `sendWaypointScreen()` теперь фильтрует список меток по текущему измерению, если у игрока нет креативного доступа к межмировым порталам.

## Изменённые файлы

- src/main/java/com/mentality/mrportal/config/MRPortalConfig.java
- src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java
- src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java
- src/main/java/com/mentality/mrportal/portal/PortalSession.java
- src/client/java/com/mentality/mrportal/client/config/MRPortalConfigScreen.java
- src/client/java/com/mentality/mrportal/client/render/ClientPortalEffectManager.java
- src/client/java/com/mentality/mrportal/client/screen/MRPortalClientNetworking.java
- src/client/java/com/mentality/mrportal/client/screen/WaypointScreen.java
- src/main/resources/assets/mr_portal/lang/en_us.json
- src/main/resources/assets/mr_portal/lang/ru_ru.json
- gradle.properties
