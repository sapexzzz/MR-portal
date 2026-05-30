# PHASE_MRP_API_03_DEFAULT_HANDLER_REGISTRATION

## Goal

Add and register the built-in/default MR-Portal teleport handler so `MRPortalApi` has a default active handler.

## Default Handler Class

- Class: `src/main/java/com/mentality/mrportal/portal/DefaultPortalTeleportHandler.java`
- Handler id: `mr_portal:default_portal`
- Priority: `0`
- Preview spark behavior: `usesDefaultPreviewSpark()` returns `true`
- `canStartTeleport(context)` returns `true`
- `startTeleport(context)` returns `false` for now because gameplay routing is intentionally deferred to the request-service phase.

## Registration Point

- File: `src/main/java/com/mentality/mrportal/MRPortal.java`
- Method: `onInitialize()`
- Registration: `MRPortalApi.registerTeleportHandler(new DefaultPortalTeleportHandler());`
- Initialization order: after config load and item registration, before server networking and tick registration.

## Validation Result

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava`: PASS
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava`: PASS
- No `TELEPORT_REQUEST_C2S` routing changes.
- No `QUICK_TELEPORT_BY_KEYBIND_C2S` routing changes.
- No `PendingTeleportManager` behavior changes.

## Known Risks

- The default handler is registered but not used by gameplay yet.
- Actual execution must be wired through a later request-service phase without duplicating payment, cooldown, scroll consumption, or quick favorite logic.
- The repository has no usable HEAD and most files are untracked, so `git diff` is limited for new-file visibility.

## Report

`Notes/REPORTS&AUDITS/PHASE_MRP_API_03_DEFAULT_HANDLER_REGISTRATION_REPORT.md`

## Next Phase

PHASE_MRP_API_04_TELEPORT_REQUEST_SERVICE_DESIGN
