# PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START

## Goal

Extract a paid/default portal session start path from `PendingTeleportManager` so future request-service routing and the default handler can start the existing `PortalSession` behavior without duplicating payment, scroll consumption, item lookup, or cooldown logic.

## Paid Session Method

- Method: `startPaidTeleportSession(ServerPlayer player, WaypointData destination, boolean infinite)`
- Method: `startPaidTeleportSession(ServerPlayer player, WaypointData destination, boolean infinite, Vec3 sourcePos, float portalYaw)`
- File: `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- Behavior: checks active session, then calls the existing private `startSession(...)` path.
- Exclusions: no staff lookup, no scroll lookup, no pearl consumption, no scroll consumption, no cooldown check, no cooldown application.

## Default Handler Execution Status

- `DefaultPortalTeleportHandler.startTeleport(context)` now starts the paid session path when `context.waypoint()` is the internal `WaypointDataView`.
- If the waypoint view is not internal, it logs a warning and returns `false`.
- `WaypointDataView` now has a package-private `waypointData()` accessor.
- No public `internalWaypoint()` was added to `MRPortalTeleportContext`.

## Compatibility Result

- `TELEPORT_REQUEST_C2S` routing unchanged.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` routing unchanged.
- `TeleportRequestService` is not called by networking.
- Current payment/cooldown/scroll logic remains in the existing current paths.
- Quick favorite delayed activation timing is unchanged.
- Preview spark behavior is unchanged.

## Validation Result

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS

## Report

`Notes/REPORTS&AUDITS/PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START_REPORT.md`

## Next Phase

PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE
