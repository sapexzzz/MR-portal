# PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK - PREVIEW_SPARK_HANDLER_HOOK

## Status

STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Goal

Implement handler-aware default preview spark routing so the active `MRPortalTeleportHandler` can opt out of base MR-Portal waypoint and quick-favorite preview visuals through `usesDefaultPreviewSpark()`.

## Baseline

- Root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `2.3`
- HEAD: unavailable; `git rev-parse --short HEAD` reports `fatal: Needed a single revision`.
- Remote: `origin https://github.com/sqwiziiy/MR-Portal`
- Previous phase: `PHASE_MRP_API_08D_RECORD_QUICK_FAVORITE_MANUAL_PASS`
- Previous phase status: `STATUS: PASS_MANUAL_VALIDATED`
- Gradle runtime: Java 21 via `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`

## Files inspected

- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalApi.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/client/java/com/mentality/mrportal/client/render/ClientPortalEffectManager.java`
- `src/client/java/com/mentality/mrportal/client/screen/MRPortalClientNetworking.java`
- `src/client/java/com/mentality/mrportal/client/screen/WaypointScreen.java`

## Files changed

- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportHandler.java`
- `src/client/java/com/mentality/mrportal/client/screen/MRPortalClientNetworking.java`
- `src/client/java/com/mentality/mrportal/client/screen/WaypointScreen.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Audit findings

- GUI world preview has two parts: server-side `PREVIEW_SPARK_S2C` broadcasts for nearby players, and client-local preview created by `WaypointScreen.init()`.
- Quick favorite preview is server-side through `MRPortalNetworking.broadcastQuickPreviewSpark(...)`.
- Existing server preview maps already safely no-op when a remove is requested for a preview that was not emitted.
- Quick favorite pending activation did not track whether the quick preview was actually emitted, so the pending record needed an explicit flag.
- Payment logic is isolated in `TeleportRequestService.commitPayment(...)`; this phase did not change it.

## Hook summary

- `MRPortalNetworking.usesDefaultPreviewSpark()` now consults `MRPortalApi.getActiveTeleportHandler()`.
- If no active handler exists, the hook returns `true` to preserve base behavior.
- GUI open writes a `useDefaultPreviewSpark` boolean to `OPEN_SCREEN_S2C`.
- GUI server-side default preview broadcast happens only when the active handler uses the default preview spark.
- Client `WaypointScreen` shows and hides its local default preview only when the server says the default preview is enabled.
- Quick favorite queueing passes the hook result into `PendingTeleportManager.queueFavoriteTeleport(...)`.
- `DefaultPortalTeleportHandler` still explicitly returns `true`.
- `MRPortalTeleportHandler` Javadoc now documents that this hook controls only preview visuals, not payment/session execution.

## Tracking summary

- GUI server preview creation remains tracked by `activePreviewSparks` and `activePreviewDimensions`.
- GUI removal uses `removeDefaultPreviewSpark(...)`, which only sends removal packets when a default preview was recorded.
- GUI client-local preview tracks `defaultPreviewShown` in `WaypointScreen` and hides only when it was shown.
- Quick favorite pending activation now stores `defaultPreviewCreated`.
- Quick favorite abort and activation remove the quick preview only when `defaultPreviewCreated` is true.

## Compatibility notes

- GUI payment unchanged.
- Quick favorite payment unchanged.
- Default handler preview behavior remains enabled.
- No Chrono checks were added.
- No addon dependency was added.
- No `[MRP07F]` diagnostics were added.
- Handler priority behavior was not changed.
- Persistent cooldown was not added.

## Validation

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- `[MRP07F]` grep: PASS, no matches.
- `usesDefaultPreviewSpark` grep: PASS, consulted in preview paths.
- Preview creation/removal grep: PASS, GUI and quick preview paths present.
- Quick favorite routing grep: PASS, route remains service-based.
- GUI routing grep: PASS, route remains service-based.
- Chrono grep checks: PASS, no `mr_portal_chrono`, `chrono_portal`, or `FabricLoader.*isModLoaded` matches.
- Payment unchanged check in `DefaultPortalTeleportHandler`: PASS, no payment logic found.
- Manual validation: pending.

## Manual validation pending

- GUI staff open/close: preview appears and disappears on close.
- GUI staff successful teleport: preview disappears on teleport start, screen closes, staff payment still works.
- GUI scroll successful teleport: preview disappears on teleport start, scroll payment still works.
- Quick favorite regular staff: quick preview appears during delay, removes on activation, payment/cooldown still works.
- Quick favorite scroll: quick preview appears during delay, removes on activation, scroll payment still works.
- Addon opt-out: static-only in this phase; real handler validation remains future work.

## Known risks

- Addon opt-out needs real Chrono handler validation later.
- `OPEN_SCREEN_S2C` payload changed by one trailing boolean and both server/client were updated together.
- Preview cleanup edge cases still need manual runtime validation.

## Next recommended phase

PHASE_MRP_API_09B_PREVIEW_SPARK_HOOK_MANUAL_VALIDATION
