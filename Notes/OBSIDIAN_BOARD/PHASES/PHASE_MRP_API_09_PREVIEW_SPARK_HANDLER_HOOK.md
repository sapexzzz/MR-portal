# PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK

## Goal

Implement `MRPortalTeleportHandler.usesDefaultPreviewSpark()` routing so addon handlers can opt out of base MR-Portal default preview sparks without changing payment or teleport execution.

## Hook Summary

- `MRPortalNetworking` now consults the active handler through `MRPortalApi.getActiveTeleportHandler()`.
- A missing active handler falls back to default preview behavior.
- GUI default preview creation is skipped when the active handler opts out.
- Quick favorite default preview creation is skipped when the active handler opts out.
- `DefaultPortalTeleportHandler` continues to use default preview sparks.

## Tracking Summary

- GUI server preview removal is gated by the existing recorded preview maps.
- GUI client-local preview is controlled by a new `OPEN_SCREEN_S2C` boolean and a `WaypointScreen` flag that hides only previews it showed.
- Quick favorite pending activations record whether the default preview spark was emitted.
- Quick favorite abort and activation remove preview only when that flag is true.

## Validation Status

- `compileJava`: PASS.
- `compileClientJava`: PASS.
- Static grep checks: PASS.
- Manual validation: pending.

## Report

`Notes/REPORTS&AUDITS/PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK_REPORT.md`

## Next Phase

PHASE_MRP_API_09B_PREVIEW_SPARK_HOOK_MANUAL_VALIDATION
