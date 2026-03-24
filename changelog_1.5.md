# Changelog 1.5.0

## Исправления

- Портал назначения теперь появляется даже при телепортации за пределы дальности прорисовки. Ранее пакет PORTAL_CREATE_S2C отправлялся только игрокам в радиусе 64 блоков от точки назначения; теперь после телепорта пакет отправляется напрямую телепортированному игроку с пересчитанными таймингами.

## Новые возможности

- **Cloth Config API**: Все настройки мода можно редактировать прямо в игре через ModMenu → MR Portal. Экран конфигурации содержит две категории: «Основные» и «Портал».
- **Настройка языка**: Добавлена опция `language` в конфиг (auto / en / ru). При выборе конкретного языка он применяется к Minecraft и ресурсы перезагружаются.
- **ModMenu интеграция**: Мод регистрирует экран настроек через ModMenu API.

## Технические изменения

- MRPortalNetworking: добавлены методы `sendPortalCreateToPlayer()` и `sendSinglePortalToPlayer()` для отправки портала конкретному игроку после телепорта.
- PendingTeleportManager: после `player.teleportTo()` вызывается `sendPortalCreateToPlayer()`.
- MRPortalConfig: добавлено поле `language` с валидацией в `sanitize()`.
- MRPortalConfigScreen (клиент): полный экран Cloth Config со всеми параметрами.
- MRPortalModMenuIntegration (клиент): точка входа ModMenu API.
- fabric.mod.json: добавлен entrypoint `modmenu`, блок `suggests` для cloth-config и modmenu.
- build.gradle: добавлены maven-репозитории Shedaniel и TerraformersMC, зависимости cloth-config-fabric и modmenu.

## Новые зависимости

- Cloth Config API 11.1.136 (рекомендуется)
- ModMenu 7.2.2 (рекомендуется)

## Новые файлы

- src/client/java/com/mentality/mrportal/client/config/MRPortalConfigScreen.java
- src/client/java/com/mentality/mrportal/client/config/MRPortalModMenuIntegration.java

## Изменённые файлы

- src/main/java/com/mentality/mrportal/MRPortalNetworking.java
- src/main/java/com/mentality/mrportal/PendingTeleportManager.java
- src/main/java/com/mentality/mrportal/MRPortalConfig.java
- src/main/resources/fabric.mod.json
- src/main/resources/assets/mr_portal/lang/ru_ru.json
- src/main/resources/assets/mr_portal/lang/en_us.json
- build.gradle
- gradle.properties
