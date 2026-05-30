# PHASE_MRP_API_08B_QUICK_FAVORITE_MANUAL_VALIDATION - QUICK_FAVORITE_MANUAL_VALIDATION

## Status
STATUS: BLOCKED_MANUAL_VALIDATION

## Goal
Manually validate quick favorite behavior after `QUICK_TELEPORT_BY_KEYBIND_C2S` was routed through `TeleportRequestService`, including activation-time payment, scroll consumption, cooldown behavior, preview cleanup, and GUI regression smoke.

## Baseline
- root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- branch: `2.3`
- HEAD: unavailable, `fatal: Needed a single revision`
- remote: `origin https://github.com/sqwiziiy/MR-Portal`
- previous phase status: `STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING`
- Gradle runtime: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`

## Static validation
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS
- `[MRP07F]` grep: no matches
- `TeleportRequestService` grep in `MRPortalNetworking`: GUI and quick favorite routes present
- `queueQuickFavoriteRequest` grep: networking and service present
- `activateQueuedQuickFavorite` grep: manager and service present
- `activatePendingFavoriteTeleport` grep: no matches
- `QUICK_TELEPORT_BY_KEYBIND_C2S` grep: receiver present
- `queueFavoriteTeleport` grep: manager queue storage and service queue call present
- `handleGuiTeleportRequest` grep: GUI service route present
- `mr_portal_chrono` grep: no matches
- `chrono_portal` grep: no matches
- `FabricLoader.*isModLoaded` grep: no matches
- handler payment-free check: no `consumeEnderPearls`, `addCooldown`, `shrink`, `findPortalStaff`, or `findTeleportScroll` matches in `DefaultPortalTeleportHandler`

## Manual validation matrix
### Test A - Quick favorite with regular Portal Staff
- status: NOT TESTED
- steps performed: not run in this environment
- expected: keypress queues without payment; activation consumes pearls once, applies cooldown, starts portal, and leaves no stuck preview
- actual: not captured
- notes/logs: manual runtime validation required

### Test B - Quick favorite with not enough pearls
- status: NOT TESTED
- steps performed: not run in this environment
- expected: activation fails safely, no cooldown, no partial consumption, no portal, no stuck preview
- actual: not captured
- notes/logs: manual runtime validation required

### Test C - Quick favorite cooldown block
- status: NOT TESTED
- steps performed: not run in this environment
- expected: immediate second quick favorite is blocked by cooldown with no extra pearls or second portal
- actual: not captured
- notes/logs: manual runtime validation required

### Test D - Quick favorite with Teleport Scroll
- status: NOT TESTED
- steps performed: not run in this environment
- expected: scroll not consumed at keypress; one scroll consumed at activation; no pearls/cooldown; portal starts; no stuck preview
- actual: not captured
- notes/logs: manual runtime validation required

### Test E - Quick favorite with infinite/creative
- status: NOT TESTED
- steps performed: not run in this environment
- expected: no payment, no regular staff cooldown, old dimension behavior preserved, portal starts
- actual: not captured
- notes/logs: manual runtime validation required

### Test F - Same-dimension restriction
- status: NOT TESTED
- steps performed: not run in this environment
- expected: non-creative/non-infinite cross-dimension favorite blocked with no payment/cooldown/portal/stuck preview
- actual: not captured
- notes/logs: manual runtime validation required

### Test G - GUI regression smoke
- status: NOT TESTED
- steps performed: not run in this environment
- expected: GUI staff and GUI scroll still work as manually validated before phase 08
- actual: not captured
- notes/logs: manual runtime validation required

## Bugs found
None from static validation. Runtime validation was not run, so gameplay bugs may still exist.

## Fixes applied
No source fixes applied in this validation phase.

## Compatibility conclusion
- quick favorite service route validated: static only, runtime pending
- payment at activation validated: static only, runtime pending
- scroll at activation validated: static only, runtime pending
- GUI regression status: runtime pending
- preview cleanup status: runtime pending

## Known risks
- All quick favorite runtime behavior remains unverified in-game.
- Preview cleanup and exact activation-time inventory/cooldown behavior require manual runtime checks.

## Next recommended phase
PHASE_MRP_API_08B_QUICK_FAVORITE_MANUAL_VALIDATION_CONTINUATION
