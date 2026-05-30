# PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT - FIX_NOTES_LAYOUT

## Status
STATUS: PASS_LAYOUT_FIXED

## Goal
Correct the Notes layout so each project owns its own project-tracking files.

- Base MR-Portal notes live under `MR-portal/Notes/`.
- Chrono addon notes live under `Add-ons/Chrono/Notes/`.
- Future addon notes live under `Add-ons/<AddonName>/Notes/`.
- Root-level `Notes/` and root-level `Notes/ARCHIVE/` are not used for active project tracking.

This phase updated filesystem/project-tracking layout and the saved prompt template/example only. No MR-Portal API changes, Chrono addon implementation, Java gameplay changes, staging, commits, or GitHub repository creation were performed.

## Audit

### Root workspace listing before fix
```text
./Add-ons
./Add-ons/Chrono
./Add-ons/Chrono/README.md
./MR-portal
./MR-portal/.git
./MR-portal/.gitattributes
./MR-portal/.github
./MR-portal/.gitignore
./MR-portal/.gradle
./MR-portal/.venv
./MR-portal/.vscode
./MR-portal/LICENSE
./MR-portal/build
./MR-portal/build.gradle
./MR-portal/gradle
./MR-portal/gradle.properties
./MR-portal/gradlew
./MR-portal/gradlew.bat
./MR-portal/readme.md
./MR-portal/readme_en.md
./MR-portal/settings.gradle
./MR-portal/src
./Notes
./Notes/ARCHIVE
./Notes/OBSIDIAN_BOARD
./Notes/REPORTS&AUDITS
```

### Root Notes contents before fix
```text
Notes/ARCHIVE/changelog_1.0.md
Notes/ARCHIVE/changelog_1.1.md
Notes/ARCHIVE/changelog_1.2.md
Notes/ARCHIVE/changelog_1.3.md
Notes/ARCHIVE/changelog_1.4.md
Notes/ARCHIVE/changelog_1.5.md
Notes/ARCHIVE/changelog_1.6.md
Notes/ARCHIVE/changelog_1.7.md
Notes/ARCHIVE/changelog_1.7hotfix.md
Notes/ARCHIVE/changelog_1.8.md
Notes/ARCHIVE/changelog_1.9.md
Notes/ARCHIVE/changelog_2.0.md
Notes/ARCHIVE/changelog_2.1.md
Notes/ARCHIVE/changelog_2.2.md
Notes/ARCHIVE/idea.txt
Notes/ARCHIVE/old_data/readme_1.6.md
Notes/ARCHIVE/old_data/readme_1.7hotfix.md
Notes/ARCHIVE/old_data/readme_1.8.0.md
Notes/ARCHIVE/old_data/readme_1.9.0.md
Notes/ARCHIVE/old_data/readme_2.0.0.md
Notes/ARCHIVE/old_data/readme_2.1.0.md
Notes/ARCHIVE/old_data/readme_en_1.6.md
Notes/ARCHIVE/old_data/readme_en_1.7hotfix.md
Notes/ARCHIVE/old_data/readme_en_1.8.0.md
Notes/ARCHIVE/old_data/readme_en_1.9.0.md
Notes/ARCHIVE/old_data/readme_en_2.0.0.md
Notes/ARCHIVE/old_data/readme_en_2.1.0.md
Notes/ARCHIVE/пример.txt
Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md
Notes/OBSIDIAN_BOARD/API_DESIGN.md
Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT.md
Notes/OBSIDIAN_BOARD/README.md
Notes/OBSIDIAN_BOARD/mrportalboard.canvas
Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md
```

### MR-portal docs/notes before fix
```text
MR-portal/readme.md
MR-portal/readme_en.md
```

`MR-portal/Notes/` did not exist before this phase.

### Add-ons/Chrono contents before fix
```text
Add-ons/Chrono/README.md
```

`Add-ons/Chrono/Notes/` did not exist before this phase. `Add-ons/Chrono` was not a Git repo.

### Git status before fix
MR-portal:
```text
?? .gitattributes
?? .github/
?? .gitignore
?? LICENSE
?? build.gradle
?? gradle.properties
?? gradle/
?? gradlew
?? gradlew.bat
?? readme.md
?? readme_en.md
?? settings.gradle
?? src/
```

Add-ons/Chrono:
```text
fatal: not a git repository (or any parent up to mount point /)
Stopping at filesystem boundary (GIT_DISCOVERY_ACROSS_FILESYSTEM not set).
```

### Ownership classification
- `Notes/REPORTS&AUDITS/*` clearly belonged to base MR-Portal project tracking.
- `Notes/OBSIDIAN_BOARD/*` clearly belonged to base MR-Portal project tracking.
- `Notes/ARCHIVE/changelog_*.md`, `old_data/`, `idea.txt`, and `пример.txt` were MR-Portal historical docs/templates and belonged back under `MR-portal/`.
- No root Notes files clearly belonged to Chrono at audit time.
- No ambiguous files remained after classification.

## Files/folders moved
- `Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md` -> `MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md`: base MR-Portal report.
- `Notes/OBSIDIAN_BOARD/README.md` -> `MR-portal/Notes/OBSIDIAN_BOARD/README.md`: base MR-Portal board file.
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md` -> `MR-portal/Notes/OBSIDIAN_BOARD/API_DESIGN.md`: base MR-Portal API planning.
- `Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md` -> `MR-portal/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md`: base MR-Portal addon architecture planning.
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT.md` -> `MR-portal/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT.md`: base MR-Portal phase note.
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas` -> `MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas`: base MR-Portal board.
- `Notes/ARCHIVE/changelog_*.md` -> `MR-portal/changelog_*.md`: restored MR-Portal historical changelogs.
- `Notes/ARCHIVE/old_data/` -> `MR-portal/old_data/`: restored MR-Portal historical readmes.
- `Notes/ARCHIVE/idea.txt` -> `MR-portal/idea.txt`: restored MR-Portal planning note.
- `Notes/ARCHIVE/пример.txt` -> `MR-portal/пример.txt`: restored saved prompt template/example.
- `Notes/` was removed with `rmdir` after safe migration. One `rmdir` call attempted `Notes/OBSIDIAN_BOARD/PHASES` after it had already been moved; this produced a harmless `No such file or directory` message and root `Notes/` was verified absent afterward.

No destination conflicts were encountered.

## Files updated
- `MR-portal/пример.txt`: rewritten as a corrected reusable prompt template.
- `MR-portal/Notes/OBSIDIAN_BOARD/README.md`: updated for project-owned Notes policy.
- `MR-portal/Notes/OBSIDIAN_BOARD/API_DESIGN.md`: updated to state base notes/addon notes policy.
- `MR-portal/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md`: updated with correct Chrono repo, mod id, package, and relative paths.
- `MR-portal/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT.md`: updated to note that phase 01 root Notes layout was superseded.
- `MR-portal/Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT.md`: created.
- `MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas`: updated paths and phase 02 nodes/edges.
- `MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md`: updated with correction note and Chrono repo correction.
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/README.md`: created.
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/CHRONO_DESIGN.md`: created.
- `Add-ons/Chrono/Notes/OBSIDIAN_BOARD/chronoportalboard.canvas`: created.

`Add-ons/Chrono/README.md` was read but not changed.

## Corrected layout
Command: `find . -maxdepth 4 -mindepth 1 -print | sort`

Key corrected layout:
```text
./Add-ons
./Add-ons/Chrono
./Add-ons/Chrono/Notes
./Add-ons/Chrono/Notes/OBSIDIAN_BOARD
./Add-ons/Chrono/Notes/REPORTS&AUDITS
./Add-ons/Chrono/README.md
./MR-portal
./MR-portal/Notes
./MR-portal/Notes/OBSIDIAN_BOARD
./MR-portal/Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md
./MR-portal/Notes/OBSIDIAN_BOARD/API_DESIGN.md
./MR-portal/Notes/OBSIDIAN_BOARD/PHASES
./MR-portal/Notes/OBSIDIAN_BOARD/README.md
./MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas
./MR-portal/Notes/REPORTS&AUDITS
./MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md
./MR-portal/Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT_REPORT.md
./MR-portal/changelog_1.0.md
./MR-portal/changelog_1.1.md
./MR-portal/changelog_1.2.md
./MR-portal/changelog_1.3.md
./MR-portal/changelog_1.4.md
./MR-portal/changelog_1.5.md
./MR-portal/changelog_1.6.md
./MR-portal/changelog_1.7.md
./MR-portal/changelog_1.7hotfix.md
./MR-portal/changelog_1.8.md
./MR-portal/changelog_1.9.md
./MR-portal/changelog_2.0.md
./MR-portal/changelog_2.1.md
./MR-portal/changelog_2.2.md
./MR-portal/idea.txt
./MR-portal/old_data
./MR-portal/readme.md
./MR-portal/readme_en.md
./MR-portal/пример.txt
```

The full command output also includes unchanged base mod source, Gradle, build, and Git internals under `MR-portal/`.

## Path policy
- Base MR-Portal notes: `MR-portal/Notes/`
- Chrono addon notes: `Add-ons/Chrono/Notes/`
- Future addon notes: `Add-ons/<AddonName>/Notes/`
- No root `Notes/`
- No root `Notes/ARCHIVE/`

## Chrono repository correction
- Correct repo: `sqwiziiy/Chrono-Portal`
- Old suggested repo corrected from phase 01: `sqwiziiy/MR-Portal-Chrono`
- Corrected references in active board/design/template files.
- `grep -R "sqwiziiy/MR-Portal-Chrono" -n MR-portal Add-ons || true` returned no output before this final report was completed; the only remaining mention is this correction record.

## Relative path policy
- From `Add-ons/Chrono/` to `MR-portal/`: `../../MR-portal/`
- From `MR-portal/` to `Add-ons/Chrono/`: `../Add-ons/Chrono/`

Validated with:
```text
Chrono to MR-portal relative path: ok
MR-portal to Chrono relative path: ok
```

## Validation
```text
MR-portal/Notes/REPORTS&AUDITS: ok
MR-portal/Notes/OBSIDIAN_BOARD: ok
Add-ons/Chrono/Notes/REPORTS&AUDITS: ok
Add-ons/Chrono/Notes/OBSIDIAN_BOARD: ok
root Notes absent: ok
Add-ons/Chrono: ok
MR-portal: ok
Chrono to MR-portal relative path: ok
MR-portal to Chrono relative path: ok
mrportal canvas json: ok
chrono canvas json: ok
```

### Grep checks
`grep -R "sqwiziiy/MR-Portal-Chrono" -n MR-portal Add-ons || true`

Result before final report completion: no output.

`grep -R "/Notes/REPORTS&AUDITS" -n MR-portal Add-ons || true`

Result: only project-owned `MR-portal/Notes/REPORTS&AUDITS/` and `Add-ons/Chrono/Notes/REPORTS&AUDITS/` references, plus historical pre-fix audit lines inside reports.

`grep -R "Notes/ARCHIVE" -n MR-portal Add-ons || true`

Result: references remain only as historical phase 01/phase 02 correction context or explicit statements that root-level `Notes/ARCHIVE/` is not part of the workflow.

`grep -R "Chrono-Portal" -n MR-portal Add-ons || true`

Result: relevant Chrono repo references present in base addon architecture notes, the prompt template, Chrono notes, and Chrono README.

## Canvas validation
Base MR-Portal canvas:
```text
MR-portal/Notes/OBSIDIAN_BOARD/mrportalboard.canvas
json valid: yes
duplicate node ids: none
duplicate edge ids: none
broken file node paths: none
node overlaps: none
```

Chrono canvas:
```text
Add-ons/Chrono/Notes/OBSIDIAN_BOARD/chronoportalboard.canvas
json valid: yes
duplicate node ids: none
duplicate edge ids: none
broken file node paths: none
node overlaps: none
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
