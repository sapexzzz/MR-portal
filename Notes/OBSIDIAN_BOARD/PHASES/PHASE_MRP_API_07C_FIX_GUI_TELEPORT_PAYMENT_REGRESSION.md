# PHASE_MRP_API_07C_FIX_GUI_TELEPORT_PAYMENT_REGRESSION

## Goal
Fix the GUI teleport regression where regular Portal Staff requests started sessions through `TeleportRequestService` without preserving staff pearl payment and cooldown behavior.

## Bug Summary
- Regular Portal Staff GUI teleport did not consume ender pearls.
- Regular Portal Staff GUI teleport did not apply cooldown.
- One-time/single-use staff behavior was reported as not consuming the item.
- Teleport Scroll worked correctly.
- Quick favorite smoke looked OK and remained old-path.

## Root Cause
`TeleportRequestService` classified activation from the broad `creativeView` flag. That flag means the GUI has creative/infinite waypoint visibility, not necessarily that the actual request activator is free. When true for a non-creative player, the service treated the request as `INFINITE_STAFF` and built a free payment plan.

`DefaultPortalTeleportHandler` also used `context.creativeView()` as the session infinite flag, which kept the broad bypass downstream.

## Fix Summary
- Staff classification now uses the actual `ItemStack` found by `PendingTeleportManager.findPortalStaff(player)`.
- Creative bypass is limited to `player.getAbilities().instabuild`.
- Infinite bypass is limited to an actual infinite staff stack.
- Regular staff plans retain the same staff stack through precheck and payment commit.
- `DefaultPortalTeleportHandler` now treats only `INFINITE_STAFF` and `CREATIVE` activators as infinite sessions.
- No payment/cooldown logic was moved into handlers.
- Quick favorite remains unchanged and is not routed through `TeleportRequestService`.

## Validation Status
- `compileJava`: PASS.
- `compileClientJava`: PASS.
- Static grep checks: PASS.
- Handler payment duplication check: PASS.
- Manual gameplay validation: pending.

## Notes
The inspected base source and available backup did not contain regular staff shrink/damage/break behavior. Scroll shrink remains the only base item consumption path found besides ender pearl consumption.

## Report
`Notes/REPORTS&AUDITS/PHASE_MRP_API_07C_FIX_GUI_TELEPORT_PAYMENT_REGRESSION_REPORT.md`

## Next Phase
`PHASE_MRP_API_07D_GUI_TELEPORT_PAYMENT_FIX_MANUAL_VALIDATION`
