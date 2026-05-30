# PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE - ROUTE_GUI_TELEPORT_THROUGH_SERVICE

## Status

STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Goal

Route normal GUI waypoint teleport requests from `TELEPORT_REQUEST_C2S` through `TeleportRequestService` and the active `MRPortalTeleportHandler`, while leaving quick favorite routing unchanged.

## Baseline

- Root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `2.3`
- HEAD: unavailable (`fatal: Needed a single revision`)
- Remote:
  - `origin https://github.com/sqwiziiy/MR-Portal (fetch)`
  - `origin https://github.com/sqwiziiy/MR-Portal (push)`
- Git status summary: repository still has no usable committed baseline and project files are untracked.
- Java 21 Gradle note: use `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`.
- Previous paid-session phase: `PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START`.

## Files inspected

- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestResult.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestFailureReason.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportActivationInfo.java`
- `src/main/java/com/mentality/mrportal/portal/WaypointDataView.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalApi.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java`
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`
- `src/main/java/com/mentality/mrportal/config/MRPortalConfig.java`
- `src/main/java/com/mentality/mrportal/waypoint/ServerWaypointStore.java`
- `src/main/java/com/mentality/mrportal/waypoint/WaypointData.java`

## Pre-edit audit

- `TELEPORT_REQUEST_C2S` still directly looks up waypoints and calls `PendingTeleportManager.beginTeleport` or `beginScrollTeleport`.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` still uses favorite lookup, `findPreferredTeleportItem`, and `queueFavoriteTeleport`.
- `TeleportRequestService.handleGuiTeleportRequest(...)` is a skeleton returning `NOT_IMPLEMENTED`.
- Payment/cooldown helper logic still lives in `PendingTeleportManager`.
- `DefaultPortalTeleportHandler` starts paid sessions and contains no payment logic.

## Files changed

- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Routing summary

- `TELEPORT_REQUEST_C2S` now calls `TeleportRequestService.handleGuiTeleportRequest(server, player, waypointId, useScroll, hasCreativeView(player))`.
- On `result.successful()`, networking removes the preview spark and closes the container.
- On failure, networking does not remove the preview spark or close the screen, matching the old unsuccessful flow.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` remains the old path: favorite lookup, preferred item lookup, and `queueFavoriteTeleport`.

## Service behavior

- Waypoint lookup: `ServerWaypointStore.get(server).getWaypoint(player.getUUID(), waypointId)`.
- Activator classification:
  - creative player: `CREATIVE`
  - infinite staff creative view: `INFINITE_STAFF`
  - GUI scroll path: `TELEPORT_SCROLL`
  - normal staff path: `REGULAR_STAFF` unless an infinite staff is detected
- Same-dimension validation: blocked for non-creative/non-infinite requests.
- Active session behavior: service checks `PendingTeleportManager.hasActiveSession(player)` before payment.
- Payment/cooldown behavior:
  - regular staff checks staff presence, cooldown, and pearl count before consuming pearls/applying cooldown
  - scroll checks scroll presence before shrinking one scroll
  - creative/infinite uses no payment
- Context creation: builds `MRPortalTeleportContext` with `WaypointDataView`, `quickTeleport=false`, and computed cost/cooldown values.
- Handler flow: calls `MRPortalApi.getActiveTeleportHandler()`, then `canStartTeleport(context)`, commits payment, then `startTeleport(context)`.

## Payment/cooldown preservation

- Staff pearls are consumed exactly once in the service commit step.
- Staff cooldown is applied exactly once in the service commit step.
- Scroll is consumed exactly once in the service commit step.
- Infinite/creative does not consume resources or apply cooldown.
- `DefaultPortalTeleportHandler` remains resource-free and contains no item/payment/cooldown operations.

## Compatibility notes

- Quick favorite unchanged.
- Preview spark success cleanup preserved.
- Client rendering unchanged.
- Item use behavior unchanged.
- No Chrono checks.
- No addon dependency.

## Validation

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- Grep checks:
  - `TeleportRequestService` appears in `MRPortalNetworking.java` for GUI routing.
  - `handleGuiTeleportRequest` appears in source.
  - `queueQuickFavoriteRequest` does not appear in `MRPortalNetworking.java`.
  - `QUICK_TELEPORT_BY_KEYBIND_C2S` remains in `MRPortalNetworking.java`.
  - `queueFavoriteTeleport` remains in `MRPortalNetworking.java`.
  - `mr_portal_chrono`: no matches.
  - `chrono_portal`: no matches.
  - `FabricLoader.*isModLoaded`: no matches.
- Payment duplication check:
  - `DefaultPortalTeleportHandler.java` contains no `consumeEnderPearls`, `addCooldown`, `shrink`, `findPortalStaff`, or `findTeleportScroll`.
  - `TeleportRequestService.java` contains the expected `consumeEnderPearls`, `addCooldown`, and `shrink` payment operations.

## Manual validation

Pending. Required tests:

- Regular staff same-dimension teleport: pearl cost once, cooldown once, portal opens/teleports, preview disappears, screen closes.
- Not enough pearls: no teleport, no cooldown, no partial consumption, message appears.
- Cooldown: immediate second staff use blocked.
- Scroll teleport: one scroll consumed, no pearls, no staff cooldown, portal opens/teleports.
- Infinite/creative: no pearl cost, cross-dimension behavior matches old behavior.
- Same-dimension restriction: non-creative/non-infinite cross-dimension blocked.
- Quick favorite smoke: old behavior still works and is not service-routed.

## Known risks

- This is the first gameplay routing phase.
- Manual validation is required after build/static validation.
- Quick favorite routing remains future work.
- Addon handler transaction semantics still need careful validation.

## Next recommended phase

PHASE_MRP_API_07B_GUI_TELEPORT_MANUAL_VALIDATION
