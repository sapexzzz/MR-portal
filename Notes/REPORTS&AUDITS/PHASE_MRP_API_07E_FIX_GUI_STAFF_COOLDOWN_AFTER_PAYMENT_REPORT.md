# PHASE_MRP_API_07E_FIX_GUI_STAFF_COOLDOWN_AFTER_PAYMENT - FIX_GUI_STAFF_COOLDOWN_AFTER_PAYMENT

## Status
STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Goal
Fix the remaining regular Portal Staff cooldown regression in the GUI-routed `TeleportRequestService` path.

## Runtime bug report from user
- Regular staff pearls are consumed.
- Regular staff cooldown is missing or not visibly retained.
- Teleport Scroll has no cooldown, which is expected.
- Payment otherwise works.

## Root cause
Audit found that cooldown code exists in `TeleportRequestService.commitPayment(...)` and `cooldownTicks` is calculated from `MRPortalConfig.cooldownSeconds * 20`. Runtime config has `cooldownSeconds: 60`, so this is not a zero-tick cooldown issue.

The 07D inventory sync fix changed the regular staff transaction order from the old `PendingTeleportManager.beginTeleport(...)` order. The old path consumed pearls, applied cooldown, then started the session. The GUI service was consuming pearls, synchronizing inventory/menu state, then applying cooldown. This phase restores the old order and keeps sync after the full payment/cooldown commit.

## Files inspected
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`
- `src/main/java/com/mentality/mrportal/config/MRPortalConfig.java`
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `../Backup-MR-portal/src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`

## Files changed
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_07E_FIX_GUI_STAFF_COOLDOWN_AFTER_PAYMENT_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_07E_FIX_GUI_STAFF_COOLDOWN_AFTER_PAYMENT.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Fix summary
- Restored regular staff commit order to match the old `PendingTeleportManager.beginTeleport(...)` transaction.
- Cooldown item target remains `paymentPlan.staffStack().getItem()`, the same live staff stack used for cooldown precheck.
- Cooldown ticks remain `MRPortalConfig.cooldownSeconds * 20`; runtime config audit showed `cooldownSeconds: 60`.
- Regular staff path now consumes pearls, applies cooldown, synchronizes inventory/menu state, then starts the handler.
- Scroll remains unchanged: scroll shrink and inventory sync only, no cooldown.
- Quick favorite remains unchanged and old-path.

## Payment/cooldown transaction order
- Validate waypoint/session/dimension/item/cooldown/pearl availability.
- Call `handler.canStartTeleport(context)`.
- Consume pearls with `PendingTeleportManager.consumeEnderPearls(...)`.
- Apply regular staff cooldown with `player.getCooldowns().addCooldown(paymentPlan.staffStack().getItem(), paymentPlan.cooldownTicks())`.
- Synchronize inventory/menu state.
- Call `handler.startTeleport(context)`.

## Compatibility notes
- Teleport Scroll still has no cooldown.
- Quick favorite routing is unchanged.
- Preview behavior is unchanged.
- Client rendering is unchanged.
- No Chrono checks were added.
- No addon dependency was added.
- `DefaultPortalTeleportHandler` remains payment-free.

## Validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- `grep -R "TeleportRequestService" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: GUI route remains present.
- `grep -R "queueQuickFavoriteRequest" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: no output.
- `grep -R "QUICK_TELEPORT_BY_KEYBIND_C2S" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: packet declaration and receiver remain present.
- `grep -R "queueFavoriteTeleport" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: old quick favorite queue calls remain present.
- `grep -R "mr_portal_chrono" -n src || true`: no output.
- `grep -R "chrono_portal" -n src || true`: no output.
- `grep -R "FabricLoader.*isModLoaded" -n src || true`: no output.
- Handler payment-free check: no output for `consumeEnderPearls`, `addCooldown`, `shrink`, `findPortalStaff`, or `findTeleportScroll` in `DefaultPortalTeleportHandler.java`.
- Service cooldown check shows cooldown precheck, `cooldownSeconds * 20` plan creation, and `addCooldown(...)` in service.

Manual validation was not run by the agent. Pending runtime tests:
- A. Regular staff same-dimension GUI teleport: teleport works, pearls decrease by `portalPearlCost`, cooldown appears or immediate second use is blocked.
- B. Immediate second regular staff GUI teleport: blocked by cooldown, no extra pearls consumed.
- C. Not enough pearls: blocked, no cooldown, no pearl consumption.
- D. Teleport scroll: scroll decreases by exactly 1, no pearls, no staff cooldown.
- E. Quick favorite smoke: old path still works and is not service-routed.

## Known risks
- If immediate second use is blocked but the visual overlay still does not appear, the remaining issue is likely visual-only feedback rather than server cooldown enforcement.
- If immediate second use is not blocked after this order fix, the next target is verifying whether the GUI close/open interaction is bypassing the cooldown precheck rather than whether the cooldown packet is visible.

## Next recommended phase
PHASE_MRP_API_07F_GUI_STAFF_COOLDOWN_MANUAL_VALIDATION
