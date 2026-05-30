# MR Portal 2.3 Changelog

MR-Portal 2.3 is mainly an internal API/service-layer update that prepares the base mod for external teleport-style addons while preserving normal MR-Portal behavior.

## Main Changes

- Added public teleport handler API.
- Added `MRPortalApi`.
- Added `MRPortalTeleportHandler`.
- Added `MRPortalTeleportContext`.
- Added `MRPortalWaypointView`.
- Added `MRPortalTeleportActivator`.
- Added handler registration and active handler selection by priority.
- Added the default built-in handler for normal MR-Portal portal behavior.
- Routed GUI waypoint teleport requests through the new service/API path.
- Routed quick favorite teleport requests through the new service/API path.
- Preserved payment, cooldown, and resource validation in the base mod.
- Preserved regular staff pearl cost and cooldown.
- Preserved Teleport Scroll consumption behavior.
- Preserved Infinite Portal Staff no-cost behavior.
- Preserved same-dimension restrictions for regular staff and scroll.
- Added handler-controlled default preview spark hook through `usesDefaultPreviewSpark()`.
- The default handler keeps normal preview spark behavior.
- Addon handlers can opt out of base preview spark visuals.

## Fixes And Validation Highlights

- Fixed the GUI Teleport Scroll consumption route by making scroll/staff GUI mode server-owned.
- Fixed a quick favorite regular staff payment regression when a scroll was also present.
- Verified GUI staff and scroll behavior manually.
- Verified quick favorite staff and scroll behavior manually.
- Verified preview spark open and cleanup behavior manually.
- Removed temporary `[MRP07F]` diagnostic logs.

## Addon Support

- This update does not bundle Chrono Portal.
- It prepares the base mod for separate addons such as Chrono Portal.
- MR-Portal does not hardcode Chrono.
- Chrono should be a separate jar installed alongside MR-Portal.

## Developer Notes

- The base mod owns validation, payment, cooldown, and resource checks.
- Handlers own teleport execution style after validation.
- `usesDefaultPreviewSpark()` controls only default preview visuals, not payment or session execution.
- The default handler remains payment-free internally; payment is handled before handler execution.

## Known Notes

- Portal Staff cooldown disappearing after rejoin remains legacy vanilla cooldown behavior.
- Addon handler runtime validation is future work.
- Chrono implementation is separate/future.

## Dependencies And Build

- Fabric 1.20.1.
- Java 17 target.
- Java 21 Gradle runtime used in this workspace.
- Build validated with:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava
```
