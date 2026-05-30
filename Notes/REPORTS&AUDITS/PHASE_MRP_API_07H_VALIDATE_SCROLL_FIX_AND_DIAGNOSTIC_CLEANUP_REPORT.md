# PHASE_MRP_API_07H_VALIDATE_SCROLL_FIX_AND_DIAGNOSTIC_CLEANUP - VALIDATE_SCROLL_FIX_AND_DIAGNOSTIC_CLEANUP

## Status
STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING

## Goal
Validate the 07G server-owned GUI scroll mode fix, confirm staff/quick-favorite parity, and remove temporary `[MRP07F]` diagnostics only after critical runtime validation passes.

## Baseline
- root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- branch: `2.3`
- HEAD: unavailable, `fatal: Needed a single revision`
- remote: `origin https://github.com/sqwiziiy/MR-Portal`
- previous phase status: `STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING`
- Gradle runtime: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`

## Files inspected
- `src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java`
- `src/main/java/com/mentality/mrportal/item/PortalStaffItem.java`
- `src/main/java/com/mentality/mrportal/item/TeleportScrollItem.java`
- `src/main/java/com/mentality/mrportal/portal/TeleportRequestService.java`
- `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`

## Runtime validation results
### Test A - Teleport Scroll GUI consumption
- status: NOT TESTED
- steps: pending in-game runtime validation
- expected: one scroll consumed, no pearls, no staff cooldown, portal starts, preview disappears, screen closes
- actual: not captured
- notes: diagnostics retained for this test

### Test B - Regular Portal Staff GUI regression check
- status: NOT TESTED
- steps: pending in-game runtime validation
- expected: pearls decrease by `portalPearlCost`, cooldown applies or immediate second use is blocked, portal starts
- actual: not captured
- notes: diagnostics retained for this test

### Test C - Immediate second staff cooldown check
- status: NOT TESTED
- steps: pending in-game runtime validation
- expected: blocked by cooldown, no extra pearls consumed
- actual: not captured
- notes: diagnostics retained for this test

### Test D - Not enough pearls
- status: NOT TESTED
- steps: pending in-game runtime validation
- expected: teleport blocked, no cooldown, no pearl consumption
- actual: not captured
- notes: diagnostics retained for this test

### Test E - Quick favorite smoke
- status: STATIC PASS / RUNTIME NOT TESTED
- steps: static grep confirmed `QUICK_TELEPORT_BY_KEYBIND_C2S` still calls `queueFavoriteTeleport`
- expected: old-path quick favorite behavior remains unchanged
- actual: no service quick favorite route found in networking
- notes: runtime smoke still pending

### Test F - Staff cooldown rejoin parity note
- status: STATIC PASS / RUNTIME NOT TESTED
- steps: previous backup audit found vanilla `ItemCooldowns` only
- expected: cooldown disappearing after rejoin remains `EXPECTED_LEGACY_BEHAVIOR`
- actual: no persistent cooldown implementation added
- notes: no fix applied in this phase

## Diagnostic cleanup
- `[MRP07F]` removed: no
- source files cleaned: none
- grep result: `[MRP07F]` remains in `MRPortalNetworking` and `TeleportRequestService`
- reason: critical manual runtime validation was not completed in this phase

## Files changed
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_07H_VALIDATE_SCROLL_FIX_AND_DIAGNOSTIC_CLEANUP_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_07H_VALIDATE_SCROLL_FIX_AND_DIAGNOSTIC_CLEANUP.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## Compatibility notes
- Quick favorite unchanged; `QUICK_TELEPORT_BY_KEYBIND_C2S` still uses `queueFavoriteTeleport`.
- Teleport Scroll still has no cooldown.
- Portal Staff cooldown rejoin behavior remains classified as expected legacy behavior.
- No Chrono checks were added.
- No addon dependency was added.
- `DefaultPortalTeleportHandler` remains payment-free.

## Validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS
- `[MRP07F]` grep: present before cleanup and retained
- `TeleportRequestService` grep in `MRPortalNetworking`: GUI route present
- `queueQuickFavoriteRequest` grep in `MRPortalNetworking`: no matches
- `QUICK_TELEPORT_BY_KEYBIND_C2S` grep: receiver present
- `queueFavoriteTeleport` grep: old quick favorite path present
- `mr_portal_chrono` grep: no matches
- `chrono_portal` grep: no matches
- `FabricLoader.*isModLoaded` grep: no matches
- handler payment-free check: no `consumeEnderPearls`, `addCooldown`, `shrink`, `findPortalStaff`, or `findTeleportScroll` matches in `DefaultPortalTeleportHandler`

## Known risks
- Runtime scroll consumption is not yet manually confirmed after the 07G server-owned mode fix.
- Runtime staff payment/cooldown regression check remains pending.
- Temporary `[MRP07F]` diagnostics still add noisy logs until validation passes and cleanup runs.

## Next recommended phase
PHASE_MRP_API_07H_MANUAL_VALIDATION_CONTINUATION
