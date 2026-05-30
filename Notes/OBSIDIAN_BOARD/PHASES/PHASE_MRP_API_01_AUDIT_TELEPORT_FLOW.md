# PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW

## Short name
AUDIT_TELEPORT_FLOW

## Goal
Audit the current MR-Portal teleport flow before introducing any public teleport-handler API.

## Files inspected
- `MRPortal.java`
- `MRPortalClient.java`
- `MRPortalNetworking.java`
- `MRPortalClientNetworking.java`
- `PendingTeleportManager.java`
- `PortalSession.java`
- `MRPortalConfig.java`
- `MRPortalConfigManager.java`
- `MRPortalItems.java`
- `PortalStaffItem.java`
- `TeleportScrollItem.java`
- `WaypointData.java`
- `ServerWaypointStore.java`
- `WaypointScreen.java`
- `ClientPortalEffectManager.java`
- `DimensionUtil.java`
- build/config metadata files

## Teleport entry points found
- GUI waypoint teleport through regular portal staff.
- GUI waypoint teleport through infinite staff or creative view.
- GUI waypoint teleport through teleport scroll.
- Open waypoint GUI by keybind.
- Quick favorite teleport by keybind.

## Major risks
- Payment/cooldown must stay in base MR-Portal, not addon handlers.
- Quick favorite must later route through the same API path as GUI teleport.
- Scroll consumption must remain exactly once.
- Preview sparks must not stick after screen close, success, or delayed favorite abort.
- Default no-addon behavior must preserve current `PortalSession` lifecycle and entity teleport behavior.
- Server source must not reference client classes.

## Proposed next phase
PHASE_MRP_API_02_PUBLIC_HANDLER_API

## Report path
`MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW_REPORT.md`
