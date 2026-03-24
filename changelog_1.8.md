# Changelog 1.8.0

## Новые возможности

- **Настраиваемое вертикальное смещение портала**: Новый параметр `portalVerticalOffset` (по умолчанию 0.0) позволяет поднимать или опускать портал относительно стандартной позиции. Диапазон: от -2.0 до 5.0.
- **Скорость анимации портала**: Новый параметр `portalAnimationSpeed` (по умолчанию 1.0) — множитель скорости открытия и закрытия портала. Значение 2.0 = в два раза быстрее, 0.5 = в два раза медленнее. Диапазон: от 0.1 до 5.0.

## Исправления

- **Настройка языка мода меняла весь интерфейс Minecraft**: Ранее при выборе языка в настройках мода (`en` / `ru`) изменялся глобальный язык Minecraft и перезагружались все ресурсы. Теперь язык мода работает полностью автономно — затрагивает только тексты мода (GUI, тултипы, сообщения), не влияя на остальной интерфейс игры.

## Обновлённые значения по умолчанию

- `portalActiveTicks`: 60 → 100
- `portalScale`: 1.2 → 1.5
- `portalSpawnDistance`: 2.0 → 2.2

## Оптимизация

- Конфигурация кэшируется в локальных переменных при обработке сетевых пакетов вместо повторных вызовов `MRPortalConfigManager.get()`.

## Технические изменения

- Создан утилитарный класс `ModTranslation` (`com.mentality.mrportal.util`), который загружает файлы перевода мода из ресурсов и предоставляет метод `get(key, args...)`. В режиме `auto` возвращает `Component.translatable()`, в режиме `en`/`ru` — `Component.literal()` с подставленным переводом.
- Все вызовы `Component.translatable()` в модовых GUI и сообщениях заменены на `ModTranslation.get()`: WaypointScreen, MRPortalConfigScreen, PendingTeleportManager, MRPortalNetworking, PortalStaffItem, TeleportScrollItem.
- Удалён метод `applyLanguage()` из `MRPortalConfigScreen`, который вызывал `minecraft.getLanguageManager().setSelected()` и `reloadResourcePacks()`.
- Константа `VERTICAL_PORTAL_OFFSET` в `PendingTeleportManager` заменена на динамический метод `verticalOffset(config)`, который учитывает `portalVerticalOffset` из конфига.
- Скорость анимации (`animSpeed`) передаётся через сетевые пакеты создания портала на клиент, где масштабирует `SPARK_DURATION` и `CLOSE_DURATION`.

## Изменённые файлы

- src/main/java/com/mentality/mrportal/config/MRPortalConfig.java
- src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java
- src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java
- src/main/java/com/mentality/mrportal/item/PortalStaffItem.java
- src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java
- src/client/java/com/mentality/mrportal/client/render/ClientPortalEffectManager.java
- src/client/java/com/mentality/mrportal/client/network/MRPortalClientNetworking.java
- src/client/java/com/mentality/mrportal/client/screen/MRPortalConfigScreen.java
- src/client/java/com/mentality/mrportal/client/screen/WaypointScreen.java
- src/main/resources/assets/mr_portal/lang/en_us.json
- src/main/resources/assets/mr_portal/lang/ru_ru.json
- gradle.properties

## Новые файлы

- src/main/java/com/mentality/mrportal/util/ModTranslation.java
