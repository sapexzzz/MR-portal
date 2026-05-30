# PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON

## Goal

Add an internal `TeleportRequestService` skeleton and minimal helper/data types without changing gameplay routing.

## New Skeleton Classes

- `TeleportRequestService`: public internal orchestrator class with unrouted stubs for future GUI and quick favorite requests.
- `TeleportRequestResult`: public result record with success/failure/message state.
- `TeleportRequestFailureReason`: public failure enum for service outcomes.
- `TeleportPaymentPlan`: package-private internal payment/cooldown plan; does not consume resources.
- `TeleportActivationInfo`: package-private internal activation classification data.
- `WaypointDataView`: package-private read-only adapter from `WaypointData` to `MRPortalWaypointView`.

## Validation Result

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS

## Compatibility Result

- `TELEPORT_REQUEST_C2S` is not routed through the service.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` is not routed through the service.
- `PendingTeleportManager` behavior is unchanged.
- `DefaultPortalTeleportHandler` behavior is unchanged.
- No payment, cooldown, scroll consumption, preview spark, or session start behavior changed.

## Report

`Notes/REPORTS&AUDITS/PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON_REPORT.md`

## Next Phase

PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START
