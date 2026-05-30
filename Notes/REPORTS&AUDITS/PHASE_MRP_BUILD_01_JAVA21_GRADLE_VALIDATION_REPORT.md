# PHASE_MRP_BUILD_01_JAVA21_GRADLE_VALIDATION - JAVA21_GRADLE_VALIDATION

## Status
STATUS: PASS_BUILD_VALIDATED

## Goal
Validate the local Gradle runtime setup after `PHASE_MRP_API_02_PUBLIC_HANDLER_API`, where compile validation was blocked because Fabric Loom requires Java 21 while Gradle was run with Java 17.

This phase validated that the new public API source compiles when Gradle runs on Java 21. No gameplay routing, teleport behavior, Chrono code, or Java source/target compatibility was changed.

## Baseline
- Root: `/home/mentality/scripts/java_minecraft_mod_prortal_from_mr/MR-portal`
- Branch: `2.3`
- HEAD: unavailable, `fatal: Needed a single revision`
- Remote:
```text
origin	https://github.com/sqwiziiy/MR-Portal (fetch)
origin	https://github.com/sqwiziiy/MR-Portal (push)
```
- Git status summary: repository remains mostly untracked, including `Notes/`, Gradle files, and `src/`.
- Previous blocked compile reason: Fabric Loom requires Java 21 runtime; previous Gradle run used Java 17.

## Java audit
Default Java:
```text
openjdk version "17.0.19" 2026-04-21
OpenJDK Runtime Environment (build 17.0.19+10)
OpenJDK 64-Bit Server VM (build 17.0.19+10, mixed mode, sharing)
```

Default javac:
```text
javac 17.0.19
```

`JAVA_HOME`:
```text

```

Paths:
```text
which java: /usr/bin/java
which javac: /usr/bin/javac
```

Installed JVMs:
```text
/usr/lib/jvm/default
/usr/lib/jvm/default-runtime
/usr/lib/jvm/java-17-openjdk
/usr/lib/jvm/java-21-openjdk
/usr/lib/jvm/java-26-openjdk
```

`archlinux-java status`:
```text
Available Java environments:
  java-17-openjdk (default)
  java-21-openjdk
  java-26-openjdk
```

Default `./gradlew --version` audit:
```text
Exception in thread "main" java.io.FileNotFoundException: /home/mentality/.gradle/wrapper/dists/gradle-9.3.0-bin/79n14ral3mx1ozqr3csh2u872/gradle-9.3.0-bin.zip.lck (Read-only file system)
```

This was a sandbox filesystem restriction while the Gradle wrapper tried to access `~/.gradle`. The Java 21 Gradle commands were rerun with approved access.

## Build settings audit
Relevant settings:
```text
build.gradle:2: id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"
build.gradle:75: sourceCompatibility = JavaVersion.VERSION_17
build.gradle:76: targetCompatibility = JavaVersion.VERSION_17
gradle.properties:12: loom_version=1.15-SNAPSHOT
```

Source target remains Java 17. No build file changes were made.

## Actions taken
- Used `JAVA_HOME=/usr/lib/jvm/java-21-openjdk` for Gradle commands.
- Did not run `archlinux-java set`; system default Java remains Java 17.
- Did not edit Java source files.
- Did not edit build files.
- API source files from `PHASE_MRP_API_02_PUBLIC_HANDLER_API` needed no fixes.

## Validation
Java 21 Gradle runtime:
```text
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew --version

Gradle 9.3.0
Launcher JVM:  21.0.11 (Arch Linux 21.0.11+10)
Daemon JVM:    /usr/lib/jvm/java-21-openjdk
```

Compile:
```text
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileJava
> Task :compileJava
BUILD SUCCESSFUL in 10s
```

Client compile:
```text
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew compileClientJava
> Task :compileJava UP-TO-DATE
> Task :processResources
> Task :classes
> Task :compileClientJava
BUILD SUCCESSFUL in 1s
```

## Source changes
No source changes were made in this phase.

## Compatibility
- No gameplay routing changed.
- No `MRPortalNetworking` behavior changed.
- No `PendingTeleportManager` behavior changed.
- No Chrono code changed.
- No addon dependency added.
- Java source/target compatibility remains Java 17.

## Known risks
- The default system Java is still Java 17; Gradle must be invoked with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk` or an equivalent Java 21 runtime.
- The repository has no usable HEAD and remains mostly untracked, so Git diff output is limited.

## Next recommended phase
PHASE_MRP_API_03_DEFAULT_HANDLER_REGISTRATION
