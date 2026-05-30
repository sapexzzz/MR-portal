# PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE

## Quick Favorite Route Summary
`QUICK_TELEPORT_BY_KEYBIND_C2S` now routes into `TeleportRequestService.queueQuickFavoriteRequest(...)`.

## Queue/Activation Split
- Queue stage validates favorite, active session, preferred activator, and same-dimension eligibility.
- Queue stage does not consume pearls, consume scrolls, or apply cooldown.
- Existing `PendingTeleportManager` queue storage and delay ticking remain.
- Delayed activation delegates to `TeleportRequestService.activateQueuedQuickFavorite(...)`.

## Payment Ownership
Quick favorite payment/cooldown now belongs to `TeleportRequestService` at activation time.

`PendingTeleportManager` remains responsible for queue storage/ticking, preview removal timing, portal sessions, portal packets, and entity teleport.

## Validation Status
- `compileJava`: PASS
- `compileClientJava`: PASS
- Static routing checks: PASS
- Manual runtime validation: PENDING

## Report
`Notes/REPORTS&AUDITS/PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE_REPORT.md`

## Next Phase
PHASE_MRP_API_08B_QUICK_FAVORITE_MANUAL_VALIDATION
