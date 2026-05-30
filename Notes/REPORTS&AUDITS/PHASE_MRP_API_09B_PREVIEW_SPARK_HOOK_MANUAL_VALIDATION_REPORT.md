# PHASE_MRP_API_09B_PREVIEW_SPARK_HOOK_MANUAL_VALIDATION - PREVIEW_SPARK_HOOK_MANUAL_VALIDATION

## Status

STATUS: BLOCKED_MANUAL_VALIDATION

## Goal

Validate that the default MR-Portal preview spark behavior did not regress after `PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK`.

## Baseline

- Root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `2.3`
- HEAD: unavailable; `git rev-parse --short HEAD` reports `fatal: Needed a single revision`.
- Remote: `origin https://github.com/sqwiziiy/MR-Portal`
- Previous phase: `PHASE_MRP_API_09_PREVIEW_SPARK_HANDLER_HOOK`
- Previous phase status: `STATUS: PASS_BUILD_ONLY_MANUAL_VALIDATION_PENDING`
- Gradle runtime: Java 21 via `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`

## Static validation

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS.
- `[MRP07F]` grep: PASS, no matches.
- `usesDefaultPreviewSpark` grep: PASS, hook is present and consulted in GUI/quick preview paths.
- `OPEN_SCREEN_S2C` grep: PASS, server send path and client receive path are present.
- `broadcastPreviewSpark` / `removeDefaultPreviewSpark` grep: PASS, default GUI preview create/remove paths remain present.
- `broadcastQuickPreviewSpark` / `removeQuickPreviewSpark` grep: PASS, quick preview create/remove paths remain present.
- `defaultPreviewCreated` grep: PASS, quick favorite pending activation tracks whether default preview was emitted.
- `defaultPreviewShown` grep: PASS, client local preview tracks whether it was shown.
- `queueQuickFavoriteRequest` grep: PASS, quick favorite route remains service-routed.
- `activateQueuedQuickFavorite` grep: PASS, activation route remains service-routed.
- `handleGuiTeleportRequest` grep: PASS, GUI route remains service-routed.
- Chrono grep checks: PASS, no `mr_portal_chrono`, `chrono_portal`, or `FabricLoader.*isModLoaded` matches.
- Payment unchanged check: PASS, no `consumeEnderPearls`, `addCooldown`, or `shrink` matches in `DefaultPortalTeleportHandler`.

## Manual validation matrix

### Test A - GUI staff open/close preview

- Status: BLOCKED.
- Steps performed: Not run in this environment.
- Expected: GUI opens normally, preview appears, no packet decode crash, preview disappears on close, no stuck preview.
- Actual: Not observed.
- Notes/logs: Runtime client validation still required.

### Test B - GUI staff successful teleport

- Status: BLOCKED.
- Steps performed: Not run in this environment.
- Expected: Preview appears, staff payment/cooldown still works, preview disappears on teleport start, screen closes, portal starts, no stuck preview.
- Actual: Not observed.
- Notes/logs: Runtime client validation still required.

### Test C - GUI scroll successful teleport

- Status: BLOCKED.
- Steps performed: Not run in this environment.
- Expected: Preview appears, scroll consumption still works, no staff cooldown, preview disappears on teleport start, screen closes, portal starts, no stuck preview.
- Actual: Not observed.
- Notes/logs: Runtime client validation still required.

### Test D - Quick favorite regular staff preview

- Status: BLOCKED.
- Steps performed: Not run in this environment.
- Expected: Quick preview appears during delay, keypress does not consume pearls, activation consumes pearls and applies cooldown, preview disappears on activation, portal starts, no stuck preview.
- Actual: Not observed.
- Notes/logs: Runtime client validation still required.

### Test E - Quick favorite scroll preview

- Status: BLOCKED.
- Steps performed: Not run in this environment.
- Expected: Quick preview appears during delay, scroll is not consumed at keypress, scroll is consumed at activation, no pearls, preview disappears on activation, portal starts, no stuck preview.
- Actual: Not observed.
- Notes/logs: Runtime client validation still required.

### Test F - Screen refresh / waypoint edit preview stability

- Status: BLOCKED.
- Steps performed: Not run in this environment.
- Expected: No crash, no uncontrolled duplicate preview, sane preview state, close removes default preview, no stuck preview.
- Actual: Not observed.
- Notes/logs: Runtime client validation still required.

### Test G - Log check

- Status: BLOCKED.
- Steps performed: Not run because runtime validation was not performed.
- Expected: No relevant errors, packet decode/encode errors, or preview cleanup exceptions.
- Actual: Not observed.
- Notes/logs: `logs/latest.log` runtime review remains pending.

## Bugs found

None found during static validation. Runtime validation remains blocked/pending.

## Fixes applied

No source fixes were applied in this validation phase.

## Compatibility conclusion

- Default handler preview behavior is statically intact but not runtime validated in this phase.
- GUI preview open/close is not runtime validated.
- Quick favorite preview is not runtime validated.
- GUI payment remains statically unchanged.
- Quick favorite payment remains statically unchanged.
- `OPEN_SCREEN_S2C` payload appears statically matched: server writes the trailing `useDefaultPreviewSpark` boolean and client reads it after preview scale.
- No Chrono checks were added.
- No addon dependency was added.

## Known risks

- Addon opt-out still needs real Chrono handler validation.
- Default preview cleanup edge cases need in-game validation.
- `OPEN_SCREEN_S2C` packet compatibility needs runtime smoke validation.

## Next recommended phase

PHASE_MRP_API_09B_PREVIEW_SPARK_HOOK_MANUAL_VALIDATION_CONTINUATION
