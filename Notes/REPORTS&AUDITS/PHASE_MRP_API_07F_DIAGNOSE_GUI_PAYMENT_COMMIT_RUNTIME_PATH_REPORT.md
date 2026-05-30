# PHASE_MRP_API_07F_DIAGNOSE_GUI_PAYMENT_COMMIT_RUNTIME_PATH - DIAGNOSE_GUI_PAYMENT_COMMIT_RUNTIME_PATH

## Status
STATUS: DIAGNOSTICS_ADDED_RUNTIME_LOGS_PENDING

## Goal
Add temporary targeted runtime diagnostics to the GUI-routed `TeleportRequestService` payment path to identify why GUI staff and scroll teleports start sessions while payment resource mutations are not observed at runtime.

## Runtime bug report from user
- Portal Staff GUI teleport starts but does not consume ender pearls.
- Portal Staff GUI teleport starts but does not apply cooldown.
- Teleport Scroll GUI teleport starts but does not consume the scroll.
- Teleport Scroll having no cooldown is expected.
- Quick favorite remains OK.

## Files inspected
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`
- `src/main/java/com/mentality/mrportal/config/MRPortalConfig.java`

## Audit findings before diagnostics
- `TELEPORT_REQUEST_C2S` calls `TeleportRequestService.handleGuiTeleportRequest(...)`.
- `queueQuickFavoriteRequest` is not called by `MRPortalNetworking`.
- `commitPayment(...)` exists only in `TeleportRequestService`.
- Staff payment path calls `PendingTeleportManager.consumeEnderPearls(...)` and `player.getCooldowns().addCooldown(...)`.
- Scroll payment path calls `paymentPlan.scrollStack().shrink(1)`.
- `DefaultPortalTeleportHandler` remains payment-free.

## Files changed
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_07F_DIAGNOSE_GUI_PAYMENT_COMMIT_RUNTIME_PATH_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_07F_DIAGNOSE_GUI_PAYMENT_COMMIT_RUNTIME_PATH.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Diagnostics added
- `MRPortalNetworking` `TELEPORT_REQUEST_C2S`: before service call with player, waypoint, `useScroll`, and `creativeView`.
- `MRPortalNetworking` `TELEPORT_REQUEST_C2S`: after service call with result success, failure reason, and message-sent flag.
- `TeleportRequestService.handleGuiTeleportRequest(...)`: request entry with player, waypoint id, `useScroll`, `creativeView`, creative flag, and dimension.
- Waypoint lookup result with destination dimension and same-dimension result.
- Active-session, same-dimension, missing-item, cooldown, and not-enough-pearl block points.
- Activator classification with `activator`, `infinite`, `creativeBypass`, `useScrollPath`, staff stack, and scroll stack.
- Payment plan details with `activator`, `pearlCost`, `cooldownTicks`, staff stack, scroll stack, `creativeBypass`, and `infinite`.
- Before and after `handler.canStartTeleport(context)`.
- Start of `commitPayment(...)`.
- Staff commit before and after pearl consumption, including pearl counts and removed amount.
- Staff commit before and after cooldown application, including item, ticks, `isOnCooldown`, and cooldown percent.
- Scroll commit before and after `scrollStack.shrink(1)`.
- `syncInventory(...)` call.
- Before and after `handler.startTeleport(context)`.

## Static validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- `grep -R "\[MRP07F\]" -n src/main/java/com/mentality/mrportal`: PASS, logs present in `MRPortalNetworking` and `TeleportRequestService`.
- `grep -R "TeleportRequestService" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: GUI route remains present.
- `grep -R "queueQuickFavoriteRequest" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: no output.
- `grep -R "QUICK_TELEPORT_BY_KEYBIND_C2S" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: packet declaration and receiver remain present.
- `grep -R "queueFavoriteTeleport" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: old quick favorite calls remain present.
- `grep -R "mr_portal_chrono" -n src || true`: no output.
- `grep -R "chrono_portal" -n src || true`: no output.
- `grep -R "FabricLoader.*isModLoaded" -n src || true`: no output.
- Handler payment-free check: no output for `consumeEnderPearls`, `addCooldown`, `shrink`, `findPortalStaff`, or `findTeleportScroll` in `DefaultPortalTeleportHandler.java`.

## Runtime log results
Runtime logs not captured yet.

## Diagnostic answers
Pending `[MRP07F]` runtime logs.

1. Is `TELEPORT_REQUEST_C2S` actually calling `TeleportRequestService` at runtime? Pending runtime logs.
2. Does `handleGuiTeleportRequest` enter staff branch for Portal Staff? Pending runtime logs.
3. Does `handleGuiTeleportRequest` enter scroll branch for Teleport Scroll? Pending runtime logs.
4. What activator is selected? Pending runtime logs.
5. What pearlCost is in the payment plan? Pending runtime logs.
6. What cooldownTicks is in the payment plan? Pending runtime logs.
7. Is `commitPayment` called? Pending runtime logs.
8. Is `consumeEnderPearls` called? Pending runtime logs.
9. Does pearl count change inside `commitPayment` logs? Pending runtime logs.
10. Is `addCooldown` called? Pending runtime logs.
11. Is `scrollStack.shrink(1)` called? Pending runtime logs.
12. Does scroll stack count change inside `commitPayment` logs? Pending runtime logs.
13. Does `handler.startTeleport` return true? Pending runtime logs.
14. Is the same test accidentally using creative/infinite mode? Pending runtime logs.
15. Is GUI closing/sync happening after successful result? Pending runtime logs.

## Root cause
Not found yet. This phase is diagnostic-first.

## Fixes applied
None. This phase intentionally adds diagnostics only.

## Compatibility notes
- Quick favorite is unchanged and still uses `queueFavoriteTeleport(...)`.
- `DefaultPortalTeleportHandler` remains payment-free.
- No Chrono checks were added.
- No addon dependency was added.

## Next recommended phase
PHASE_MRP_API_07G_CAPTURE_GUI_PAYMENT_DIAGNOSTIC_LOGS
