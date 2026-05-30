# PHASE_MRP_API_07C_FIX_GUI_TELEPORT_PAYMENT_REGRESSION - FIX_GUI_TELEPORT_PAYMENT_REGRESSION

## Status
STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Goal
Fix the GUI teleport payment regression introduced when normal waypoint GUI teleports were routed through `TeleportRequestService`.

## Baseline
- Root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `2.3`
- HEAD: unavailable (`git rev-parse --short HEAD` returned `fatal: Needed a single revision`)
- Remote: `origin https://github.com/sqwiziiy/MR-Portal`
- Gradle runtime: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`
- Previous phase status: `PHASE_MRP_API_07_ROUTE_GUI_TELEPORT_THROUGH_SERVICE` was build-passing with manual validation pending.
- Git status: repository still reports broad untracked baseline; this phase touched only the source/docs listed below.

## Runtime bug report from user
- Regular Portal Staff GUI teleport starts but does not consume ender pearls.
- Regular Portal Staff GUI teleport starts but does not apply cooldown.
- One-time/single-use Portal Staff GUI teleport starts but does not consume the item.
- Teleport Scroll works correctly.
- Favorite waypoint / quick favorite behavior appears OK.

## Root cause
Initial audit found `TeleportRequestService.handleGuiTeleportRequest(...)` classified activation from the broad `creativeView` value passed by `MRPortalNetworking.hasCreativeView(player)`.

That flag means the player can see the creative/infinite waypoint view. It is true for creative players and for players with an infinite staff anywhere in inventory. The service then converted any non-creative `creativeView == true` request into `MRPortalTeleportActivator.INFINITE_STAFF`, causing the payment plan to skip regular staff pearl cost and cooldown.

The default handler also treated `context.creativeView()` as a paid-session infinite flag, which could preserve the same broad bypass downstream. The fix should classify staff payment from the actual staff stack selected by `PendingTeleportManager.findPortalStaff(player)` and reserve free behavior for creative players or actual infinite staff.

The inspected base source and backup `PendingTeleportManager` do not contain separate regular staff shrink/damage/break behavior. Regular staff ownership was preserved by requiring the staff stack and applying cooldown; scroll shrink remains the only item consumption path found in base source.

## Files inspected
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportActivationInfo.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestFailureReason.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`
- `src/main/java/com/mentality/mrportal/config/MRPortalConfig.java`
- `../Backup-MR-portal/src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`

## Files changed
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_07C_FIX_GUI_TELEPORT_PAYMENT_REGRESSION_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_07C_FIX_GUI_TELEPORT_PAYMENT_REGRESSION.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Fix summary
- Staff activation classification now uses the live staff stack returned by `PendingTeleportManager.findPortalStaff(player)`.
- `creativeView` no longer automatically classifies a non-creative GUI request as `INFINITE_STAFF`.
- Regular staff payment plans now retain the same staff stack for precheck and commit, avoiding inconsistent item lookup.
- Regular staff GUI teleports now reach `consumeEnderPearls(...)` and `addCooldown(...)` in `TeleportRequestService.commitPayment(...)`.
- Scroll requests still use the scroll stack and shrink one scroll once.
- `DefaultPortalTeleportHandler` now treats sessions as infinite only for `INFINITE_STAFF` or `CREATIVE` activators. It remains payment-free.
- Quick favorite remains on the old `PendingTeleportManager.queueFavoriteTeleport(...)` path.

## Payment/cooldown preservation
- Regular staff order: waypoint/session/dimension validation, staff lookup, cooldown precheck, pearl count precheck, handler `canStartTeleport`, pearl consumption, cooldown application, handler start.
- Not enough pearls: returns `NOT_ENOUGH_PEARLS`, sends the existing message, and does not start a session or apply cooldown.
- Cooldown active: returns `COOLDOWN_ACTIVE`, sends the existing message, and does not consume pearls.
- Scroll order: scroll lookup, handler `canStartTeleport`, scroll shrink once, handler start.
- Infinite/creative order: no pearl cost, no scroll shrink, no cooldown, handler start.
- Base source and backup audit found no regular staff `shrink`, damage, or break path. No new staff-stack consumption was added because that would change legacy regular staff behavior.

## Compatibility notes
- Quick favorite routing is unchanged. `QUICK_TELEPORT_BY_KEYBIND_C2S` still calls the old favorite flow and `queueFavoriteTeleport(...)`.
- Preview behavior is unchanged.
- Client rendering is unchanged.
- No Chrono checks were added.
- No addon dependency was added.
- `DefaultPortalTeleportHandler` contains no pearl, scroll, cooldown, or item lookup logic.

## Validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- `grep -R "TeleportRequestService" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`: GUI route remains present.
- `grep -R "queueQuickFavoriteRequest" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: no output; quick favorite is not routed through the service.
- `grep -R "QUICK_TELEPORT_BY_KEYBIND_C2S" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: packet declaration and receiver remain present.
- `grep -R "queueFavoriteTeleport" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true`: old quick favorite queue calls remain present.
- `grep -R "mr_portal_chrono" -n src || true`: no output.
- `grep -R "chrono_portal" -n src || true`: no output.
- `grep -R "FabricLoader.*isModLoaded" -n src || true`: no output.
- `grep -n "consumeEnderPearls\|addCooldown\|shrink\|findPortalStaff\|findTeleportScroll" src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java || true`: no output.
- `grep -n "consumeEnderPearls\|addCooldown\|shrink\|portalPearlCost\|cooldownSeconds" src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java || true`: service contains scroll shrink, pearl consumption, cooldown application, and cooldown config usage.

Manual validation was not run by the agent. Required pending runtime checks:
- A. Regular staff same-dimension GUI teleport: pearls consumed exactly once, cooldown applied, portal starts, preview disappears, screen closes.
- B. One-time/single-use Portal Staff GUI teleport: validate against the actual runtime item variant. Base source inspected has no separate regular staff shrink/damage behavior.
- C. Not enough pearls: no teleport, no cooldown, no partial consumption.
- D. Cooldown: immediate second use blocked, no extra pearl consumption.
- E. Teleport scroll: one scroll consumed, no pearls, no staff cooldown.
- F. Quick favorite smoke: old path still works and is not service-routed.

## Known risks
- Manual validation is still required after the fix because this is a gameplay routing regression.
- The base source inspected does not show a separate one-time staff item consumption path; if an external/local item variant exists, it needs runtime validation.
- Payment is committed immediately before handler start; a non-default handler returning false after payment remains a later API transaction risk.

## Next recommended phase
PHASE_MRP_API_07D_GUI_TELEPORT_PAYMENT_FIX_MANUAL_VALIDATION
