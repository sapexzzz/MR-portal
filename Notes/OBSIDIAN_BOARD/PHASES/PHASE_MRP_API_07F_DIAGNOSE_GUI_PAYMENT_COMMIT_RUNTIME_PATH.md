# PHASE_MRP_API_07F_DIAGNOSE_GUI_PAYMENT_COMMIT_RUNTIME_PATH

## Diagnostic Goal
Add temporary `[MRP07F]` runtime logs to the GUI-routed payment path so the next runtime pass can identify whether the payment commit path is skipped, misclassified, or mutating the wrong stack.

## Runtime Bug Summary
- Portal Staff GUI teleport starts but does not consume pearls.
- Portal Staff GUI teleport starts but does not apply cooldown.
- Teleport Scroll GUI teleport starts but does not consume the scroll.
- Scroll having no cooldown is expected.
- Quick favorite remains OK.

## Log Prefix
`[MRP07F]`

## Diagnostics Added
- `MRPortalNetworking` before and after `TeleportRequestService.handleGuiTeleportRequest(...)`.
- `TeleportRequestService` request entry, waypoint lookup, activator classification, payment plan, handler acceptance, payment commit, stack mutation, cooldown application, sync, and handler start result.
- Staff branch logs pearl counts before/after consumption and cooldown state before/after `addCooldown`.
- Scroll branch logs stack count before/after `shrink(1)`.

## Validation Status
- `compileJava`: PASS.
- `compileClientJava`: PASS.
- Static `[MRP07F]` grep: PASS.
- Quick favorite service route: absent.
- Runtime logs: pending.

## Report
`Notes/REPORTS&AUDITS/PHASE_MRP_API_07F_DIAGNOSE_GUI_PAYMENT_COMMIT_RUNTIME_PATH_REPORT.md`

## Next Phase
`PHASE_MRP_API_07G_CAPTURE_GUI_PAYMENT_DIAGNOSTIC_LOGS`
