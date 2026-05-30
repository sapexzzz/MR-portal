# PHASE_MRP_API_07D_FIX_GUI_PAYMENT_COMMIT_RESOURCE_CONSUMPTION - FIX_GUI_PAYMENT_COMMIT_RESOURCE_CONSUMPTION

## Status
STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Goal
Fix the remaining GUI payment resource consumption regression after `PHASE_MRP_API_07C_FIX_GUI_TELEPORT_PAYMENT_REGRESSION`.

## Runtime bug report from user
- Portal Staff cooldown is now applied.
- Portal Staff GUI teleport still does not visibly consume ender pearls.
- Teleport Scroll GUI teleport still does not visibly consume the scroll.
- Quick favorite/favorite waypoint behavior is still OK.

## Root cause
Audit found that `TeleportRequestService.commitPayment(...)` does call the old live-stack mutation paths:

- `PendingTeleportManager.consumeEnderPearls(player, paymentPlan.pearlCost())`
- `paymentPlan.scrollStack().shrink(1)`
- `player.getCooldowns().addCooldown(...)`

`TeleportPaymentPlan` stores live `ItemStack` references and no `copy()` usage was found in the payment service or payment plan. Since cooldown is visible at runtime but inventory resources are not, the likely remaining bug is missing inventory synchronization after GUI-routed server-side stack mutation before the custom waypoint screen is closed.

The fix should keep resource mutation in `TeleportRequestService`, preserve old helper logic, and force a server inventory/menu sync only after successful GUI payment mutation.

## Files inspected
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `../Backup-MR-portal/src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`

## Files changed
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_07D_FIX_GUI_PAYMENT_COMMIT_RESOURCE_CONSUMPTION_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_07D_FIX_GUI_PAYMENT_COMMIT_RESOURCE_CONSUMPTION.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Fix summary
- Added `TeleportRequestService.syncInventory(ServerPlayer)`.
- After GUI scroll payment, the service now calls `syncInventory(player)` immediately after `scrollStack.shrink(1)`.
- After GUI staff pearl payment, the service now calls `syncInventory(player)` immediately after `PendingTeleportManager.consumeEnderPearls(...)` succeeds.
- Cooldown application remains unchanged and still occurs exactly once after successful staff resource commit.
- The default handler remains payment-free.
- Quick favorite remains on the old direct `PendingTeleportManager.queueFavoriteTeleport(...)` path.

## Payment transaction order
- Precheck waypoint/session/dimension/item/cooldown/pearl availability.
- Call `handler.canStartTeleport(context)`.
- Commit resources in `TeleportRequestService.commitPayment(...)`.
- Synchronize inventory/menu state after stack mutation.
- Apply staff cooldown for regular staff.
- Call `handler.startTeleport(context)`.

## Compatibility notes
- Quick favorite routing is unchanged.
- Preview behavior is unchanged.
- Client rendering is unchanged.
- No Chrono checks were added.
- No addon dependency was added.
- `DefaultPortalTeleportHandler` contains no payment, cooldown, stack shrink, or item lookup logic.

## Validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- `grep -R "TeleportRequestService" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: GUI route remains present.
- `grep -R "queueQuickFavoriteRequest" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: no output; quick favorite is not routed through the service.
- `grep -R "QUICK_TELEPORT_BY_KEYBIND_C2S" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: packet declaration and receiver remain present.
- `grep -R "queueFavoriteTeleport" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: old quick favorite calls remain present.
- `grep -R "mr_portal_chrono" -n src || true`: no output.
- `grep -R "chrono_portal" -n src || true`: no output.
- `grep -R "FabricLoader.*isModLoaded" -n src || true`: no output.
- Handler payment-free check: no output for `consumeEnderPearls`, `addCooldown`, `shrink`, `findPortalStaff`, or `findTeleportScroll` in `DefaultPortalTeleportHandler.java`.
- Service commit check: service contains scroll shrink, pearl consumption, cooldown application, and no `copy()` usage.

Manual validation was not run by the agent. Pending runtime tests:
- A. Regular staff same-dimension GUI teleport: teleport works, pearls decrease by `portalPearlCost`, cooldown appears, preview disappears, screen closes.
- B. Not enough pearls: teleport blocked, pearls unchanged, cooldown not applied.
- C. Cooldown: immediate second teleport blocked, no extra pearls consumed.
- D. Teleport scroll: teleport works, scroll decreases by exactly 1, pearls unchanged, staff cooldown not applied.
- E. Quick favorite smoke: old path still works and is not service-routed.

## Known risks
- Manual runtime validation is required because the reported issue is visible inventory state after GUI interaction.
- If runtime still shows no resource consumption after inventory sync, the next likely target is packet/UI state refreshing around the custom waypoint screen close.

## Next recommended phase
PHASE_MRP_API_07E_GUI_PAYMENT_COMMIT_MANUAL_VALIDATION
