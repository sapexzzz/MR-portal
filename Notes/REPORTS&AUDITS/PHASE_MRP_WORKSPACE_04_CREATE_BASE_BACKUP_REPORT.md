# PHASE_MRP_WORKSPACE_04_CREATE_BASE_BACKUP - CREATE_BASE_BACKUP

## Status
STATUS: PASS_BACKUP_CREATED

## Goal
Create a full filesystem backup copy of the current base MR-Portal project before any API or source-code changes.

No Java source code, API implementation, staging, or commits were part of this phase.

## Audit

- Workspace root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr`
- `MR-portal/` exists: yes
- `Backup-MR-portal/` already existed before this phase: yes
- Timestamped backup required: yes
- `MR-portal/Notes/` at initial phase 04 audit time: missing
- `MR-portal/src/` exists: yes
- `MR-portal/build.gradle` exists: yes
- `MR-portal/Notes/` was restored before backup validation from existing misplaced Notes copies found under `Add-ons/Chrono/Notes/`, without deleting or modifying Chrono files.

### MR-portal git status
```text
?? .gitattributes
?? .github/
?? .gitignore
?? LICENSE
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

### MR-portal branch
```text
2.2
```

### MR-portal HEAD
```text
fatal: Needed a single revision
```

### MR-portal remote
```text
origin	https://github.com/sqwiziiy/MR-Portal (fetch)
origin	https://github.com/sqwiziiy/MR-Portal (push)
```

### Key files/folders found
- `MR-portal/src/`: yes
- `MR-portal/build.gradle`: yes
- `MR-portal/.git/`: yes
- `MR-portal/пример.txt`: yes
- `MR-portal/Notes/`: restored before backup

## Backup operation

Exact command used for the validated backup:
```text
cp -a MR-portal Backup-MR-portal-20260530-172501
```

Source path:
`/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/`

Destination path:
`/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/Backup-MR-portal-20260530-172501/`

Destination was timestamped because `Backup-MR-portal/` already existed. The existing `Backup-MR-portal/` was not overwritten or deleted.

Earlier timestamped copies, `Backup-MR-portal-20260530-172139/`, `Backup-MR-portal-20260530-172305/`, and `Backup-MR-portal-20260530-172414/`, were created before all required loose base docs and final phase 04 validation text were present. They were left untouched. The validated phase 04 backup snapshot is `Backup-MR-portal-20260530-172501/`.

## Backup validation

Validated after copying:
- `test -d Backup-MR-portal-20260530-172501`: pass
- `test -d Backup-MR-portal-20260530-172501/src`: pass
- `test -f Backup-MR-portal-20260530-172501/build.gradle`: pass
- `test -d Backup-MR-portal-20260530-172501/Notes`: pass
- `test -d Backup-MR-portal-20260530-172501/.git`: pass
- `test -f Backup-MR-portal-20260530-172501/пример.txt`: pass
- root-level `Notes/` absent: pass

File counts:
```text
find MR-portal -type f | wc -l: 1427
find Backup-MR-portal-20260530-172501 -type f | wc -l: 1427
```

Disk usage:
```text
139M	MR-portal
139M	Backup-MR-portal-20260530-172501
```

Canvas validation:
```text
json valid: yes
duplicate node ids: none
duplicate edge ids: none
broken file paths: none
overlap warnings: none
```

## Active working folder
Future API/source changes must be made in:
`/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/`

`Backup-MR-portal-20260530-172501/` is a frozen backup snapshot unless the user explicitly says otherwise.

## Notes policy
- Base reports remain under `MR-portal/Notes/REPORTS&AUDITS/`.
- Base board remains under `MR-portal/Notes/OBSIDIAN_BOARD/`.
- Chrono reports remain under `Add-ons/Chrono/Notes/REPORTS&AUDITS/`.
- Root-level `Notes/` remains forbidden.

## Known risks
- `Backup-MR-portal/` already existed, so this phase used a timestamped destination.
- `MR-portal/Notes/` was missing at initial phase 04 audit time and was restored before the backup copy so the backup contains the base Notes tree.
- `Add-ons/Chrono` is not a Git repository.
- `MR-portal` has no usable `HEAD`, so Git cannot identify a committed baseline.
- The existing `Backup-MR-portal/` appears to be an earlier backup and was not overwritten.
- Earlier timestamped backup attempts remain on disk and were not deleted.

## Next recommended phase
PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW
