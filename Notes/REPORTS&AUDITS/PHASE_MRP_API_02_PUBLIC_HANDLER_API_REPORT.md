# PHASE_MRP_API_02_PUBLIC_HANDLER_API - PUBLIC_HANDLER_API

## Status
STATUS: BLOCKED_BUILD_JAVA_VERSION

## Goal
Add public MR-Portal teleport handler API contracts inside the base `mr-portal.jar` only.

This phase added API contracts only. Gameplay is not routed through the API yet. `TELEPORT_REQUEST_C2S`, `QUICK_TELEPORT_BY_KEYBIND_C2S`, `PendingTeleportManager`, item behavior, and client rendering behavior were not changed.

## Baseline
- Workspace root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/`
- Active base mod root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/`
- Backup folders found:
```text
./Backup-MR-portal
./MR-portal
```
- Branch: `2.3`
- HEAD: unavailable, `fatal: Needed a single revision`
- Remote:
```text
origin	https://github.com/sqwiziiy/MR-Portal (fetch)
origin	https://github.com/sqwiziiy/MR-Portal (push)
```
- Git status summary: repository still has many untracked files, including `Notes/`, Gradle files, source files, metadata, and resources.
- Existing API package before implementation: none.
- Existing source references before implementation:
  - `MRPortalApi`: none
  - `registerTeleportHandler`: none
  - `mr_portal_chrono`: none
  - `chrono_portal`: none
  - `FabricLoader.*isModLoaded`: none

## Files inspected
- `src/main/java/com/mentality/mrportal/waypoint/WaypointData.java`: internal waypoint data shape used to define public read-only waypoint view.
- `src/main/java/com/mentality/mrportal/MRPortal.java`: logger availability and base mod constants.
- `src/main/resources/fabric.mod.json`: mod id and entrypoint metadata.
- `src/main/java/com/mentality/mrportal/util/DimensionUtil.java`: dimension key conventions.
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW_REPORT.md`: previous audit findings and ownership rules.
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`: current API design notes.

## Files changed

Source:
- `src/main/java/com/mentality/mrportal/api/MRPortalApi.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportHandler.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalWaypointView.java`
- `src/main/java/com/mentality/mrportal/api/MRPortalTeleportActivator.java`

Reports/board:
- `Notes/REPORTS&AUDITS/PHASE_MRP_API_02_PUBLIC_HANDLER_API_REPORT.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_02_PUBLIC_HANDLER_API.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

## API added

### `MRPortalApi`
Purpose: public registry entry point for MR-Portal addon teleport handlers.

Public members:
- `API_VERSION = 1`
- `registerTeleportHandler(MRPortalTeleportHandler handler)`
- `getActiveTeleportHandler()`
- `getRegisteredTeleportHandlers()`

Rules:
- Does not route gameplay yet.
- Uses `MRPortal.LOGGER`.
- Keeps handler storage private.
- Returns immutable handler snapshots.

### `MRPortalTeleportHandler`
Purpose: addon contract for teleport execution strategy.

Public methods:
- `ResourceLocation id()`
- `int priority()`
- `default boolean canStartTeleport(MRPortalTeleportContext context)`
- `boolean startTeleport(MRPortalTeleportContext context)`
- `default boolean usesDefaultPreviewSpark()`

Rules:
- `canStartTeleport` must not consume pearls, scrolls, or cooldown.
- `startTeleport` will receive a base-validated and paid context in later routing phases.
- Preview spark hook is defined but not wired into networking yet.

### `MRPortalTeleportContext`
Purpose: future base-validated request data passed to handlers.

Data:
- `MinecraftServer server`
- `ServerPlayer player`
- `MRPortalWaypointView waypoint`
- `boolean creativeView`
- `boolean useScroll`
- `boolean quickTeleport`
- `MRPortalTeleportActivator activator`
- `int pearlCost`
- `int cooldownTicks`

Rules:
- Null-checks server, player, waypoint, and activator.
- Rejects negative `pearlCost` and `cooldownTicks`.
- Stores no client-only classes.
- Does not consume payment or start teleport.

### `MRPortalWaypointView`
Purpose: stable read-only waypoint view for addons.

Public methods:
- `UUID id()`
- `String name()`
- `ResourceKey<Level> dimension()`
- `double x()`
- `double y()`
- `double z()`
- `boolean favorite()`

Rules:
- Exposes no NBT.
- Exposes no store mutation.
- Does not expose `ServerWaypointStore`.

### `MRPortalTeleportActivator`
Purpose: identifies the base activation path that produced the request.

Values:
- `REGULAR_STAFF`
- `INFINITE_STAFF`
- `TELEPORT_SCROLL`
- `CREATIVE`

No Chrono-specific values were added.

## Handler registry behavior
- Registration is via `MRPortalApi.registerTeleportHandler`.
- Handler and handler id are null-checked.
- Duplicate handler id behavior: latest registration replaces the previous handler and logs a warning.
- Active selection: highest `priority()` wins.
- Same-priority tie behavior: deterministic selection by handler id string, with a warning.
- No-handler behavior: `getActiveTeleportHandler()` returns `null`. A built-in default handler is planned for a later phase.

## Compatibility notes
- No networking routing changed.
- No `PendingTeleportManager` behavior changed.
- No item behavior changed.
- No client rendering behavior changed.
- No Chrono hardcoded checks were added.
- No addon dependency was added.
- No GUI selector/dropdown was added.

`MRPortalNetworking.java` and `PendingTeleportManager.java` match the available `Backup-MR-portal/` copies by `diff -q`.

## Payment/cooldown ownership
Base MR-Portal keeps ownership of waypoint lookup, item validation, regular/infinite staff detection, scroll detection, pearl payment, scroll consumption, staff cooldown, creative/infinite bypass, same-dimension restriction, and quick favorite lookup/delay.

Addon handlers must not consume pearls, scrolls, or cooldown directly through these API contracts.

## Validation

Baseline:
```text
pwd: /home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal
branch: 2.3
HEAD: fatal: Needed a single revision
remote: origin https://github.com/sqwiziiy/MR-Portal
```

Compile:
```text
./gradlew compileJava: BLOCKED
./gradlew compileClientJava: BLOCKED
```

Both Gradle tasks failed during project configuration before Java compilation:
```text
Could not resolve net.fabricmc:fabric-loom:1.15.5.
Dependency requires at least JVM runtime version 21. This build uses a Java 17 JVM.
```

API file checks:
```text
src/main/java/com/mentality/mrportal/api/MRPortalApi.java: exists
src/main/java/com/mentality/mrportal/api/MRPortalTeleportHandler.java: exists
src/main/java/com/mentality/mrportal/api/MRPortalTeleportContext.java: exists
src/main/java/com/mentality/mrportal/api/MRPortalWaypointView.java: exists
src/main/java/com/mentality/mrportal/api/MRPortalTeleportActivator.java: exists
```

Report/board checks:
```text
Notes/REPORTS&AUDITS/PHASE_MRP_API_02_PUBLIC_HANDLER_API_REPORT.md: exists
Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_API_02_PUBLIC_HANDLER_API.md: exists
python3 -m json.tool Notes/OBSIDIAN_BOARD/mrportalboard.canvas >/dev/null: pass
```

Grep sanity:
```text
grep -R "mr_portal_chrono" -n src || true: no output
grep -R "chrono_portal" -n src || true: no output
grep -R "FabricLoader.*isModLoaded" -n src || true: no output
grep -R "registerTeleportHandler" -n src/main/java || true: MRPortalApi only
grep -R "getActiveTeleportHandler" -n src/main/java || true: MRPortalApi only
grep -R "TELEPORT_REQUEST_C2S" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true: unchanged existing packet id and receiver
grep -R "QUICK_TELEPORT_BY_KEYBIND_C2S" -n src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true: unchanged existing packet id and receiver
```

Canvas integrity:
```text
json valid: yes
duplicate node ids: none
duplicate edge ids: none
broken file paths: none
overlap warnings: none
```

## Source diff summary
```text
git diff --stat: no output
git diff -- src/main/java/com/mentality/mrportal/api || true: no output
git diff -- src/main/java/com/mentality/mrportal/network/MRPortalNetworking.java || true: no output
git diff -- src/main/java/com/mentality/mrportal/portal/PendingTeleportManager.java || true: no output
```

Important note: the repository has no usable HEAD and files are untracked, so `git diff` does not show the new API files. `git status --short --untracked-files=all` lists the new API files under `src/main/java/com/mentality/mrportal/api/`.

## Manual validation
Manual validation: not applicable, public contracts only.

## Known risks
- No default handler is registered yet.
- API contracts are not used by gameplay yet.
- Later payment/routing refactor remains high-risk.
- Build validation requires running Gradle with Java 21 or newer.
- The repo has no usable HEAD and is mostly untracked, so Git diff output is limited until files are added.

## Next recommended phase
PHASE_MRP_API_03_DEFAULT_HANDLER_REGISTRATION
