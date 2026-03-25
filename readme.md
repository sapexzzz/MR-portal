# Mentalitys | MR Portal

Mentalitys | MR Portal is a Fabric 1.20.1 mod that adds personal teleport portals and waypoints inspired by Magic Rampage.

Current version: 2.2.0

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
- Destination portal now appears even when teleporting beyond render distance
- Cloth Config API support for editing settings in-game via ModMenu
- Mod language setting (auto / en / ru) in the config screen
- Mobs and other players can teleport through the portal
- Preview spark (while choosing a waypoint) is visible to nearby players
- Teleport Scroll — single-use consumable teleportation item (looks like enchanted paper)
- Quick-open GUI keybind (V by default) — works if a staff or scroll is in the inventory
- Cross-dimension teleport blocked for normal staff and scroll (infinite staff is unrestricted)
- Portal spawns 2 blocks in front of the player and sits at ground level
- Recipes automatically unlock when picking up ingredients
- Configurable portal vertical offset (portalVerticalOffset)
- Configurable portal open/close animation speed (portalAnimationSpeed)
- Fixed language setting — now only affects mod text, does not change Minecraft UI
- Fixed oversized portal hitbox: teleport only triggers when you actually enter the portal plane
- One waypoint can now be marked as favorite
- Added a second configurable keybind for instant portal activation to the favorite waypoint (default: the key used for ё / grave accent)
- The favorite star now sits directly in the waypoint row to the left of the name
- The quick favorite key now shows sparks for 1.5 seconds before the portal starts opening
- The destination portal appears slightly behind the player after teleport and no longer plays an opening sound
- Normal staff and scroll are limited to waypoints from the current dimension in both the list and teleport logic
- Clicking the favorite star now also selects that waypoint as the active target
- Removed the redundant right-panel text that said whether the waypoint is favorite
- Other entities can keep using the portal until it actually closes

## Items

### Portal Staff

- ID: mr_portal:portal_staff
- Survival use
- Has cooldown
- Consumes a fixed ender pearl cost per teleport

### Teleport Scroll

- ID: mr_portal:teleport_scroll
- Single-use teleportation scroll
- Stacks up to 16
- Looks like enchanted paper
- Does not require ender pearls
- Consumed on teleportation
- Cannot teleport between dimensions

### Infinite Portal Staff

- ID: mr_portal:infinite_portal_staff
- No cooldown
- No ender pearl cost
- Intended for creative and admin use

## Recipes

### Portal Staff

Portal Staff crafting layout:

- corners: obsidian
- sides: ender eyes
- center: nether star

### Teleport Scroll

- sides: ender pearls (4)
- center: paper
- output: 2 teleport scrolls

## Usage

1. Hold a portal staff or teleport scroll.
2. Right-click to open the waypoint screen and preview the spark.
3. Add the current location or pick an existing waypoint.
4. After selecting a waypoint, the source portal opens in front of the player.
5. A spark and portal appear at the destination at the same time.
6. Walk into the portal to teleport.

You can also press V (configurable in controls) to open the menu without right-clicking, as long as a staff or scroll is in the inventory.

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
- portalVerticalOffset: vertical portal offset (-2.0 to 5.0)
- portalAnimationSpeed: portal animation speed multiplier (0.1 to 5.0)
- quickFavoritePortalDelayTicks: delay before the quick favorite portal starts
- portalExitBehindDistance: how far behind the player the destination portal appears after teleport

Settings can be edited in-game via ModMenu → Mentalitys | MR Portal (requires Cloth Config API).

Default values in 2.2.0:

- cooldownSeconds = 60
- maxWaypoints = 10
- portalOpenDelay = 60
- portalActiveTicks = 100
- portalCloseDelayTicks = 60
- portalPearlCost = 3
- applyBlindnessOnTeleport = true
- blindnessDuration = 40
- portalScale = 1.5
- language = auto
- portalSpawnDistance = 2.2
- portalVerticalOffset = 0.0
- portalAnimationSpeed = 1.0
- quickFavoritePortalDelayTicks = 30
- portalExitBehindDistance = 0.2

## Favorite Waypoint And Quick Portal

- You can mark one waypoint as favorite in the waypoint screen
- The favorite waypoint is marked with a star and stored on the server
- The new quick keybind defaults to the grave accent key, which is the same physical key as ё on RU layouts
- Quick activation uses this priority: infinite portal staff, normal portal staff, teleport scroll
- Cross-dimension quick activation requires an infinite portal staff or creative privileges
- By default, the quick favorite portal waits 1.5 seconds with sparks before starting to open
- The favorite toggle now lives directly in the waypoint row: empty star for normal, filled yellow star for favorite
- Clicking the star now behaves like selecting the waypoint too, so it immediately becomes the current target
- While the portal is still open, other entities and players can continue entering it until the close moment

## Dependencies

- Fabric API 0.92.7+1.20.1
- Cloth Config API 11.1.136 (recommended)
- ModMenu 7.2.2 (recommended)

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

## Source code

https://github.com/sapexzzz/MR-portal

## Version files

- changelog_1.0.md
- changelog_1.1.md
- changelog_1.2.md
- changelog_1.3.md
- changelog_1.4.md
- changelog_1.5.md
- changelog_1.6.md
- changelog_1.7.md
- changelog_1.7hotfix.md
- changelog_1.8.md
- changelog_1.9.md
- changelog_2.0.md
- changelog_2.1.md
- changelog_2.2.md
