package com.mentality.mrportal.portal;

import com.mentality.mrportal.api.MRPortalTeleportActivator;
import com.mentality.mrportal.waypoint.WaypointData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Internal activation classification for future request handling.
 */
record TeleportActivationInfo(
	ServerPlayer player,
	WaypointData waypoint,
	MRPortalTeleportActivator activator,
	boolean creativeView,
	boolean useScroll,
	boolean quickTeleport,
	boolean infinite,
	Vec3 sourcePos,
	float sourceYaw,
	long requestTick
) {
	TeleportActivationInfo {
		Objects.requireNonNull(player, "player");
		Objects.requireNonNull(waypoint, "waypoint");
		Objects.requireNonNull(activator, "activator");
		Objects.requireNonNull(sourcePos, "sourcePos");
	}
}
