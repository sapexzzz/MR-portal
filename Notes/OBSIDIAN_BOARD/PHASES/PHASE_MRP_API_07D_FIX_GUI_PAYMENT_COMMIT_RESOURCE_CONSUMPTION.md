# PHASE_MRP_API_07D_FIX_GUI_PAYMENT_COMMIT_RESOURCE_CONSUMPTION

## Goal
Fix the remaining GUI payment commit issue where runtime validation showed cooldown applying but item resources not visibly decreasing after GUI teleport.

## Bug Summary
- Portal Staff cooldown now works after 07C.
- Ender pearls still did not visibly decrease after Portal Staff GUI teleport.
- Teleport Scroll still did not visibly decrease after scroll GUI teleport.
- Quick favorite remains OK and old-path.

## Root Cause
The service was mutating live stacks and did not copy payment `ItemStack`s, but the GUI-routed payment commit did not force an inventory/menu sync after server-side stack mutation before the custom waypoint screen close.

## Fix Summary
- `TeleportRequestService.commitPayment(...)` now synchronizes inventory after `scrollStack.shrink(1)`.
- `TeleportRequestService.commitPayment(...)` now synchronizes inventory after successful `PendingTeleportManager.consumeEnderPearls(...)`.
- Cooldown application remains unchanged.
- Default handler remains payment-free.
- Quick favorite remains unchanged and is not routed through `TeleportRequestService`.

## Validation Status
- `compileJava`: PASS.
- `compileClientJava`: PASS.
- Static grep checks: PASS.
- Handler payment-free check: PASS.
- Manual gameplay validation: pending.

## Report
`Notes/REPORTS&AUDITS/PHASE_MRP_API_07D_FIX_GUI_PAYMENT_COMMIT_RESOURCE_CONSUMPTION_REPORT.md`

## Next Phase
`PHASE_MRP_API_07E_GUI_PAYMENT_COMMIT_MANUAL_VALIDATION`
