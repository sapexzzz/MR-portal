# PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT

## Short name
CLEAN_PROJECT_ROOT

## Goal
Clean and reorganize the local project root into a stable workspace layout for the base MR-Portal mod, future standalone addons, the future Chrono addon placeholder, and shared project notes.

## Corrected note
The original phase 01 output placed project tracking under root-level `Notes/`. That layout was corrected by `PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT`.

Current policy:

- Base MR-Portal notes live under `MR-portal/Notes/`.
- Chrono notes live under `Add-ons/Chrono/Notes/`.
- Future addon notes live under `Add-ons/<AddonName>/Notes/`.
- Root-level `Notes/` is not used for active project tracking.

## Current workspace layout
```text
MR-portal/
  Notes/
    REPORTS&AUDITS/
    OBSIDIAN_BOARD/
Add-ons/
  Chrono/
    Notes/
      REPORTS&AUDITS/
      OBSIDIAN_BOARD/
```

## Files moved
- Base MR-Portal repo content moved from workspace root to `MR-portal/`.
- Historical MR-Portal docs were later restored to `MR-portal/`.
- Base reports and board files were later moved to `MR-portal/Notes/`.

## Report path
`MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md`

## Validation result
Workspace organization validation passed. Gradle compile validation was attempted, but both `compileJava` and `compileClientJava` failed during configuration because Fabric Loom requires a Java 21+ runtime while that run used Java 17.

## Next phase recommendation
PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW
