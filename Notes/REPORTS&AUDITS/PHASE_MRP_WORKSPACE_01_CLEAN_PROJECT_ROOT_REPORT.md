# PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT - CLEAN_PROJECT_ROOT

## Status
STATUS: PASS_WORKSPACE_ORGANIZED

## Goal
Clean and reorganize the local project root into a stable workspace layout for the original/base MR-Portal Fabric mod, future standalone MR-Portal addons, the future Chrono addon placeholder, and shared Notes containing reports, audits, architecture notes, and an Obsidian canvas board.

This phase was filesystem organization and project tracking setup only. No MR-Portal API changes, Chrono addon implementation, Java gameplay changes, GitHub repository creation, staging, or commits were performed.

Correction note from `PHASE_MRP_WORKSPACE_02_FIX_NOTES_LAYOUT`: this report has been moved to `MR-portal/Notes/REPORTS&AUDITS/`. Root-level `Notes/` is no longer used for active project tracking. The current Chrono repository is `sqwiziiy/Chrono-Portal`; the previous phase 01 Chrono repo suggestion is superseded.

## Initial workspace audit

### pwd
```text
/home/mentality/scripts/java_minecraft_mod_prortal_from_mr
```

### Top-level listing before organization
```text
./.git
./.gitattributes
./.github
./.gitignore
./.gradle
./.venv
./.vscode
./LICENSE
./build
./build.gradle
./changelog_1.0.md
./changelog_1.1.md
./changelog_1.2.md
./changelog_1.3.md
./changelog_1.4.md
./changelog_1.5.md
./changelog_1.6.md
./changelog_1.7.md
./changelog_1.7hotfix.md
./changelog_1.8.md
./changelog_1.9.md
./changelog_2.0.md
./changelog_2.1.md
./changelog_2.2.md
./gradle
./gradle.properties
./gradlew
./gradlew.bat
./idea.txt
./old_data
./readme.md
./readme_en.md
./settings.gradle
./src
./пример.txt
```

### Git repo roots found
```text
./.git
```

The workspace root itself was a Git repo before organization.

### Gradle/Fabric files found
```text
./build.gradle
./build/resources/main/fabric.mod.json
./gradle.properties
./settings.gradle
./src/main/resources/fabric.mod.json
```

### Loose readme/changelog/markdown files found
```text
./changelog_1.0.md
./changelog_1.1.md
./changelog_1.2.md
./changelog_1.3.md
./changelog_1.4.md
./changelog_1.5.md
./changelog_1.6.md
./changelog_1.7.md
./changelog_1.7hotfix.md
./changelog_1.8.md
./changelog_1.9.md
./changelog_2.0.md
./changelog_2.1.md
./changelog_2.2.md
./old_data/readme_1.6.md
./old_data/readme_1.7hotfix.md
./old_data/readme_1.8.0.md
./old_data/readme_1.9.0.md
./old_data/readme_2.0.0.md
./old_data/readme_2.1.0.md
./old_data/readme_en_1.6.md
./old_data/readme_en_1.7hotfix.md
./old_data/readme_en_1.8.0.md
./old_data/readme_en_1.9.0.md
./old_data/readme_en_2.0.0.md
./old_data/readme_en_2.1.0.md
./readme.md
./readme_en.md
```

### Existing target folders before organization
- `MR-portal/`: not present before this phase
- `Add-ons/`: not present before this phase
- `Add-ons/Chrono/`: not present before this phase
- `Notes/`: not present before this phase

### Loose files found at workspace root
```text
LICENSE
build.gradle
changelog_1.0.md
changelog_1.1.md
changelog_1.2.md
changelog_1.3.md
changelog_1.4.md
changelog_1.5.md
changelog_1.6.md
changelog_1.7.md
changelog_1.7hotfix.md
changelog_1.8.md
changelog_1.9.md
changelog_2.0.md
changelog_2.1.md
changelog_2.2.md
gradle.properties
gradlew
gradlew.bat
idea.txt
readme.md
readme_en.md
settings.gradle
пример.txt
```

### Files that looked like old changelogs/readmes/notes
- `changelog_1.0.md` through `changelog_2.2.md`
- `old_data/`
- `idea.txt`
- `пример.txt`

### Ambiguous files moved to Notes/ARCHIVE/
- `idea.txt`
- `пример.txt`
- `old_data/`
- `changelog_*.md`

## Detected MR-Portal repo
- Path before organization: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr`
- Path after organization: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `master`
- HEAD: unavailable; `git rev-parse --short HEAD` failed with `fatal: Needed a single revision`
- Remote URL: none configured; `git remote -v` returned no output

### Git status before moving
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

### Git status after moving
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

## Files/folders moved
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/.git` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/.git`: preserve Git repo inside base mod folder.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/.gitattributes` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/.gitattributes`: base mod repo file.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/.github` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/.github`: base mod repo file.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/.gitignore` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/.gitignore`: base mod repo file.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/.gradle` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/.gradle`: existing Gradle project state kept with base project.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/.venv` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/.venv`: existing local workspace environment kept with base project rather than deleted.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/.vscode` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/.vscode`: existing editor settings kept with base project.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/LICENSE` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/LICENSE`: base mod repo file.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/build` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/build`: existing build output kept with base project; not moved to Notes or Add-ons.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/build.gradle` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/build.gradle`: base Fabric Gradle project file.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/gradle` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/gradle`: Gradle wrapper files.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/gradle.properties` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/gradle.properties`: base Fabric Gradle project file.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/gradlew` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/gradlew`: Gradle wrapper script.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/gradlew.bat` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/gradlew.bat`: Gradle wrapper script.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/readme.md` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/readme.md`: current base mod README.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/readme_en.md` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/readme_en.md`: current base mod README.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/settings.gradle` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/settings.gradle`: base Fabric Gradle project file.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/src` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal/src`: base mod source tree.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/changelog_*.md` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/Notes/ARCHIVE/`: historical loose changelog files.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/old_data` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/Notes/ARCHIVE/old_data`: old README copies.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/idea.txt` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/Notes/ARCHIVE/idea.txt`: loose planning note.
- `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/пример.txt` -> `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/Notes/ARCHIVE/пример.txt`: ambiguous loose note.

## Final workspace layout
Command: `find . -maxdepth 3 -mindepth 1 -print | sort`

```text
./Add-ons
./Add-ons/Chrono
./Add-ons/Chrono/README.md
./MR-portal
./MR-portal/.git
./MR-portal/.git/HEAD
./MR-portal/.git/config
./MR-portal/.git/description
./MR-portal/.git/hooks
./MR-portal/.git/info
./MR-portal/.git/objects
./MR-portal/.git/refs
./MR-portal/.gitattributes
./MR-portal/.github
./MR-portal/.github/workflows
./MR-portal/.gitignore
./MR-portal/.gradle
./MR-portal/.gradle/8.8
./MR-portal/.gradle/9.3.0
./MR-portal/.gradle/buildOutputCleanup
./MR-portal/.gradle/file-system.probe
./MR-portal/.gradle/loom-cache
./MR-portal/.gradle/vcs-1
./MR-portal/.venv
./MR-portal/.venv/.gitignore
./MR-portal/.venv/bin
./MR-portal/.venv/include
./MR-portal/.venv/lib
./MR-portal/.venv/lib64
./MR-portal/.venv/pyvenv.cfg
./MR-portal/.vscode
./MR-portal/.vscode/settings.json
./MR-portal/LICENSE
./MR-portal/build
./MR-portal/build.gradle
./MR-portal/build/classes
./MR-portal/build/devlibs
./MR-portal/build/generated
./MR-portal/build/libs
./MR-portal/build/processIncludeJars
./MR-portal/build/resources
./MR-portal/build/tmp
./MR-portal/gradle
./MR-portal/gradle.properties
./MR-portal/gradle/wrapper
./MR-portal/gradlew
./MR-portal/gradlew.bat
./MR-portal/readme.md
./MR-portal/readme_en.md
./MR-portal/settings.gradle
./MR-portal/src
./MR-portal/src/client
./MR-portal/src/main
./Notes
./Notes/ARCHIVE
./Notes/ARCHIVE/changelog_1.0.md
./Notes/ARCHIVE/changelog_1.1.md
./Notes/ARCHIVE/changelog_1.2.md
./Notes/ARCHIVE/changelog_1.3.md
./Notes/ARCHIVE/changelog_1.4.md
./Notes/ARCHIVE/changelog_1.5.md
./Notes/ARCHIVE/changelog_1.6.md
./Notes/ARCHIVE/changelog_1.7.md
./Notes/ARCHIVE/changelog_1.7hotfix.md
./Notes/ARCHIVE/changelog_1.8.md
./Notes/ARCHIVE/changelog_1.9.md
./Notes/ARCHIVE/changelog_2.0.md
./Notes/ARCHIVE/changelog_2.1.md
./Notes/ARCHIVE/changelog_2.2.md
./Notes/ARCHIVE/idea.txt
./Notes/ARCHIVE/old_data
./Notes/ARCHIVE/пример.txt
./Notes/OBSIDIAN_BOARD
./Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md
./Notes/OBSIDIAN_BOARD/API_DESIGN.md
./Notes/OBSIDIAN_BOARD/PHASES
./Notes/OBSIDIAN_BOARD/README.md
./Notes/OBSIDIAN_BOARD/mrportalboard.canvas
./Notes/REPORTS&AUDITS
./Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md
```

## Notes structure
Created files:
- `Notes/REPORTS&AUDITS/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT_REPORT.md`
- `Notes/OBSIDIAN_BOARD/README.md`
- `Notes/OBSIDIAN_BOARD/API_DESIGN.md`
- `Notes/OBSIDIAN_BOARD/ADDON_ARCHITECTURE.md`
- `Notes/OBSIDIAN_BOARD/PHASES/PHASE_MRP_WORKSPACE_01_CLEAN_PROJECT_ROOT.md`
- `Notes/OBSIDIAN_BOARD/mrportalboard.canvas`

Archive contents:
- `Notes/ARCHIVE/changelog_1.0.md` through `Notes/ARCHIVE/changelog_2.2.md`
- `Notes/ARCHIVE/old_data/`
- `Notes/ARCHIVE/idea.txt`
- `Notes/ARCHIVE/пример.txt`

## Add-ons structure
- `Add-ons/` was created.
- `Add-ons/Chrono/` was created.
- `Add-ons/Chrono/README.md` was created as a placeholder.

## GitHub repository note
- Base repo: `sqwiziiy/MR-portal`
- Future Chrono addon repo correction: `sqwiziiy/Chrono-Portal`
- No GitHub repo was created in this phase.

## Validation

### Directory checks
```text
MR-portal: ok
Add-ons: ok
Add-ons/Chrono: ok
Notes/REPORTS&AUDITS: ok
Notes/OBSIDIAN_BOARD: ok
Notes/OBSIDIAN_BOARD/PHASES: ok
Notes/ARCHIVE: ok
```

### MR-portal Git checks
```text
$ git -C MR-portal status --short
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

$ git -C MR-portal branch --show-current
master

$ git -C MR-portal rev-parse --short HEAD
fatal: Needed a single revision

$ git -C MR-portal remote -v
```

No remote output was returned.

### Gradle compile validation
`MR-portal` is confirmed as a Gradle Fabric project, so compile validation was attempted.

Command: `cd MR-portal && ./gradlew compileJava`

Result: failed during Gradle configuration.

```text
FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring root project 'MR-portal'.
> Could not resolve all artifacts for configuration 'classpath'.
   > Could not resolve net.fabricmc:fabric-loom:1.15.5.
     Required by:
         buildscript of root project 'MR-portal' > net.fabricmc.fabric-loom-remap:net.fabricmc.fabric-loom-remap.gradle.plugin:1.15-SNAPSHOT:20260313.091358-5
      > Dependency requires at least JVM runtime version 21. This build uses a Java 17 JVM.
```

Command: `cd MR-portal && ./gradlew compileClientJava`

Result: failed during Gradle configuration.

```text
FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring root project 'MR-portal'.
> Could not resolve all artifacts for configuration 'classpath'.
   > Could not resolve net.fabricmc:fabric-loom:1.15.5.
     Required by:
         buildscript of root project 'MR-portal' > net.fabricmc.fabric-loom-remap:net.fabricmc.fabric-loom-remap.gradle.plugin:1.15-SNAPSHOT:20260313.091358-5
      > Dependency requires at least JVM runtime version 21. This build uses a Java 17 JVM.
```

No build pass is claimed in this phase.

### Canvas validation
```text
canvas json: ok
duplicate node ids: none
duplicate edge ids: none
node overlaps: none
```

## Known risks
- The detected Git repo has no commit yet, so HEAD cannot be resolved.
- No remote is configured locally, despite the project context naming `sqwiziiy/MR-portal`.
- All active base mod source/project files still appear untracked in the moved Git repo.
- Gradle compile checks fail because the currently used JVM is Java 17 while the resolved Fabric Loom dependency requires Java 21 or newer.
- Historical changelog/readme/note files were archived based on naming and folder context; they were not deleted.

## Next recommended phase
PHASE_MRP_API_01_AUDIT_TELEPORT_FLOW
