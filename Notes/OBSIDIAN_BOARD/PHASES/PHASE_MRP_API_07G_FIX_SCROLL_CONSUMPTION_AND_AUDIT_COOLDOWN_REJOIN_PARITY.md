# PHASE_MRP_API_07G_FIX_SCROLL_CONSUMPTION_AND_AUDIT_COOLDOWN_REJOIN_PARITY

## Goal
Fix the remaining GUI Teleport Scroll consumption regression and audit Portal Staff cooldown behavior across logout/rejoin.

## Scroll Bug Summary
GUI scroll activation mode was not server-owned. Scroll-opened waypoint screens could later send a non-scroll GUI teleport request if the client inferred `useScroll=false`, so the service would not reach the scroll shrink branch.

## Cooldown Rejoin Audit Classification
EXPECTED_LEGACY_BEHAVIOR

Backup audit found old Portal Staff cooldown used vanilla `player.getCooldowns().addCooldown(...)` only. No persistent cooldown save/restore path was found, so cooldown disappearing after rejoin is not treated as a routing regression.

## Fix Summary
- `TeleportScrollItem` opens waypoint GUI with server scroll mode enabled.
- `PortalStaffItem` opens waypoint GUI with server scroll mode disabled.
- Keybind-opened screens mark staff/infinite and scroll modes explicitly.
- Waypoint screen refreshes preserve the active mode.
- `TELEPORT_REQUEST_C2S` uses the server active screen mode when routing GUI requests through `TeleportRequestService`.
- Active mode is cleared on screen close and successful GUI teleport.
- Quick favorite remains unchanged.

## Validation Status
- `compileJava`: PASS
- `compileClientJava`: PASS
- Static routing/addon/payment checks: PASS
- Manual runtime validation: PENDING

## Report
`Notes/REPORTS&AUDITS/PHASE_MRP_API_07G_FIX_SCROLL_CONSUMPTION_AND_AUDIT_COOLDOWN_REJOIN_PARITY_REPORT.md`

## Next Phase
PHASE_MRP_API_07H_VALIDATE_SCROLL_FIX_AND_DIAGNOSTIC_CLEANUP
