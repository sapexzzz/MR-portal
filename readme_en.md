# MR Portal

MR Portal is a Fabric 1.20.1 mod that adds personal teleport portals and waypoints inspired by Magic Rampage.

Current version: 1.4.0

## Features

- Survival portal staff with a crafting recipe
- Infinite creative-only portal staff
- Per-player waypoint storage by UUID
- Dedicated MR Portal creative tab
- GUI that does not pause the game in singleplayer
- Separate survival and creative behavior
- Spark appears immediately when the waypoint screen opens
- New spark particle behavior instead of the previous look
- Spark now uses white particles only
- Black portal that grows from the spark and collapses back into a point
- Source portal appears in front of the player instead of under the player
- After teleportation the portal stays for a short delay and then collapses smoothly
- Configurable blindness effect that can be disabled
- Geometry and timing settings in config/mr_portal.json
- Clearer and more informative GUI with resource status
- Custom waypoint names when creating a point
- Rename existing waypoints directly from the GUI
- Waypoints are grouped by dimension with section headers
- Waypoint name is prompted via dialog only when adding
- Portal always collapses smoothly even if nobody entered
- Staff items always render with enchant glint and white particles while carried
- The normal staff tooltip now shows cooldown state correctly

## Items

### Portal Staff

- ID: mr_portal:portal_staff
- Survival use
- Has cooldown
- Consumes a fixed ender pearl cost per teleport

### Infinite Portal Staff

- ID: mr_portal:infinite_portal_staff
- No cooldown
- No ender pearl cost
- Intended for creative and admin use

## Recipe

Portal Staff crafting layout:

- corners: obsidian
- sides: ender eyes
- center: nether star

## Usage

1. Hold a portal staff.
2. Right-click to open the waypoint screen and preview the spark.
3. Add the current location or pick an existing waypoint.
4. After selecting a waypoint, the source portal opens in front of the player.
5. A spark and portal appear at the destination at the same time.
6. Walk into the portal to teleport.

## Config

The file is created automatically:

- config/mr_portal.json

Main options:

- cooldownSeconds: survival staff cooldown in seconds
- maxWaypoints: waypoint limit per player
- portalOpenDelay: portal opening time in ticks
- portalActiveTicks: how long the portal stays open after opening
- portalCloseDelayTicks: how long the portal remains after teleport before collapse starts
- portalPearlCost: teleport cost for the normal staff
- applyBlindnessOnTeleport: enables or disables blindness after teleport
- blindnessDuration: blindness duration in ticks
- portalScale: black portal size
- portalSpawnDistance: distance in blocks from the player to the source portal

Default values in 1.4.0:

- cooldownSeconds = 60
- maxWaypoints = 10
- portalOpenDelay = 60
- portalActiveTicks = 60
- portalCloseDelayTicks = 60
- portalPearlCost = 3
- applyBlindnessOnTeleport = true
- blindnessDuration = 40
- portalScale = 1.2
- portalSpawnDistance = 2.0

## Build

Requirements:

- Java 17+ for the project
- In this environment the build was verified with Java 21

Command:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH
./gradlew build
```

The final jar is generated in build/libs.

## Version files

- changelog_1.0.md
- changelog_1.1.md
- changelog_1.2.md
- changelog_1.3.md
- changelog_1.4.md