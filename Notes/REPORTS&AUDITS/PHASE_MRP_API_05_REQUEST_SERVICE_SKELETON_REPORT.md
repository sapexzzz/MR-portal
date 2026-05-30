# PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON - REQUEST_SERVICE_SKELETON

## Status

STATUS: PASS_SKELETON_ADDED

## Goal

Add an internal `TeleportRequestService` skeleton and minimal helper/data types needed for later routing phases, without changing current gameplay routing or behavior.

## Baseline

- Root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `2.3`
- HEAD: unavailable (`fatal: Needed a single revision`)
- Remote:
  - `origin https://github.com/sqwiziiy/MR-Portal (fetch)`
  - `origin https://github.com/sqwiziiy/MR-Portal (push)`
- Git status summary: repository still has no usable committed baseline and project files are untracked.
- Gradle runtime note: use `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`.
- Previous design phase: `PHASE_MRP_API_04_TELEPORT_REQUEST_SERVICE_DESIGN`.

## Files inspected

- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalWaypointView.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalApi.java`
- `src/main/java/com/mentality/mrportal/waypoint/WaypointData.java`
- `src/main/java/com/mentality/mrportal/waypoint/ServerWaypointStore.java`
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`
- `src/main/java/com/mentality/mrportal/config/MRPortalConfig.java`

## Pre-edit audit

- Existing portal package files before this phase:
  - `DefaultPortalTeleportHandler.java`
  - `PendingTeleportManager.java`
  - `PortalSession.java`
- `TeleportRequestService`: absent before implementation.
- `TeleportRequestResult`: absent before implementation.
- `TeleportPaymentPlan`: absent before implementation.
- `WaypointDataView`: absent before implementation.
- `MRPortalNetworking` still directly calls `PendingTeleportManager.beginTeleport`, `beginScrollTeleport`, and `queueFavoriteTeleport`.
- `PendingTeleportManager` still owns current payment, cooldown, scroll consumption, quick favorite activation, and session execution.

## Files changed

- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestResult.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestFailureReason.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportActivationInfo.java`
- `src/main/java/com/mentality/mrportal/portal/WaypointDataView.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_05_REQUEST_SERVICE_SKELETON.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Skeleton summary

- `TeleportRequestService`
  - Access: `public final`.
  - Purpose: internal server-side orchestrator for future teleport requests.
  - Methods: `handleGuiTeleportRequest(...)`, `queueQuickFavoriteRequest(...)`, package-private `buildContext(...)`.
  - Current behavior: returns `NOT_IMPLEMENTED` for non-null stub calls and `INVALID_PLAYER` for null player.
  - No gameplay behavior changes because it is not called by networking or manager code.
- `TeleportRequestResult`
  - Access: `public record`.
  - Fields: `successful`, `failureReason`, `messageSent`.
  - Factories: `success()`, `failed(reason)`, `failed(reason, messageSent)`.
  - Note: component is named `successful` because Java records already generate an accessor matching component names and cannot also declare a static `success()` method with the same signature.
- `TeleportRequestFailureReason`
  - Access: `public enum`.
  - Values include `NONE`, `NOT_IMPLEMENTED`, waypoint/session/item/payment/handler failures, `INVALID_PLAYER`, and `MISSING_DESTINATION_LEVEL`.
- `TeleportPaymentPlan`
  - Access: package-private record.
  - Fields: activator, infinite flag, creative bypass, pearl cost, cooldown ticks, staff stack, scroll stack.
  - Does not consume payment or apply cooldown.
- `TeleportActivationInfo`
  - Access: package-private record.
  - Fields: player, waypoint, activator, creative/useScroll/quick/infinite flags, source position, yaw, request tick.
  - Internal only; may contain `WaypointData`.
- `WaypointDataView`
  - Access: package-private final class.
  - Purpose: read-only adapter from internal `WaypointData` to public `MRPortalWaypointView`.
  - No mutation, NBT, or store access.

## Ownership boundaries

- Service skeleton does not consume payment.
- Service skeleton does not apply cooldown.
- Service skeleton does not consume scrolls.
- Service skeleton does not start sessions.
- Service skeleton does not remove or create preview sparks.
- Service skeleton is not called by networking yet.
- `PendingTeleportManager` remains the current executor.

## Compatibility notes

- `TELEPORT_REQUEST_C2S` unchanged.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` unchanged.
- `PendingTeleportManager` unchanged.
- `DefaultPortalTeleportHandler` unchanged.
- Item behavior unchanged.
- Preview spark behavior unchanged.
- Client rendering unchanged.
- No Chrono hardcoded checks.
- No addon dependency.

## Validation

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: initially failed due to Java record naming conflict, then PASS after renaming the result component from `success` to `successful`.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- Required file checks: PASS for all six skeleton files.
- Report/board checks: PASS.
- Canvas JSON validation: PASS.
- Grep checks:
  - `mr_portal_chrono`: no matches.
  - `chrono_portal`: no matches.
  - `FabricLoader.*isModLoaded`: no matches.
  - `TeleportRequestService`: only new service/docs references in source.
  - `handleGuiTeleportRequest`: new service stub only.
  - `queueQuickFavoriteRequest`: new service stub only.
- Routing unchanged checks:
  - `TeleportRequestService` does not appear in `MRPortalNetworking.java`.
  - `getActiveTeleportHandler` does not appear in `MRPortalNetworking.java`.
- Sensitive diff checks:
  - `git diff -- src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`: no output.
  - `git diff -- src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`: no output.
  - `git diff -- src/client/java`: no output.

## Manual validation

Manual game validation not required because service skeleton is not routed.

## Known risks

- Skeleton is not used yet.
- Later extraction/payment phases remain high-risk.
- Mutable `ItemStack` fields in planned helper data types must remain internal only.
- Repository has no usable HEAD and most files are untracked, so `git diff` is limited for new-file visibility.

## Next recommended phase

PHASE_MRP_API_06_EXTRACT_PAID_SESSION_START
