# PHASE_MRP_WORKSPACE_03_SPLIT_BASE_AND_ADDON_NOTES - SPLIT_BASE_AND_ADDON_NOTES

## Status
STATUS: PASS_NOTES_SPLIT_FIXED

## Goal
Fix Notes ownership after `PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT` so base MR-Portal notes stay under `MR-portal/Notes/` and Chrono/addon-specific notes live under `Add-ons/Chrono/Notes/`.

This phase was documentation and workspace layout only. No Java source code was edited. No files were staged or committed.

## Audit

Workspace root:
```text
/home/mentality/scripts/java_minecraft_mod_prortal_from_mr
```

Base MR-Portal-specific files found:
- `MR-portal/Notes/OBSIDIAN_BOARD/API_DESIGN.md`: base API planning for API code inside `mr-portal.jar`.
- `MR-portal/Notes/OBSIDIAN_BOARD/README.md`: base board policy.
- `MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas`: mixed before correction; now base-only plus a Chrono pointer.
- `MR-portal/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT.md`: base workspace phase note.
- `MR-portal/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT.md`: base workspace phase note.
- `MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md`: historical/base workspace report.
- `MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT_REPORT.md`: historical/base workspace report.
- `MR-portal/пример.txt`: reusable workspace prompt template/example.

Chrono/addon-specific files found:
- `MR-portal/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md`: addon/Chrono architecture note; moved out of base notes.
- `Add-ons/Chrono/README.md`: Chrono placeholder and identity source.
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/README.md`: Chrono board README.
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/CHRONO_DESIGN.md`: Chrono addon design note.
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/chronoportalboard.canvas`: Chrono board.

Mixed content found:
- `MR-portal/Notes/OBSIDIAN_BOARD/API_DESIGN.md`: stays with base because API work is implemented in the base mod; wording now points addon docs to Chrono notes.
- `MR-portal/Notes/OBSIDIAN_BOARD/README.md`: stays with base as policy and pointer; now says base-only.
- `MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas`: was mixed; rewritten as a base board with a Chrono notes pointer only.
- `MR-portal/пример.txt`: stays with base/project template; updated with separate base, Chrono, and future addon rules.
- Historical reports mention earlier root `Notes/ARCHIVE/` and the old Chrono repo only as correction history.

## Files moved
- `MR-portal/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md` -> `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md`

## Files updated
- `MR-portal/Notes/OBSIDIAN_BOARD/README.md`
- `MR-portal/Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas`
- `MR-portal/пример.txt`
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/README.md`
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md`
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/CHRONO_DESIGN.md`
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_03_SPLIT_BASE_AND_ADDON_NOTES.md`
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/chronoportalboard.canvas`
- `Add-ons/Chrono/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_03_SPLIT_BASE_AND_ADDON_NOTES_REPORT.md`

## Final layout
```text
MR-portal/Notes/
  REPORTS&AUDITS/
    PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md
    PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT_REPORT.md
  OBSIDIAN_BOARD/
    README.md
    API_DESIGN.md
    PHASES/
      PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT.md
      PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT.md
    mrportalboard.canvas

Add-ons/Chrono/Notes/
  REPORTS&AUDITS/
    PHASE_MRP_WORKSPACE_03_SPLIT_BASE_AND_ADDON_NOTES_REPORT.md
  OBSIDIAN_BOARD/
    README.md
    ADDON_ARCHITECTURE.md
    CHRONO_DESIGN.md
    PHASES/
      PHASE_MRP_WORKSPACE_03_SPLIT_BASE_AND_ADDON_NOTES.md
    chronoportalboard.canvas
```

Root-level `Notes/` remains absent.

## Base notes policy
`MR-portal/Notes/` is only for base MR-Portal work:
- API work inside the original/base mod.
- Audits of MR-Portal source code.
- Reports for modifying MR-Portal itself.
- Base MR-Portal canvas/board and base phase notes.

Base API docs remain here because the API is implemented inside `mr-portal.jar`.

## Chrono notes policy
`Add-ons/Chrono/Notes/` is only for Chrono Portal addon work:
- Chrono addon architecture.
- Chrono addon design.
- Chrono addon reports/audits.
- Chrono addon canvas/board.
- Files about the separate Chrono addon repo/project.

Chrono identity:
- Repository: `sqwiziiy/Chrono-Portal`
- Mod id: `chrono_portal`
- Package: `com.mentality.mrportalchrono`
- Relative path from Chrono root to base MR-Portal: `../../MR-portal/`

## Future addon notes policy
Future addons own their own notes:
- Root: `Add-ons/<AddonName>/`
- Reports: `Add-ons/<AddonName>/Notes/REPORTS&AUDITS/`
- Board: `Add-ons/<AddonName>/Notes/OBSIDIAN_BOARD/`

Do not create shared root reports, shared root canvases, root-level `Notes/`, or root-level `Notes/ARCHIVE/` for active project tracking.

## Validation commands/results

Directory checks:
```text
pwd: /home/mentality/scripts/java_minecraft_mod_prortal_from_mr
MR-portal/Notes/REPORTS&AUDITS: ok
MR-portal/Notes/OBSIDIAN_BOARD: ok
Add-ons/Chrono/Notes/REPORTS&AUDITS: ok
Add-ons/Chrono/Notes/OBSIDIAN_BOARD: ok
root Notes absent: ok
Add-ons/Chrono/../../MR-portal: ok
MR-portal/../Add-ons/Chrono: ok
```

JSON checks:
```text
python3 -m json.tool MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas >/dev/null: ok
python3 -m json.tool Add-ons/Chrono/Notes/OBSIDIAN_BOARD/chronoportalboard.canvas >/dev/null: ok
```

Find docs:
```text
MR-portal/Notes/OBSIDIAN_BOARD/API_DESIGN.md
MR-portal/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT.md
MR-portal/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT.md
MR-portal/Notes/OBSIDIAN_BOARD/README.md
MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas
MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md
MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT_REPORT.md

Add-ons/Chrono/Notes/OBSIDIAN_BOARD/.obsidian/app.json
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/.obsidian/appearance.json
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/.obsidian/core-plugins.json
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/.obsidian/workspace.json
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/CHRONO_DESIGN.md
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_03_SPLIT_BASE_AND_ADDON_NOTES.md
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/README.md
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/chronoportalboard.canvas
Add-ons/Chrono/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_03_SPLIT_BASE_AND_ADDON_NOTES_REPORT.md
```

Grep checks:
- `sqwiziiy/MR-Portal-Chrono`: only historical correction mentions remain in phase 02 report.
- `MR-portal/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE`: only historical report/audit mentions remain; no active file remains there.
- `root Notes`: only historical/correction context remains.
- `Notes/ARCHIVE`: only historical/correction context and explicit forbidden-policy text remain.
- `Chrono-Portal`: present in Chrono docs, Chrono README, and corrected policy/template references.
- `chrono_portal`: present in Chrono docs, Chrono README, and corrected prompt template.
- `../../MR-portal`: present in Chrono docs and canvas.

## Canvas validation

Base canvas: `MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas`
```text
json valid: yes
duplicate node ids: none
duplicate edge ids: none
broken file node paths: none
overlap warnings: none
```

Chrono canvas: `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/chronoportalboard.canvas`
```text
json valid: yes
duplicate node ids: none
duplicate edge ids: none
broken file node paths: none
overlap warnings: none
```

## Git status

MR-portal:
```text
?? .gitattributes
?? .github/
?? .gitignore
?? LICENSE
?? Notes/
?? build.gradle
?? changelog_1.0.md
?? changelog_1.1.md
?? changelog_1.2.md
?? changelog_1.3.md
?? changelog_1.4.md
?? changelog_1.5.md
?? changelog_1.6.md
?? changelog_1.7.md
?? changelog_1.7hotfix.md
?? changelog_1.8.md
?? changelog_1.9.md
?? changelog_2.0.md
?? changelog_2.1.md
?? changelog_2.2.md
?? gradle.properties
?? gradle/
?? gradlew
?? gradlew.bat
?? old_data/
?? readme.md
?? readme_en.md
?? settings.gradle
?? src/
?? "\320\277\321\200\320\270\320\274\320\265\321\200.txt"
```

Add-ons/Chrono:
```text
fatal: not a git repository (or any parent up to mount point /)
Stopping at filesystem boundary (GIT_DISCOVERY_ACROSS_FILESYSTEM not set).
```

## Next recommended phase
PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW
