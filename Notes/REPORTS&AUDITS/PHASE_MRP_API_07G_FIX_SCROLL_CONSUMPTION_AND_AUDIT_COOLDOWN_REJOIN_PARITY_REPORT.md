# PHASE_MRP_API_07G_FIX_SCROLL_CONSUMPTION_AND_AUDIT_COOLDOWN_REJOIN_PARITY - FIX_SCROLL_CONSUMPTION_AND_AUDIT_COOLDOWN_REJOIN_PARITY

## Status
STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Goal
Fix the remaining GUI Teleport Scroll consumption regression and audit whether Portal Staff cooldown disappearing after rejoin is a legacy parity issue or a regression.

## Runtime findings from user
- Portal Staff now consumes ender pearls.
- Portal Staff cooldown appears during normal use.
- Portal Staff cooldown disappears after leaving/rejoining the world.
- Teleport Scroll still does not consume itself after opening/starting a portal.
- Teleport Scroll having no cooldown is expected.

## Audit findings
1. Old `PendingTeleportManager` consumed Teleport Scroll with `scroll.shrink(1)`: yes.
2. Old scroll consumption mutated a live hand/inventory stack returned by `findTeleportScroll(...)`: yes.
3. Current `TeleportRequestService` stores a live scroll stack when `useScrollPath` is true; no copied stack path was found.
4. Whether scroll branch is reached at runtime is not proven by logs because no `[MRP07F]` runtime log excerpts were available in `logs/latest.log`.
5. Whether `handler.startTeleport` returns true for scroll is pending runtime logs.
6. Code audit found a confirmed scroll route weakness: the server did not remember whether the screen was opened by a scroll. The client guessed `useScroll` from local hand state, so a scroll-opened screen could send `useScroll=false` and the service would start a non-scroll request.
7. Old Portal Staff cooldown persistence across logout/rejoin: no persistent cooldown storage or login restoration found.
8. Current cooldown uses vanilla `ItemCooldowns`, same as old code.
9. Persistent cooldown is out of scope for parity unless requested as a new feature.

## Runtime diagnostic logs
No `[MRP07F]` runtime excerpts were available in `logs/latest.log` during this phase.

## Root cause
Confirmed code-level scroll root cause: GUI scroll activation mode was client-inferred instead of server-owned. `TeleportScrollItem.use(...)` opened the waypoint screen with only `creativeView=false`; later `TELEPORT_REQUEST_C2S` trusted the client's `useScroll` boolean. If the client guessed false, the server used the staff/non-scroll path and never reached `commitPayment`'s scroll shrink branch.

## Files inspected
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportPaymentPlan.java`
- `src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`
- `src/main/java/com/mentality/mrportal/item/MRPortalItems.java`
- `src/main/java/com/mentality/mrportal/config/MRPortalConfig.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- `../Backup-MR-portal/src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java`

## Files changed
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_07G_FIX_SCROLL_CONSUMPTION_AND_AUDIT_COOLDOWN_REJOIN_PARITY_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_07G_FIX_SCROLL_CONSUMPTION_AND_AUDIT_COOLDOWN_REJOIN_PARITY.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Fix summary
- `MRPortalNetworking` now tracks the server-owned scroll/staff mode for an open waypoint screen.
- `PortalStaffItem` opens the waypoint screen with `useScroll=false`.
- `TeleportScrollItem` opens the waypoint screen with `useScroll=true`.
- The keybind open path marks staff/infinite screens as non-scroll and scroll screens as scroll mode.
- Waypoint add/delete/rename/favorite refreshes preserve the active screen mode.
- `TELEPORT_REQUEST_C2S` now uses the client packet value OR the server's active screen scroll mode before calling `TeleportRequestService`.
- Active screen mode is cleared on screen close and after successful GUI teleport.
- Quick favorite routing remains unchanged.

## Cooldown rejoin parity classification
EXPECTED_LEGACY_BEHAVIOR

Old code used vanilla `player.getCooldowns().addCooldown(...)` only. No saved persistent cooldown state, world data, player persistent NBT, or login restoration path was found in the backup. Cooldown disappearing after rejoin is therefore not an API routing regression.

## Validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS
- `[MRP07F]` grep: diagnostics remain in `MRPortalNetworking` and `TeleportRequestService`.
- `TeleportRequestService` grep in `MRPortalNetworking`: present only for GUI `TELEPORT_REQUEST_C2S`.
- `queueQuickFavoriteRequest` grep in `MRPortalNetworking`: no matches.
- `QUICK_TELEPORT_BY_KEYBIND_C2S` grep: receiver still present.
- `queueFavoriteTeleport` grep: quick favorite still uses old manager path.
- `mr_portal_chrono` grep: no matches.
- `chrono_portal` grep: no matches.
- `FabricLoader.*isModLoaded` grep: no matches.
- Default handler payment-free check: no `consumeEnderPearls`, `addCooldown`, `shrink`, `findPortalStaff`, or `findTeleportScroll` matches.

## Manual validation
Manual runtime validation was not run by the agent in this phase.

Pending manual checks:
1. Teleport Scroll GUI decreases scroll count by exactly 1, consumes no pearls, applies no staff cooldown, and starts the portal.
2. Regular Portal Staff GUI still consumes pearls and blocks immediate second use with cooldown.
3. Portal Staff cooldown disappearing after rejoin remains classified as expected legacy vanilla cooldown behavior.
4. Quick favorite smoke test still uses old behavior and does not route through `TeleportRequestService`.

## Known risks
- Manual validation is required to confirm scroll count decreases exactly once after the server-owned mode fix.
- `[MRP07F]` diagnostics remain temporary and should be removed in a cleanup phase after root cause validation.

## Next recommended phase
PHASE_MRP_API_07H_VALIDATE_SCROLL_FIX_AND_DIAGNOSTIC_CLEANUP
