# PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW - AUDIT_TELEPORT_FLOW

## Status
STATUS: PASS_AUDIT_ONLY

## Goal
Audit the current MR-Portal teleport flow before introducing any public teleport-handler API. This phase maps teleport entry points, cost/cooldown/payment paths, preview spark behavior, client visual behavior, and portal session lifecycle.

No Java source code was modified. No public API was implemented. No gameplay behavior was changed.

## Baseline
- Workspace root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/`
- Active base mod root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/`
- Backup snapshot before API/source changes requested by phase context: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/Backup-MR-portal-20260530-172501/`
- Backup path availability during this audit: only `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/Backup-MR-portal/` was present when checked after the audit; `Backup-MR-portal-20260530-172501/` was not present in the workspace listing.
- Branch: `2.2`
- HEAD: unavailable, `git rev-parse --short HEAD` returned `fatal: Needed a single revision`
- Remote:
```text
origin	https://github.com/sqwiziiy/MR-Portal (fetch)
origin	https://github.com/sqwiziiy/MR-Portal (push)
```
- Git status summary: repository has many untracked files, including `Notes/`, Gradle files, source files, resources, changelogs, and `пример.txt`.
- Java source changes in this phase: none.

## Files inspected
- `build.gradle`: Fabric Loom setup, split client/main source sets, Java 17 compile target.
- `gradle.properties`: Minecraft `1.20.1`, loader `0.18.4`, Loom `1.15-SNAPSHOT`, mod version `2.2.0`.
- `settings.gradle`: plugin repositories only.
- `src/main/resources/fabric.mod.json`: mod id `mr-portal`, main/client/modmenu entrypoints, Java `>=17`.
- `src/main/java/com/mentality/mrportal/MRPortal.java`: server init, config/items/network registration, server tick hook.
- `src/client/java/com/mentality/mrportal/MRPortalClient.java`: client networking/render registration and keybinds.
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`: packet ids, server packet handlers, waypoint screen payload, preview sparks, portal visual packets.
- `src/client/java/com/mentality/mrportal/client/screen/MRPortalClientNetworking.java`: client packet receivers and client packet senders.
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`: teleport request validation, payment/cooldown, session creation, quick favorite delay, tick/session lifecycle, entity teleport.
- `src/main/java/com/mentality/mrportal/portal/PortalSession.java`: immutable session record and close-state transition.
- `src/main/java/com/mentality/mrportal/config/MRPortalConfig.java`: teleport-related config values and sanitization.
- `src/main/java/com/mentality/mrportal/config/MRPortalConfigManager.java`: config load/save/access.
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`: staff, infinite staff, scroll registration and item predicates.
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`: staff use opens waypoint screen; client tooltip/cosmetic particles.
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`: scroll use opens waypoint screen in non-creative mode.
- `src/main/java/com/mentality/mrportal/waypoint/WaypointData.java`: waypoint record serialization/network payload.
- `src/main/java/com/mentality/mrportal/waypoint/ServerWaypointStore.java`: waypoint storage, lookup, favorite handling.
- `src/client/java/com/mentality/mrportal/client/screen/WaypointScreen.java`: GUI list, local validation, teleport request, screen close.
- `src/client/java/com/mentality/mrportal/client/render/ClientPortalEffectManager.java`: preview spark, remote spark, portal visual lifecycle.
- `src/main/java/com/mentality/mrportal/util/DimensionUtil.java`: dimension id/key conversion and display names.
- `src/client/java/com/mentality/mrportal/client/config/MRPortalConfigScreen.java`: config UI entries for relevant fields.
- `src/client/java/com/mentality/mrportal/client/config/MRPortalModMenuIntegration.java`: ModMenu config screen integration.

No other files exist under `src/main/java/com/mentality/mrportal/portal/`. No separate client keybind file exists; keybinds live in `MRPortalClient.java`.

## Teleport entry point map

### GUI waypoint teleport through staff use
1. Client/server item use: `PortalStaffItem.use`.
2. On server, calls `MRPortalNetworking.sendWaypointScreen(serverPlayer, this.infinite || player.getAbilities().instabuild)`.
3. Server sends `OPEN_SCREEN_S2C` and broadcasts preview spark via `broadcastPreviewSpark`.
4. Client opens `WaypointScreen`, selects waypoint, clicks teleport.
5. Client sends `TELEPORT_REQUEST_C2S` with waypoint id and `useScroll`.
6. Server handler in `MRPortalNetworking.registerServer` reads waypoint id, looks up `ServerWaypointStore.getWaypoint`.
7. Non-creative/non-infinite flow enforces same-dimension rule.
8. If not scroll: calls `PendingTeleportManager.beginTeleport(player, waypoint, creativeView)`.
9. `beginTeleport` handles active session, staff lookup, cooldown, pearl cost, cooldown application, and calls `startSession`.

### GUI waypoint teleport through teleport scroll use
1. Item use: `TeleportScrollItem.use`.
2. Server calls `MRPortalNetworking.sendWaypointScreen(serverPlayer, false)`.
3. Client receiver detects no staff in hands and sets `WaypointScreen.useScroll = true`.
4. Teleport button sends `TELEPORT_REQUEST_C2S` with `useScroll = true`.
5. Server same handler calls `PendingTeleportManager.beginScrollTeleport(player, waypoint)` when not creative view.
6. `beginScrollTeleport` checks active session, finds scroll, consumes one scroll immediately, and calls `startSession`.

### Regular portal staff
- Detected by `MRPortalItems.isPortalStaff`, which includes regular and infinite staff.
- Payment/cooldown path uses `PendingTeleportManager.findPortalStaff`, `consumeEnderPearls`, and `player.getCooldowns().addCooldown`.
- Regular staff requires same dimension unless creative/infinite view is active.

### Infinite staff / creative path
- Infinite staff is detected by `MRPortalItems.isInfinite`.
- Creative view is active if player has an infinite staff in hand/inventory or `player.getAbilities().instabuild`.
- `MRPortalNetworking.hasCreativeView` drives GUI filtering and free resource display.
- `PendingTeleportManager.beginTeleport(..., infinite=true)` skips staff requirement, pearl cost, and cooldown.
- Creative players also bypass cost/cooldown through `player.getAbilities().instabuild`.
- Same-dimension restriction is bypassed in creative/infinite GUI view.

### Quick favorite keybind
1. Client keybind in `MRPortalClient`: grave accent sends `MRPortalClientNetworking.sendQuickTeleportByKeybind()`.
2. Packet id: `QUICK_TELEPORT_BY_KEYBIND_C2S`.
3. Server handler gets favorite with `ServerWaypointStore.getFavoriteWaypoint(player.getUUID())`.
4. Rejects missing favorite, active session, missing item, and non-infinite cross-dimension favorites.
5. Chooses item by `PendingTeleportManager.findPreferredTeleportItem`: infinite staff first, regular staff second, scroll last.
6. Calls `PendingTeleportManager.queueFavoriteTeleport`.
7. `queueFavoriteTeleport` stores a `PendingFavoriteActivation`, broadcasts a quick preview spark, and delays activation by `quickFavoritePortalDelayTicks`.
8. Tick activation later calls `activatePendingFavoriteTeleport`, which repeats current dimension/item/payment/cooldown checks and starts a normal `PortalSession`.

### Other discovered path: open GUI by keybind
- Client V key sends `OPEN_BY_KEYBIND_C2S`.
- Server checks inventory/hands for staff or scroll.
- If staff or creative view exists, opens waypoint screen with creative flag as appropriate.
- If only scroll exists, opens screen with creative false.
- This is not a direct teleport entry point, but it is a teleport UI entry point.

## GUI waypoint teleport flow
1. Screen open starts through `PortalStaffItem.use`, `TeleportScrollItem.use`, or `OPEN_BY_KEYBIND_C2S`.
2. Server builds `OPEN_SCREEN_S2C` in `sendWaypointScreen`:
   - calculates source preview position with `PendingTeleportManager.calculateSourcePortalCenter`;
   - sends creative flag, current dimension, dimension list, filtered waypoints, available pearls, required pearls, preview position/yaw/scale.
3. Waypoints are filtered to same dimension unless creative view is true.
4. Server sends `OPEN_SCREEN_S2C` to the player.
5. Server also calls `broadcastPreviewSpark(player, previewPos)` to nearby players, excluding the opener.
6. Client receives `OPEN_SCREEN_S2C`, reconstructs waypoints, infers scroll mode if not creative and the player lacks staff in hands, then opens `WaypointScreen`.
7. `WaypointScreen.init` displays a local preview spark for the opener through `ClientPortalEffectManager.showPreviewSpark`.
8. User selects a waypoint. Client-side `tryTeleport` checks selection, non-creative same-dimension, and available pearl count.
9. Client sends `TELEPORT_REQUEST_C2S` with waypoint id and `useScroll`.
10. `WaypointScreen.onClose` hides local preview spark and sends `SCREEN_CLOSED_C2S`.
11. Server `SCREEN_CLOSED_C2S` removes remote preview spark. Successful server teleport start also removes preview spark and closes container.
12. Server `TELEPORT_REQUEST_C2S` loads waypoint from `ServerWaypointStore.getWaypoint`.
13. Server enforces non-creative same-dimension rule again.
14. Server calls either `beginScrollTeleport` or `beginTeleport`.
15. `PendingTeleportManager.startSession` creates `PortalSession`, stores it in `sessionsByPlayer`, and broadcasts `PORTAL_CREATE_S2C`.
16. `PendingTeleportManager.tick` waits `portalOpenDelay`, detects the owner inside the source portal, teleports to destination exit, optionally applies blindness, sends destination/source effects to the teleported player, and marks the session closing for `portalCloseDelayTicks`.
17. Other entities may teleport through the source portal while active/closing.
18. Session cleanup broadcasts `PORTAL_CLOSE_S2C`.

## Teleport scroll flow
- `useScroll` is set client-side by `MRPortalClientNetworking` when opening a non-creative waypoint screen and the player lacks a staff in main/off hand.
- Scroll use directly opens the screen with `creativeView=false`.
- The GUI sends `TELEPORT_REQUEST_C2S` with `useScroll`.
- Server only uses scroll path when `useScroll && !creativeView`.
- `beginScrollTeleport` requires no active session and finds a teleport scroll in hand/inventory.
- Scroll is consumed immediately with `scroll.shrink(1)` before `startSession`.
- Scroll does not consume pearls and does not apply staff cooldown.
- Scroll uses the same waypoint lookup, same non-creative same-dimension rule, and same `PortalSession` behavior as staff.
- Quick favorite scroll also consumes one scroll in `activatePendingFavoriteTeleport` after the delay, before `startSession`.
- Risk: if `startSession` failed after `scroll.shrink(1)`, the scroll would already be consumed. Current `startSession` always returns true once invoked.

## Staff/infinite/creative flow
- Regular/infinite staff registration is in `MRPortalItems`.
- Staff use opens screen via `PortalStaffItem.use`.
- `MRPortalItems.isPortalStaff` includes both regular and infinite staff.
- `MRPortalItems.isInfinite` detects only the infinite staff item.
- `MRPortalNetworking.hasCreativeView` returns true for infinite staff in hands/inventory or creative instabuild.
- `PendingTeleportManager.beginTeleport`:
  - rejects active session;
  - calculates `pearlCost = getRequiredPearls(infinite || instabuild)`;
  - for non-infinite/non-creative, requires a portal staff, checks item cooldown, consumes ender pearls, and applies cooldown;
  - then starts a session.
- Pearl cost is counted/consumed from player inventory only, not hands separately; hand-held items are part of inventory in normal player inventory semantics.
- Cooldown is applied to the staff item type using `player.getCooldowns().addCooldown(staff.getItem(), cooldownSeconds * 20)`.
- Same-dimension rule is enforced in networking before `beginTeleport`, and again for quick favorite delayed activation.

## Quick favorite flow
- Client trigger: `MRPortalClient` quick keybind, default grave accent.
- Packet id: `QUICK_TELEPORT_BY_KEYBIND_C2S`.
- Server favorite lookup: `ServerWaypointStore.getFavoriteWaypoint`.
- Item choice: `PendingTeleportManager.findPreferredTeleportItem`, priority infinite staff, regular staff, scroll.
- Delay behavior: `queueFavoriteTeleport` stores `activateTick = currentTick + quickFavoritePortalDelayTicks`.
- Preview behavior: quick favorite broadcasts preview spark including the triggering player.
- At activation, invalid player/dead/dimension-changed removes preview and aborts.
- Activation repeats active session check, same-dimension rule for non-infinite/non-creative, scroll lookup/consumption, staff lookup, cooldown check, pearl consumption, cooldown application.
- Quick favorite does not bypass base validation, payment, or cooldown; it delays them until activation.
- It currently bypasses the GUI and local client resource status display, so errors surface only as action bar messages.
- It should route through the future API because otherwise addon handlers would affect GUI teleport but not quick favorite teleport.

## Portal session lifecycle
- `PortalSession` fields: `sessionId`, `playerId`, source dimension/position/yaw, destination dimension/portal position/exit position/yaw, `createdTick`, `infinite`, `teleported`, `closeTick`.
- `PendingTeleportManager` stores active sessions in `sessionsByPlayer` keyed by player UUID.
- `teleportedEntities` tracks entity UUIDs per session to prevent repeated entity teleports.
- `pendingFavoriteActivations` stores delayed quick favorite activations.
- `startSession` calculates source and destination portal positions, stores a session, and broadcasts portal create.
- Tick lifecycle:
  - pending favorites activate after `quickFavoritePortalDelayTicks`;
  - session age below `portalOpenDelay` does not teleport;
  - after open delay, owner inside the source portal teleports;
  - if no owner enters by `portalOpenDelay + portalActiveTicks`, session is marked closing;
  - closing waits `portalCloseDelayTicks` and broadcasts close;
  - owner death/offline closes session;
  - missing destination level closes session.
- Entity behavior:
  - owner uses `ServerPlayer.teleportTo(destinationLevel, exitX, exitY, exitZ, yaw, pitch)`;
  - other players use the same player teleport method and receive current portal effects;
  - non-player entities use `teleportTo` for same dimension or `changeDimension` then `teleportTo` for cross-dimension.
- No explicit server-stopped/world-unload cleanup hook was found beyond the manager being held in a `WeakHashMap<MinecraftServer, PendingTeleportManager>`.

## Preview spark lifecycle
- GUI preview spark source position is calculated in `sendWaypointScreen`.
- Server sends remote preview via `PREVIEW_SPARK_S2C` to nearby players, excluding opener for normal GUI open.
- Client opener shows a local preview in `WaypointScreen.init`.
- Screen close hides local preview and sends `SCREEN_CLOSED_C2S`; server removes remote preview via `PREVIEW_SPARK_REMOVE_S2C`.
- Successful server teleport start also removes server-side preview and closes the container.
- Quick favorite uses `broadcastQuickPreviewSpark`, which includes the player, and `removeQuickPreviewSpark`, which also includes the player.
- Future `usesDefaultPreviewSpark()` hook would need to choose whether to call `broadcastPreviewSpark`/local preview for GUI and quick favorite paths without leaving stale entries in `activePreviewSparks` and `activePreviewDimensions`.

## Client visual lifecycle
- `MRPortalClientNetworking` registers receivers for `OPEN_SCREEN_S2C`, `PORTAL_CREATE_S2C`, `PORTAL_CLOSE_S2C`, `PREVIEW_SPARK_S2C`, and `PREVIEW_SPARK_REMOVE_S2C`.
- `PORTAL_CREATE_S2C` passes session id, source flag, dimension, position, yaw, open delay, lifetime, scale, animation speed, and sound flags to `ClientPortalEffectManager.addEffect`.
- `PORTAL_CLOSE_S2C` calls `ClientPortalEffectManager.closeSession`.
- `PREVIEW_SPARK_S2C` calls `showRemoteSpark`; remove packet calls `hideRemoteSpark`.
- `ClientPortalEffectManager` owns client-only maps for portal effects and remote sparks, local preview spark state, render registration, client tick cleanup, opening/closing animation, particles, and local sounds.
- Client-only constraints: server source set must not reference `ClientPortalEffectManager`, `WaypointScreen`, or client networking classes. Server talks through packet ids only.

## Config dependency map
- `cooldownSeconds`: regular staff cooldown duration; tooltip display in `PortalStaffItem`; config UI.
- `maxWaypoints`: add waypoint server limit; config UI.
- `portalOpenDelay`: portal visual open delay and server delay before teleport detection; config UI.
- `portalActiveTicks`: active window before no-entry close starts; config UI.
- `portalCloseDelayTicks`: post-teleport/no-entry close delay; config UI.
- `portalPearlCost`: regular staff pearl requirement; sent to screen and consumed by manager; config UI.
- `applyBlindnessOnTeleport`: applies blindness to owner after teleport; config UI expected in config screen.
- `blindnessDuration`: blindness effect duration; config UI expected in config screen.
- `portalScale`: visual scale, preview scale, portal collision bounds; config UI.
- `portalSpawnDistance`: source portal offset from player; config UI.
- `portalVerticalOffset`: source/destination portal vertical offset; config UI.
- `portalAnimationSpeed`: client portal visual speed; config UI.
- `quickFavoritePortalDelayTicks`: delay before quick favorite activation; config UI.
- `portalExitBehindDistance`: destination portal visual offset behind destination; exit target itself remains waypoint position; config UI.
- `language`: config/general UI and translation behavior, not teleport logic.

## Payment/cooldown ownership
Current ownership is entirely base MR-Portal:
- waypoint lookup: `ServerWaypointStore`;
- item validation: `MRPortalItems` and `PendingTeleportManager` find methods;
- regular staff pearl cost: `getRequiredPearls`, `countEnderPearls`, `consumeEnderPearls`;
- scroll consumption: `beginScrollTeleport` and delayed favorite activation;
- cooldown check/application: `PendingTeleportManager`;
- creative/infinite bypass: networking creative view and manager payment checks;
- same-dimension restriction: networking and delayed favorite activation;
- quick favorite lookup/delay: networking and manager.

Future addon handlers should not duplicate or own payment/cooldown. They should receive a context after base validation/payment decisions are made, or through a service that keeps these steps centralized.

## API refactor risk map
- Double pearl consumption: if both request service and default handler consume pearls.
- Missing pearl consumption: if API route bypasses `beginTeleport`/payment service.
- Missing scroll consumption: scroll path currently consumes before session start; API must preserve exactly one scroll consumption.
- Missing cooldown: regular staff cooldown is currently item-type based and applied only after pearl consumption.
- Quick favorite bypass: if only GUI route uses API, quick favorite remains hardwired to default session behavior.
- Preview spark stuck: custom preview behavior must still remove entries on screen close, success, abort, player disconnect/dimension change.
- Screen close mismatch: `tryTeleport` sends request then `onClose`; server may receive close and success removal in either order.
- Entity portal regression: current default portals teleport owner and other entities; handler API must define whether addons replace only owner execution or whole portal entity behavior.
- Same-dimension rule lost: non-infinite/non-creative currently blocks cross-dimension before starting sessions and again for delayed favorites.
- Client class referenced from server source set: API/default handler must avoid client package references from `src/main`.
- Packet compatibility broken: existing packet ids and payload order are client/server coupled.
- Mutable internal waypoint leak: future API should expose immutable/public waypoint view, not internal mutable storage state.
- Default behavior changes without addon: default handler must reproduce current `PortalSession` lifecycle.
- Delayed favorite payment timing: quick favorite currently validates item/payment at activation time, not keypress time.

## Proposed next implementation phases

### PHASE_MRP_API_02_PUBLIC_HANDLER_API
- Goal: add public API contracts only, no routing changes.
- Likely files touched: new package under `src/main/java/com/mentality/mrportal/api/`, `API_DESIGN.md`, report/board notes.
- Risk level: low if isolated.
- Validation: compile main/client, grep no Chrono checks, confirm no behavior route changes.

### PHASE_MRP_API_03_DEFAULT_HANDLER_AND_REQUEST_SERVICE
- Goal: extract base-owned request validation/payment into an internal service and register a default handler that preserves current session behavior.
- Likely files touched: `PendingTeleportManager`, new internal request/service classes, maybe `MRPortal.java`.
- Risk level: high.
- Validation: staff cost/cooldown, scroll consumption, creative/infinite bypass, same-dimension, no-addon default behavior.

### PHASE_MRP_API_04_ROUTE_GUI_TELEPORT_THROUGH_API
- Goal: route `TELEPORT_REQUEST_C2S` through the request service/active handler.
- Likely files touched: `MRPortalNetworking`, request service, default handler.
- Risk level: high.
- Validation: GUI staff, scroll, infinite/creative, preview spark removal, screen close.

### PHASE_MRP_API_05_ROUTE_QUICK_FAVORITE_THROUGH_API
- Goal: route delayed favorite activation through the same API path.
- Likely files touched: `PendingTeleportManager`, request service, default handler.
- Risk level: high.
- Validation: favorite lookup, delay, preview spark, delayed payment/cooldown, abort on dimension change.

### PHASE_MRP_API_06_PREVIEW_SPARK_HANDLER_HOOK
- Goal: add handler control over default preview spark use without stale visuals.
- Likely files touched: API interface, networking preview methods, client visual assumptions.
- Risk level: medium.
- Validation: GUI open/close, quick favorite, addon/default preview on/off.

### PHASE_MRP_API_07_NO_ADDON_REGRESSION_VALIDATION
- Goal: validate base behavior with no addon and with a minimal test handler if available.
- Likely files touched: reports only unless fixes are required.
- Risk level: medium.
- Validation: compile, runClient/manual flows, packet sanity, no hardcoded addon checks.

## Validation performed

Required baseline commands were run from `MR-portal/`:
```text
pwd: /home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal
branch: 2.2
HEAD: fatal: Needed a single revision
remote: origin https://github.com/sqwiziiy/MR-Portal
```

Required file listings were run for main Java, client Java, and main resources.

Required grep results:
- `beginTeleport`: `MRPortalNetworking.java`, `PendingTeleportManager.java`
- `beginScrollTeleport`: `MRPortalNetworking.java`, `PendingTeleportManager.java`
- `queueFavoriteTeleport`: `MRPortalNetworking.java`, `PendingTeleportManager.java`
- `teleportTo`: owner/player/entity teleport calls in `PendingTeleportManager.java`
- `broadcastPreviewSpark`: `MRPortalNetworking.java`
- `removePreviewSpark`: `MRPortalNetworking.java`
- `PORTAL_CREATE_S2C`: server packet send and client receiver
- `PORTAL_CLOSE_S2C`: server packet send and client receiver
- `TELEPORT_REQUEST_C2S`: server receiver and client sender
- `QUICK_TELEPORT_BY_KEYBIND_C2S`: server receiver and client sender
- `cooldown`: config, tooltip, `PendingTeleportManager`
- `portalPearlCost`: config, manager, config UI
- `portalOpenDelay`, `portalActiveTicks`, `portalCloseDelayTicks`: config, session tick/visuals, config UI
- `quickFavoritePortalDelayTicks`: config, pending favorite activation, config UI
- `favorite`: network favorite setter, store, quick favorite, UI, keybind
- `Waypoint`: network, portal, waypoint store/data, client screen/network
- `ender`/`pearl`: manager payment and config UI
- `ItemStack`/`inventory`: item detection and payment paths
- `hurtAndBreak`: no results
- `shrink`: scroll and pearl consumption in `PendingTeleportManager`
- `setCooldown`: no results
- `getCooldowns`: tooltip and manager cooldown check/application

Compile was not attempted in this audit phase.

Post-update validation:
```text
test -f Notes/REPORTS&AUDITS/PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW_REPORT.md: pass
test -f Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW.md: pass
test -f Notes/OBSIDIAN_BOARD/mrportalboard.canvas: pass
python3 -m json.tool Notes/OBSIDIAN_BOARD/mrportalboard.canvas >/dev/null: pass
```

Grep sanity after report/board updates:
```text
grep -R "mr_portal_chrono" -n src || true: no output
grep -R "chrono_portal" -n src || true: no output
grep -R "FabricLoader.*isModLoaded" -n src || true: no output
grep -R "MRPortalApi" -n src || true: no output
grep -R "registerTeleportHandler" -n src || true: no output
```

Source diff checks:
```text
git diff --stat: no output
git diff -- src: no output
diff -qr src ../Backup-MR-portal/src || true: no output
```

Canvas integrity:
```text
json valid: yes
duplicate node ids: none
duplicate edge ids: none
broken file paths: none
overlap warnings: none
```

## Manual validation
Manual validation: not applicable, no gameplay code changed.

## Known risks / unknowns
- `MR-portal` has no usable Git `HEAD`, so source baseline is filesystem/backup based.
- Current repo is mostly untracked.
- `MR-portal/Notes/` was restored during the previous backup phase from misplaced copies; this audit assumes the restored base Notes tree is the active tracking location.
- The phase context names `Backup-MR-portal-20260530-172501/`, but that timestamped folder was not present at validation time. The available backup folder was `Backup-MR-portal/`, and `src` matched it by `diff -qr`.
- Packet compatibility is tightly coupled and needs careful preservation.
- No explicit server-stop cleanup hook for active sessions was found.

## Next recommended phase
PHASE_MRP_API_02_PUBLIC_HANDLER_API
