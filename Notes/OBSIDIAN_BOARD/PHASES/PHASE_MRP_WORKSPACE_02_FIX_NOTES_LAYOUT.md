# PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT

## Short name
FIX_NOTES_LAYOUT

## Goal
Correct project-tracking layout so base MR-Portal notes live inside `MR-portal/Notes/`, Chrono notes live inside `Add-ons/Chrono/Notes/`, and root-level `Notes/` is not used.

## Corrected layout
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

## Moved files
- Root base reports moved to `MR-portal/Notes/REPORTS&AUDITS/`.
- Root base board files moved to `MR-portal/Notes/OBSIDIAN_BOARD/`.
- Historical MR-Portal changelogs, `old_data/`, `idea.txt`, and `пример.txt` restored to `MR-portal/`.
- Root-level `Notes/` removed after safe migration.

## Updated template/example path
`MR-portal/пример.txt`

## Report path
`MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT_REPORT.md`

## Validation result
Layout validation passed. Root-level `Notes/` is absent, project-owned Notes folders exist, relative paths validate, and both base and Chrono canvas files have valid JSON with no duplicate ids, broken file paths, or node overlaps.

## Next phase recommendation
PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW
