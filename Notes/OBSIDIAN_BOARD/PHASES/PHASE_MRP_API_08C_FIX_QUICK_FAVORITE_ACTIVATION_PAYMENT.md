# PHASE_MRP_API_08C_FIX_QUICK_FAVORITE_ACTIVATION_PAYMENT

## Phase ID
PHASE_MRP_API_08C_FIX_QUICK_FAVORITE_ACTIVATION_PAYMENT

## Bug Summary
Quick favorite regular staff activation did not consume ender pearls at delayed activation.

## Root Cause
The queue stage stored scroll mode whenever the selected item check saw a scroll, instead of storing scroll mode only when scroll was the actual preferred activator. Regular staff should win over scroll.

## Fix Summary
`TeleportRequestService.queueQuickFavoriteRequest(...)` now queues scroll mode only when the selected activator is scroll after preference order:
- not infinite/creative
- not regular staff
- scroll selected

Payment still happens only at delayed activation.

## Validation Status
- `compileJava`: PASS
- `compileClientJava`: PASS
- Static grep checks: PASS
- Manual runtime validation: PENDING

## Report
`Notes/REPORTS&AUDITS/PHASE_MRP_API_08C_FIX_QUICK_FAVORITE_ACTIVATION_PAYMENT_REPORT.md`

## Next Phase
PHASE_MRP_API_08D_QUICK_FAVORITE_PAYMENT_MANUAL_VALIDATION
