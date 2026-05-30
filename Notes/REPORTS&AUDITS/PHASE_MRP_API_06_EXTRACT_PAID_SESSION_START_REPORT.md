# PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START - EXTRACT_PAID_SESSION_START

## Status

STATUS: PASS_PAID_SESSION_START_EXTRACTED

## Goal

Extract a session-only start path from `PendingTeleportManager` so future base-owned request routing and the built-in default handler can start the existing `PortalSession` behavior without duplicating payment, scroll consumption, item lookup, or cooldown logic.

## Baseline

- Root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `2.3`
- HEAD: unavailable (`fatal: Needed a single revision`)
- Remote:
  - `origin https://github.com/sqwiziiy/MR-Portal (fetch)`
  - `origin https://github.com/sqwiziiy/MR-Portal (push)`
- Git status summary: repository still has no usable committed baseline and project files are untracked.
- Java 21 Gradle note: use `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`.
- Previous skeleton phase: `PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON`.

## Files inspected

- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/portal/PortalSession.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportActivationInfo.java`
- `src/main/java/com/mentality/mrportal/portal/WaypointDataView.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalWaypointView.java`
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`

## Pre-edit audit

- `PendingTeleportManager.startSession(...)` is the existing private session creator.
- `beginTeleport(...)` performs active session check, staff lookup, cooldown check, pearl consumption, cooldown application, then calls `startSession(...)`.
- `beginScrollTeleport(...)` performs active session check, scroll lookup, scroll consumption, then calls `startSession(...)`.
- `activatePendingFavoriteTeleport(...)` performs delayed activation revalidation, scroll or pearl/cooldown payment, then calls `startSession(...)`.
- `MRPortalNetworking` still routes GUI and quick favorite packets directly to `PendingTeleportManager`.
- `DefaultPortalTeleportHandler.startTeleport(...)` returns `false` before implementation.
- `WaypointDataView` adapts `WaypointData` to the public `MRPortalWaypointView` but has no package-private `WaypointData` accessor yet.

## Files changed

- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/portal/WaypointDataView.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Paid session method

- Method: `public boolean startPaidTeleportSession(ServerPlayer player, WaypointData destination, boolean infinite)`
- Method: `public boolean startPaidTeleportSession(ServerPlayer player, WaypointData destination, boolean infinite, Vec3 sourcePos, float portalYaw)`
- Location: `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- Behavior: checks active session and calls the existing private `startSession(...)` method.
- Explicitly does not:
  - call `findPortalStaff`
  - call `findTeleportScroll`
  - consume ender pearls
  - consume scrolls
  - apply cooldown
  - check item cooldown
  - perform same-dimension payment rules
- Preservation:
  - `beginTeleport(...)` still performs active-session check, staff lookup, cooldown check, pearl consumption, and cooldown application before session start.
  - `beginScrollTeleport(...)` still performs active-session check, scroll lookup, and scroll shrink before session start.
  - quick favorite activation still performs delayed revalidation and consumes scroll/pearls/applies cooldown at activation time before session start.

## Default handler execution

- `DefaultPortalTeleportHandler.startTeleport(context)` now calls `PendingTeleportManager.get(context.server()).startPaidTeleportSession(...)`.
- Waypoint access uses the package-private `WaypointDataView.waypointData()` accessor.
- If `context.waypoint()` is not an internal `WaypointDataView`, the handler logs a warning and returns `false`.
- No public `internalWaypoint()` was added to `MRPortalTeleportContext`.
- Payment/cooldown is not duplicated because the handler does not inspect items, consume pearls, consume scrolls, or apply cooldown.

## Compatibility notes

- `TELEPORT_REQUEST_C2S` unchanged.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` unchanged.
- `TeleportRequestService` is not called by networking.
- Payment/cooldown still happen in existing current paths.
- Scroll consumption remains unchanged.
- Quick favorite activation timing remains unchanged.
- Preview spark behavior remains unchanged.
- No Chrono checks.
- No addon dependency.

## Validation

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- Grep checks:
  - `startPaidTeleportSession`: present in `PendingTeleportManager.java`, `DefaultPortalTeleportHandler.java`, and docs.
  - `TeleportRequestService` in `MRPortalNetworking.java`: no matches.
  - `getActiveTeleportHandler` in `MRPortalNetworking.java`: no matches.
  - `mr_portal_chrono`: no matches.
  - `chrono_portal`: no matches.
  - `FabricLoader.*isModLoaded`: no matches.
- Payment duplication check:
  - `DefaultPortalTeleportHandler.java` contains no `consumeEnderPearls`, `addCooldown`, `shrink`, `findPortalStaff`, or `findTeleportScroll`.
- Sensitive unchanged checks:
  - `MRPortalNetworking.java`: no `git diff` output.
  - `PortalStaffItem.java`: no `git diff` output.
  - `TeleportScrollItem.java`: no `git diff` output.
  - `ClientPortalEffectManager.java`: no `git diff` output.

## Manual validation

manual validation: not applicable, routing unchanged

## Known risks

- New paid method will not be routed yet.
- Default handler may become executable but still unused by current gameplay.
- Future service payment transaction remains high-risk.
- Repository has no usable HEAD and most files are untracked, so `git diff` is limited for new-file visibility.

## Next recommended phase

PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE
