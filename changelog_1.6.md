# Changelog 1.6.0

## Новые возможности

- **Телепортация мобов и других игроков**: Любая сущность (мобы, животные, другие игроки), зашедшая в активный портал, телепортируется в точку назначения. Портал теперь работает для всех, а не только для владельца. Работает и на сервере, и в одиночной игре.
- **Искра предпросмотра видна другим игрокам**: Когда игрок открывает экран выбора точки и появляется искра, другие игроки поблизости (64 блока) тоже видят её. Искра исчезает при закрытии экрана или при телепортации.

## Технические изменения

- PendingTeleportManager: добавлен метод `teleportNearbyEntities()` — сканирует AABB зоны портала и телепортирует всех сущностей, не являющихся владельцем, через портал. Отслеживание уже перенесённых сущностей через `Map<UUID, Set<UUID>> teleportedEntities`.
- MRPortalNetworking: добавлены пакеты `PREVIEW_SPARK_S2C`, `PREVIEW_SPARK_REMOVE_S2C`, `SCREEN_CLOSED_C2S`. Серверное отслеживание активных искр через `activePreviewSparks`/`activePreviewDimensions`. Методы `broadcastPreviewSpark()` и `removePreviewSpark()`.
- ClientPortalEffectManager: добавлена карта `remoteSparks` для отображения искр других игроков. Методы `showRemoteSpark()` и `hideRemoteSpark()`. Рендер и тик удалённых искр.
- MRPortalClientNetworking: обработчики PREVIEW_SPARK_S2C и PREVIEW_SPARK_REMOVE_S2C. Метод `sendScreenClosed()`.
- WaypointScreen: при закрытии экрана отправляет `SCREEN_CLOSED_C2S` на сервер.
- fabric.mod.json: обновлена ссылка на исходный код → https://github.com/sapexzzz/MR-portal

## Изменённые файлы

- src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java
- src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java
- src/client/java/com/mentality/mrportal/client/render/ClientPortalEffectManager.java
- src/client/java/com/mentality/mrportal/client/screen/MRPortalClientNetworking.java
- src/client/java/com/mentality/mrportal/client/screen/WaypointScreen.java
- src/main/resources/fabric.mod.json
- gradle.properties
