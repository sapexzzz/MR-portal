# PHASE_MRP_API_04_TELEPORT_REQUEST_SERVICE_DESIGN - TELEPORT_REQUEST_SERVICE_DESIGN

## Status

STATUS: PASS_DESIGN_ONLY

## Goal

Design the internal `TeleportRequestService` architecture before changing teleport routing. This phase is design-only and does not implement the service, route packets through the API, or change gameplay behavior.

## Baseline

- Root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `2.3`
- HEAD: unavailable (`fatal: Needed a single revision`)
- Remote:
  - `origin https://github.com/sqwiziiy/MR-Portal (fetch)`
  - `origin https://github.com/sqwiziiy/MR-Portal (push)`
- Git status summary: repository still has no usable committed baseline and project files are untracked.
- Gradle runtime note: use `JAVA_HOME=/usr/lib/jvm/java-21-openjdk` for Gradle.
- Source behavior changes: none in this phase.

## Files inspected

- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalApi.java`
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`
- `src/main/java/com/mentality/mrportal/config/MRPortalConfig.java`
- `src/main/java/com/mentality/mrportal/waypoint/WaypointData.java`
- `src/main/java/com/mentality/mrportal/waypoint/ServerWaypointStore.java`

## Current ownership map

- Networking:
  - Owns packet ids and server receivers.
  - `TELEPORT_REQUEST_C2S` reads waypoint id and `useScroll`, looks up the waypoint, enforces GUI same-dimension restrictions, calls `PendingTeleportManager.beginScrollTeleport` or `beginTeleport`, removes the normal preview spark, and closes the screen on success.
  - `QUICK_TELEPORT_BY_KEYBIND_C2S` looks up favorite waypoint, checks active session, chooses preferred item, enforces same-dimension restrictions, then queues favorite teleport.
  - Owns normal preview spark broadcast/removal and quick favorite preview spark broadcast/removal helpers.
- `PendingTeleportManager`:
  - Owns active session storage, pending quick favorite delay storage, portal ticking, owner/entity teleport, portal create/close packets, and portal geometry calculations.
  - Also currently owns payment/cooldown/item lookup logic that should move out of session execution later.
  - Current payment and cooldown happen immediately before `startSession` for GUI staff and at delayed activation time for quick favorite.
- Items:
  - `PortalStaffItem.use` and `TeleportScrollItem.use` open the waypoint screen.
  - `MRPortalItems` owns item registration and item classification helpers.
  - Staff tooltip reads cooldown client-side.
- Config:
  - `cooldownSeconds`, `portalPearlCost`, `quickFavoritePortalDelayTicks`, and portal timing/geometry fields drive current teleport behavior.
- Client preview:
  - Client receives preview/portal packets through existing networking and rendering classes.
  - Preview spark state is currently server-side in `MRPortalNetworking`.
- API/default handler:
  - `MRPortalApi` stores handlers and selects the active handler.
  - `DefaultPortalTeleportHandler` is registered with id `mr_portal:default_portal`, priority `0`, and default preview spark support.
  - Gameplay does not call the API yet.

## Proposed TeleportRequestService ownership

Create `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java` in a later phase. It should own server-side request orchestration, not portal rendering/ticking.

Recommended ownership:

- GUI waypoint request validation.
- Quick favorite request validation.
- Favorite waypoint lookup by player.
- Waypoint id lookup by player.
- Creative/infinite/staff/scroll activation classification.
- Same-dimension rule.
- Active session and pending activation checks.
- Payment precheck and payment commit orchestration.
- Scroll consumption.
- Pearl consumption.
- Cooldown check and application.
- `MRPortalTeleportContext` creation.
- Active handler lookup through `MRPortalApi`.
- `handler.canStartTeleport(context)` call before payment commit.
- `handler.startTeleport(context)` call after payment commit.
- Success/failure result to networking.
- Delayed quick favorite activation handoff while preserving payment at activation time.

## PendingTeleportManager remaining ownership

`PendingTeleportManager` should remain the default portal session executor:

- `PortalSession` storage.
- Session tick lifecycle.
- Portal create/close packet dispatch through `MRPortalNetworking`.
- Owner teleport through source portal.
- Other entity teleport through source portal.
- Portal geometry calculations unless later extracted as pure helpers.
- Destination/source portal visual lifecycle.
- Pending quick favorite delay storage can remain temporarily during the first routing step, but the eventual target should be moving request validation/payment for delayed activation into `TeleportRequestService`.

## Extraction candidates

- `findPortalStaff`
- `findTeleportScroll`
- `findPreferredTeleportItem`
- `countEnderPearls`
- `consumeEnderPearls`
- `getRequiredPearls`
- staff cooldown check/application
- scroll consumption
- same-dimension validation currently split between networking and pending manager
- active session and pending quick favorite checks
- `startSession` should be split into a paid/default execution method that assumes validation and payment are already handled
- quick favorite queue vs delayed activation payment should be separated so queueing stays cheap and activation revalidates.

## Required internal helper/data types

Minimal recommended set:

- `TeleportRequestResult`: success/failure plus optional player-facing message already sent flag.
- `TeleportRequestFailureReason`: enum for active session, missing waypoint, same dimension only, missing item, cooldown active, not enough pearls, handler rejected, handler failed.
- `TeleportPaymentPlan`: item stack, pearl cost, cooldown item/ticks, scroll stack, and bypass flags.
- `TeleportActivationInfo`: activator enum, `creativeView`, `useScroll`, `quickTeleport`, `infinite`, source position, yaw, and activation tick context.
- `WaypointDataView`: package-private adapter implementing `MRPortalWaypointView` over `WaypointData`.

Avoid public over-expansion until routing proves the exact needs.

## Default handler execution plan

Safest route:

1. Add a package-private paid-session method to `PendingTeleportManager`, for example `startPaidTeleportSession(ServerPlayer, WaypointData, boolean infinite, Vec3 sourcePos, float yaw, long createdTick)`.
2. Keep it strictly session-only: no item lookup, no pearl/scroll consumption, no cooldown logic.
3. Make `DefaultPortalTeleportHandler.startTeleport(context)` call into the paid-session method only after `TeleportRequestService` supplies an internal context/adaptor that can access `WaypointData`.
4. Do not expose mutable `WaypointData` publicly to addons.

The safest context strategy is a package-private internal implementation of `MRPortalWaypointView` that wraps `WaypointData`, plus an internal-only accessor in a package-private request object used by the default handler. Do not add public `internalWaypoint()` to `MRPortalTeleportContext` unless later implementation proves no cleaner package-private bridge works.

## Payment/cooldown transaction plan

Normal GUI staff/infinite ordering should be:

1. Read waypoint id from packet.
2. Look up waypoint for player.
3. Resolve `creativeView`, activator, `useScroll`, and quick flag.
4. Enforce same-dimension rule for non-creative/non-infinite.
5. Check active session.
6. Build payment plan without consuming resources.
7. Check item presence, cooldown, and pearl availability.
8. Build `MRPortalTeleportContext`.
9. Get active handler.
10. Call `handler.canStartTeleport(context)` without consuming resources.
11. Reserve/commit payment immediately before execution.
12. Call `handler.startTeleport(context)`.
13. If handler returns success, remove preview spark and close screen.
14. If handler returns failure, avoid charging the player where possible.

Transaction recommendation:

- For the default handler path, call `startTeleport` first only if it performs no delayed external work and can synchronously return true/false after session creation.
- Commit payment immediately before `startTeleport` only when the selected handler is known to synchronously accept the request.
- Because addon handlers may schedule delayed work, introduce a handler contract clarification in a later phase: `startTeleport` must only return true after it has accepted responsibility for execution. Payment is committed before that call for accepted base-validated requests, so a handler returning false should not happen after resource commit.
- To avoid cost loss on handler failure, the service should call `canStartTeleport`, then either commit payment and require `startTeleport` true, or roll back only simple scroll/pearl changes if `startTeleport` returns false. The preferred initial implementation is to route only the default handler first, where session start is synchronous and failure is known before external effects.

## Scroll transaction plan

Preserve current behavior:

- GUI scroll consumes exactly one scroll immediately before session start.
- Quick favorite scroll consumes exactly one scroll at delayed activation, not keypress.
- Scroll path does not use pearl cost or staff cooldown.
- If scroll is missing at activation time, fail with the current favorite item required message and do not start a session.

In the service, scroll should be represented as a payment plan with one scroll stack. Consume it only after `canStartTeleport` accepts and immediately before the handler starts the default session.

## Quick favorite preservation plan

Current behavior to preserve:

1. Keypress gets favorite waypoint.
2. Keypress checks active session.
3. Keypress finds preferred activator: infinite staff, regular staff, then scroll.
4. Keypress enforces same-dimension for non-infinite/non-creative.
5. Keypress queues preview/delay.
6. Activation rechecks player alive/dimension.
7. Activation removes quick preview spark.
8. Activation rechecks active session, same dimension, item presence, cooldown, and payment.
9. Activation consumes scroll or pearls and applies cooldown.
10. Activation starts the session.

Future service should expose two internal flows:

- `queueQuickFavoriteRequest(player)`: lookup, initial validation, source position/yaw capture, queue delay/preview.
- `activateQueuedQuickFavorite(player, activation)`: revalidation, payment/cooldown commit, context creation, handler execution.

If delay storage remains in `PendingTeleportManager` temporarily, its activation step should call back into `TeleportRequestService.activateQueuedQuickFavorite(...)`.

## Preview spark hook plan

`usesDefaultPreviewSpark()` should be consulted in these places after routing is introduced:

- Screen open: if active handler uses default preview spark, keep current `broadcastPreviewSpark`; otherwise skip default spark and let addon visuals handle preview.
- Screen close: remove default preview spark only if it was created.
- Successful GUI start: remove default preview spark only if it was created.
- Quick favorite queue: if active handler uses default preview spark, keep `broadcastQuickPreviewSpark`; otherwise skip default quick spark.
- Quick favorite abort/activation: remove default quick spark only if it was created.

The service should track whether a default preview spark was emitted for the request so removal is not mismatched.

## API/context adjustment recommendations

- `internalWaypoint()`: no public method recommended now. Prefer a package-private adapter/bridge for default handler execution.
- Source position: likely yes, add to internal activation info first; add to public context only if addon execution needs it.
- Yaw: likely yes, add to internal activation info first; add to public context only if addon execution needs portal orientation.
- Item activator: current `MRPortalTeleportActivator` is enough publicly; keep actual `ItemStack` internal.
- Delayed flag: current `quickTeleport` boolean is enough.
- Payment info: `pearlCost` and `cooldownTicks` are enough publicly; keep mutable stacks and payment commit internal.

## Risks

- Double pearl consumption if payment remains in both service and `PendingTeleportManager`.
- Missing scroll consumption if scroll handling is split incorrectly.
- Cooldown applied before a failed handler accepts execution.
- Quick favorite charging at keypress instead of activation.
- Quick favorite bypassing API while GUI uses API.
- Stuck preview spark if `usesDefaultPreviewSpark` is not tracked per request.
- Loss of same-dimension rule for non-infinite/non-creative teleports.
- Addon handler receiving mutable internal waypoint state.
- Default behavior changing before addon behavior is introduced.
- Client/server source-set leaks if client visual classes are referenced from common server code.

## Proposed next implementation phases

- `PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON`
  - Files likely touched: new `TeleportRequestService`, small internal result/helper types, possibly no routing changes.
  - Risk: low.
  - Validation: compile, grep no Chrono checks, ensure packet routing unchanged.
- `PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START`
  - Files likely touched: `PendingTeleportManager`, `DefaultPortalTeleportHandler`, internal waypoint adapter.
  - Risk: medium.
  - Validation: compile, diff exact extraction, verify no payment/cooldown in paid session method.
- `PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE`
  - Files likely touched: `MRPortalNetworking`, `TeleportRequestService`, possibly preview handling.
  - Risk: high.
  - Validation: GUI staff, infinite, creative, scroll, same-dimension, cooldown, pearl, screen close, preview removal.
- `PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE`
  - Files likely touched: `MRPortalNetworking`, `PendingTeleportManager`, `TeleportRequestService`.
  - Risk: high.
  - Validation: favorite missing, active session, initial validation, delayed activation revalidation, scroll/pearl/cooldown at activation, preview cleanup.
- `PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK`
  - Files likely touched: `MRPortalNetworking`, `TeleportRequestService`, possibly API docs.
  - Risk: medium.
  - Validation: default spark unchanged, handler opt-out prevents default spark, no stuck spark.
- `PHASE_MRP_API_10_NO_ADDON_REGRESSION_VALIDATION`
  - Files likely touched: tests/docs only unless issues found.
  - Risk: medium.
  - Validation: compile, manual game paths, API handler registration with higher priority, no Chrono hardcoding.

## Validation performed

- `pwd`: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- `git status --short --untracked-files=all || true`: repo-wide untracked baseline remains.
- `git branch --show-current || true`: `2.3`
- `git rev-parse --short HEAD || true`: unavailable (`fatal: Needed a single revision`)
- `git remote -v || true`: `origin https://github.com/sqwiziiy/MR-Portal`
- Required source inspections completed.
- Required grep mapping completed.
- `grep -R "TeleportRequestService" -n src/main/java || true`: no matches before this design phase.

Compile validation was run after docs updates:

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS

## Manual validation

Not applicable, no gameplay code changed.

## Next recommended phase

PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON
