# PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE - ROUTE_QUICK_FAVORITE_THROUGH_SERVICE

## Status
STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Goal
Route quick favorite teleport requests through `TeleportRequestService` and the active `MRPortalTeleportHandler` while preserving the old keypress queue, delayed activation, payment timing, preview, and same-dimension behavior.

## Baseline
- root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- branch: `2.3`
- HEAD: unavailable, `fatal: Needed a single revision`
- remote: `origin https://github.com/sqwiziiy/MR-Portal`
- previous phase status: `STATUS: PASS_MANUAL_VALIDATED_DIAGNOSTICS_REMOVED`
- Gradle runtime: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`

## Files inspected
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportActivationInfo.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestResult.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestFailureReason.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java`
- item/config/waypoint support files

## Files changed
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_08_ROUTE_QUICK_FAVORITE_THROUGH_SERVICE.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Routing summary
- Before: `QUICK_TELEPORT_BY_KEYBIND_C2S` performed favorite lookup, activator selection, same-dimension validation, and `PendingTeleportManager.queueFavoriteTeleport(...)` directly.
- After: `QUICK_TELEPORT_BY_KEYBIND_C2S` calls `TeleportRequestService.queueQuickFavoriteRequest(server, player)`.
- Queue stage: service performs initial validation and calls the existing manager queue/timer storage. No payment is consumed at keypress.
- Delayed activation stage: manager tick removes the quick preview and calls `TeleportRequestService.activateQueuedQuickFavorite(...)`.
- GUI route is unchanged: `TELEPORT_REQUEST_C2S` still calls `handleGuiTeleportRequest(...)`.

## Quick favorite behavior preservation
- Favorite lookup remains server-side through `ServerWaypointStore`.
- Preferred activator order still uses `PendingTeleportManager.findPreferredTeleportItem(...)`: infinite staff, regular staff, teleport scroll.
- Queue delay still uses `MRPortalConfig.quickFavoritePortalDelayTicks` through the existing manager queue.
- Preview behavior still uses `broadcastQuickPreviewSpark` and `removeQuickPreviewSpark`.
- Activation revalidates active session, same-dimension rules, selected item availability, cooldown, and resources.
- Regular staff pearls are consumed at activation only.
- Regular staff cooldown is applied at activation only.
- Teleport Scroll is consumed at activation only.
- Creative/infinite paths have no payment.
- The default handler receives the captured source position/yaw so quick favorite keeps the old keypress portal origin semantics.

## Payment/cooldown ownership
- Quick favorite payment/cooldown is now owned by `TeleportRequestService`.
- `DefaultPortalTeleportHandler` remains payment-free.
- `PendingTeleportManager` keeps pending quick favorite queue storage, delay ticking, preview removal timing, and default portal session execution.
- `PendingTeleportManager` still contains legacy direct begin methods and shared item/payment helpers.

## Compatibility notes
- GUI route unchanged.
- No Chrono checks.
- No addon dependency.
- No `[MRP07F]` diagnostics.
- No persistent cooldown added.
- Preview spark handler hook remains a future phase.

## Validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS
- `[MRP07F]` grep: no matches
- `TeleportRequestService` grep in `MRPortalNetworking`: GUI and quick favorite service calls present
- `queueQuickFavoriteRequest` grep: networking and service present
- `activateQueuedQuickFavorite` grep: manager and service present
- `activatePendingFavoriteTeleport` grep: no matches
- `QUICK_TELEPORT_BY_KEYBIND_C2S` grep: receiver still present
- `queueFavoriteTeleport` grep: manager queue storage and service queue call present
- `mr_portal_chrono` grep: no matches
- `chrono_portal` grep: no matches
- `FabricLoader.*isModLoaded` grep: no matches
- handler payment-free check: no matches in `DefaultPortalTeleportHandler`

## Manual validation
Manual runtime validation was not run in this phase.

Pending tests:
- Quick favorite with regular staff.
- Quick favorite with not enough pearls.
- Quick favorite cooldown block.
- Quick favorite with scroll.
- Quick favorite with infinite/creative.
- Same-dimension restriction.
- GUI staff/scroll regression smoke.

## Known risks
- Quick favorite manual validation is pending.
- Addon handler transaction semantics still need validation in later phases.
- Preview spark handler hook remains future work.

## Next recommended phase
PHASE_MRP_API_08B_QUICK_FAVORITE_MANUAL_VALIDATION
