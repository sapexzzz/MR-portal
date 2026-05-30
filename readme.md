# Mentalitys | MR Portal

Mentalitys | MR Portal is a Fabric 1.20.1 mod that adds personal portals and per-player waypoints inspired by Magic Rampage.

Current version: 2.3

## What It Does

MR Portal lets each player save waypoint locations and open temporary portals to them. A source portal opens in front of the player, and a destination portal appears after teleporting. Other players and entities can pass through active portals while they remain open.

The base mod supports normal survival use, one-use scroll teleporting, creative/admin teleporting, favorite waypoints, and a quick favorite keybind.

## Features

- Fabric 1.20.1 mod.
- Per-player waypoint storage.
- Waypoint GUI for adding, selecting, renaming, deleting, and favoriting points.
- Portal Staff for survival teleporting.
- Infinite Portal Staff for creative/admin use.
- Teleport Scroll as a one-use teleport item.
- Favorite waypoint support.
- Quick favorite teleport keybind.
- Preview spark / target point while selecting a destination.
- Portals open in front of the player.
- Destination portal appears after teleport.
- Other entities and players can pass through active portals.
- Regular staff and scroll are same-dimension only.
- Infinite staff and creative mode can bypass normal dimension restrictions.
- Configurable portal timing, cost, scale, language, and visual behavior.

## New In 2.3

Version 2.3 updates the internal teleport routing without changing the default MR Portal gameplay:

- GUI waypoint teleport now routes through a request service.
- Quick favorite teleport now routes through the same service layer.
- Addons can register custom teleport handlers through the public API.
- The active handler is selected by handler priority.
- Base MR Portal keeps validation, payment, cooldown, item consumption, and same-dimension checks.
- Handlers own the execution style and post-validation teleport behavior.
- Handlers can control whether base MR Portal emits its default preview spark with `usesDefaultPreviewSpark()`.
- The built-in default handler preserves the normal MR Portal portal behavior.

This prepares MR Portal for external addons such as Chrono Portal support without hardcoding addon logic into the base mod. Chrono Portal is not bundled into base MR Portal.

## Items

### Portal Staff

- ID: `mr_portal:portal_staff`
- Survival teleport item.
- Costs ender pearls when the portal starts.
- Applies a staff cooldown.
- Same-dimension only.

### Teleport Scroll

- ID: `mr_portal:teleport_scroll`
- One-use teleport item.
- Consumed when the portal starts.
- Does not cost ender pearls.
- Does not apply staff cooldown.
- Same-dimension only.

### Infinite Portal Staff

- ID: `mr_portal:infinite_portal_staff`
- Creative/admin teleport item.
- No pearl cost.
- No cooldown.
- Can bypass normal dimension restrictions.

## Usage

1. Hold a Portal Staff or Teleport Scroll.
2. Right-click to open the waypoint GUI.
3. Add your current location as a waypoint or select an existing waypoint.
4. Confirm teleport to create a portal in front of you.
5. Walk into the portal to teleport.

You can also use the configured keybind to open the waypoint GUI quickly if you are carrying a staff or scroll.

Payment is applied when the portal starts, not merely when opening the GUI.

## Favorite Waypoint And Quick Portal

- Mark one waypoint as favorite in the waypoint GUI.
- Use the quick favorite keybind to queue a portal to that favorite waypoint.
- The quick favorite path shows a preview delay before activation.
- Payment happens at delayed activation, not at keypress.
- Preferred quick activator order is: Infinite Portal Staff, Portal Staff, Teleport Scroll.
- Regular staff and scroll quick teleports are same-dimension only.
- Infinite staff and creative mode can bypass normal restrictions.

## Addon/API Support

MR Portal 2.3 includes an addon-ready teleport handler API.

Addon handlers can:

- register an `MRPortalTeleportHandler`;
- use priority to become the active handler;
- accept or reject already validated requests;
- execute a different teleport style after base validation;
- opt out of the base preview spark with `usesDefaultPreviewSpark()`.

Base MR Portal still owns:

- waypoint ownership and lookup;
- item detection;
- same-dimension validation;
- pearl cost checks and consumption;
- scroll consumption;
- staff cooldown checks and application;
- GUI and quick favorite request timing.

This keeps addon behavior compatible with the base economy and rules while allowing external visual or teleport styles.

## Config

The config file is created automatically:

- `config/mr_portal.json`

Options:

- `language`: mod language, `auto`, `en`, or `ru`.
- `cooldownSeconds`: survival staff cooldown in seconds.
- `maxWaypoints`: waypoint limit per player.
- `portalOpenDelay`: portal opening time in ticks.
- `portalActiveTicks`: how long the portal stays open after opening.
- `portalCloseDelayTicks`: how long the portal remains after teleport before collapse starts.
- `portalPearlCost`: teleport cost for the normal staff.
- `applyBlindnessOnTeleport`: enables or disables blindness after teleport.
- `blindnessDuration`: blindness duration in ticks.
- `portalScale`: portal visual size.
- `portalSpawnDistance`: distance from the player to the source portal.
- `portalVerticalOffset`: vertical portal offset.
- `portalAnimationSpeed`: portal animation speed multiplier.
- `quickFavoritePortalDelayTicks`: delay before the quick favorite portal starts.
- `portalExitBehindDistance`: how far behind the player the destination portal appears after teleport.

Default values:

- `language = auto`
- `cooldownSeconds = 60`
- `maxWaypoints = 10`
- `portalOpenDelay = 60`
- `portalActiveTicks = 100`
- `portalCloseDelayTicks = 60`
- `portalPearlCost = 3`
- `applyBlindnessOnTeleport = true`
- `blindnessDuration = 40`
- `portalScale = 1.5`
- `portalSpawnDistance = 2.2`
- `portalVerticalOffset = 0.0`
- `portalAnimationSpeed = 1.0`
- `quickFavoritePortalDelayTicks = 30`
- `portalExitBehindDistance = 0.2`

Settings can be edited in game through ModMenu -> Mentalitys | MR Portal when Cloth Config API is installed.

## Dependencies

- Fabric API for Minecraft 1.20.1.
- Cloth Config API is recommended for the in-game config screen.
- ModMenu is recommended for opening the config screen from the Mods menu.

## Build

Project target: Java 17.

In this workspace, Gradle/Loom is run with Java 21:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build
```

The built jar is generated in `build/libs`.

## Source Code

https://github.com/sqwiziiy/MR-Portal

## Notes

Historical readmes and changelogs are archived in `old_data/`. They are kept as reference material and are not part of the clean 2.3 root README set.
