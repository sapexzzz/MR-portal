# PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE

## Goal

Route normal GUI waypoint teleport requests through `TeleportRequestService` and the active `MRPortalTeleportHandler`.

## Routing Summary

- `TELEPORT_REQUEST_C2S` now calls `TeleportRequestService.handleGuiTeleportRequest(...)`.
- On successful service result, networking removes the preview spark and closes the screen.
- Failed service results leave the screen and preview behavior aligned with the old failure paths.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` remains on the old direct flow.

## Payment Preservation

- Regular staff path checks staff, cooldown, and pearl count before payment.
- Regular staff path consumes pearls once and applies cooldown once immediately before handler execution.
- Scroll path consumes one scroll once immediately before handler execution.
- Infinite/creative paths do not consume pearls, scrolls, or cooldown.
- `DefaultPortalTeleportHandler` remains payment-free.

## Validation Status

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS
- Static grep checks passed.

## Manual Validation Status

Pending. This is the first gameplay routing phase and needs in-game validation.

## Report

`Notes/REPORTS&AUDITS/PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE_REPORT.md`

## Next Phase

PHASE_MRP_API_07B_GUI_TELEPORT_MANUAL_VALIDATION
