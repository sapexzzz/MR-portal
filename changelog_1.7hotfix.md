# Changelog 1.7hotfix

## Исправления

- **Портал появлялся на позиции игрока, а не перед ним**: Источник портала (`sourcePos` в `startSession()`) вычислялся как `player.position()` вместо `calculateSourcePortalCenter()`. Теперь портал всегда появляется на расстоянии `portalSpawnDistance` (по умолчанию 2 блока) перед игроком — как для посоха, так и для свитка.
- **Портал уходил в землю**: `VERTICAL_PORTAL_OFFSET` был равен `(0, 1.0, 0)`, из-за чего нижний край портала (при высоте 1.34 блока) оказывался на 0.34 блока ниже поверхности. Теперь смещение равно `BASE_PORTAL_HALF_HEIGHT` (1.34), и портал стоит ровно на уровне земли.
- **Свиток телепорта работал иначе, чем посох**: Портал из свитка теперь создается с тем же расстоянием и поведением, что и из обычного посоха.

## Новые возможности

- **Автоматическая разблокировка рецептов**: При получении любого ингредиента рецепта (обсидиан, око Эндера, звезда Незера для посоха; эндер-жемчуг, бумага для свитка) рецепт автоматически появляется в книге крафта.

## Технические изменения

- PendingTeleportManager: `startSession()` теперь использует `calculateSourcePortalCenter(player, config)` вместо `player.position()`. Константа `VERTICAL_PORTAL_OFFSET` изменена с `(0, 1.0, 0)` на `(0, BASE_PORTAL_HALF_HEIGHT, 0)`.
- Добавлены адвансменты для разблокировки рецептов: `data/mr_portal/advancements/recipes/misc/portal_staff.json` и `teleport_scroll.json`.

## Изменённые файлы

- src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java
- gradle.properties

## Новые файлы

- src/main/resources/data/mr_portal/advancements/recipes/misc/portal_staff.json
- src/main/resources/data/mr_portal/advancements/recipes/misc/teleport_scroll.json
