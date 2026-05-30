# PHASE_MRP_API_02_PUBLIC_HANDLER_API

## Short name
PUBLIC_HANDLER_API

## Goal
Add public MR-Portal teleport handler API contracts inside the base `mr-portal.jar`.

## API classes added
- `MRPortalApi`
- `MRPortalTeleportHandler`
- `MRPortalTeleportContext`
- `MRPortalWaypointView`
- `MRPortalTeleportActivator`

## Registry behavior
- Handlers register through `MRPortalApi.registerTeleportHandler`.
- Duplicate handler ids are replaced by the latest registration and logged as a warning.
- Active handler selection uses highest priority.
- Same-priority ties are selected deterministically by handler id string.
- No registered handlers returns `null`; the built-in default handler is planned for a later phase.

## Validation result
- `compileJava`: blocked by Gradle JVM version. Fabric Loom requires Java 21; current build uses Java 17.
- `compileClientJava`: blocked by the same Gradle JVM version issue.
- API files exist.
- Grep sanity found no Chrono hardcoded checks and no addon dependency.
- No networking, `PendingTeleportManager`, item, or client rendering behavior was changed.

## Known risks
- No default handler is registered yet.
- Gameplay is not routed through the API yet.
- Later payment/routing refactor remains high-risk.
- Build validation requires running Gradle with Java 21 or newer.

## Report path
`MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_API_02_PUBLIC_HANDLER_API_REPORT.md`

## Next phase
PHASE_MRP_API_03_DEFAULT_HANDLER_REGISTRATION
