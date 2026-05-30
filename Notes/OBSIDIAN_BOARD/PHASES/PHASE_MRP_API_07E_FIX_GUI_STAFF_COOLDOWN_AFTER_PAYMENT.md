# PHASE_MRP_API_07E_FIX_GUI_STAFF_COOLDOWN_AFTER_PAYMENT

## Goal
Fix the remaining regular Portal Staff cooldown regression in the GUI-routed `TeleportRequestService` path.

## Bug Summary
- Runtime validation after 07D showed regular staff pearls are consumed.
- Teleport Scroll payment works and still has no cooldown, which is expected.
- Regular staff cooldown did not appear or persist.

## Root Cause
The GUI service had diverged from the old `PendingTeleportManager.beginTeleport(...)` transaction order. After 07D, regular staff committed pearls, synchronized inventory/menu state, then applied cooldown. The old path consumed pearls, applied cooldown, then started the session.

## Fix Summary
- Regular staff now consumes pearls, applies cooldown, then synchronizes inventory/menu state.
- Cooldown target remains the actual staff item from `paymentPlan.staffStack().getItem()`.
- Cooldown ticks remain `MRPortalConfig.cooldownSeconds * 20`.
- Scroll remains unchanged and receives no cooldown.
- Quick favorite remains old-path.
- Default handler remains payment-free.

## Validation Status
- `compileJava`: PASS.
- `compileClientJava`: PASS.
- Static grep checks: PASS.
- Handler payment-free check: PASS.
- Manual gameplay validation: pending.

## Report
`Notes/REPORTS&AUDITS/PHASE_MRP_API_07E_FIX_GUI_STAFF_COOLDOWN_AFTER_PAYMENT_REPORT.md`

## Next Phase
`PHASE_MRP_API_07F_GUI_STAFF_COOLDOWN_MANUAL_VALIDATION`
