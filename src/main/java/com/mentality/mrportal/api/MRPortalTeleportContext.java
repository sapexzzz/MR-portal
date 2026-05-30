package com.mentality.mrportal.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Base-validated teleport request data passed to MR-Portal teleport handlers.
 *
 * <p>Future routing phases will keep payment, cooldown, item detection, and
 * same-dimension ownership in base MR-Portal before handlers execute.</p>
 */
public final class MRPortalTeleportContext {
	private final MinecraftServer server;
	private final ServerPlayer player;
	private final MRPortalWaypointView waypoint;
	private final boolean creativeView;
	private final boolean useScroll;
	private final boolean quickTeleport;
	private final MRPortalTeleportActivator activator;
	private final int pearlCost;
	private final int cooldownTicks;
	private final Vec3 sourcePos;
	private final float sourceYaw;

	public MRPortalTeleportContext(
		MinecraftServer server,
		ServerPlayer player,
		MRPortalWaypointView waypoint,
		boolean creativeView,
		boolean useScroll,
		boolean quickTeleport,
		MRPortalTeleportActivator activator,
		int pearlCost,
		int cooldownTicks
	) {
		this(server, player, waypoint, creativeView, useScroll, quickTeleport, activator, pearlCost, cooldownTicks, player.position(), player.getYRot());
	}

	public MRPortalTeleportContext(
		MinecraftServer server,
		ServerPlayer player,
		MRPortalWaypointView waypoint,
		boolean creativeView,
		boolean useScroll,
		boolean quickTeleport,
		MRPortalTeleportActivator activator,
		int pearlCost,
		int cooldownTicks,
		Vec3 sourcePos,
		float sourceYaw
	) {
		this.server = Objects.requireNonNull(server, "server");
		this.player = Objects.requireNonNull(player, "player");
		this.waypoint = Objects.requireNonNull(waypoint, "waypoint");
		this.creativeView = creativeView;
		this.useScroll = useScroll;
		this.quickTeleport = quickTeleport;
		this.activator = Objects.requireNonNull(activator, "activator");
		if (pearlCost < 0) {
			throw new IllegalArgumentException("pearlCost must be non-negative");
		}
		if (cooldownTicks < 0) {
			throw new IllegalArgumentException("cooldownTicks must be non-negative");
		}
		this.pearlCost = pearlCost;
		this.cooldownTicks = cooldownTicks;
		this.sourcePos = Objects.requireNonNull(sourcePos, "sourcePos");
		this.sourceYaw = sourceYaw;
	}

	public MinecraftServer server() {
		return this.server;
	}

	public ServerPlayer player() {
		return this.player;
	}

	public MRPortalWaypointView waypoint() {
		return this.waypoint;
	}

	public boolean creativeView() {
		return this.creativeView;
	}

	public boolean useScroll() {
		return this.useScroll;
	}

	public boolean quickTeleport() {
		return this.quickTeleport;
	}

	public MRPortalTeleportActivator activator() {
		return this.activator;
	}

	public int pearlCost() {
		return this.pearlCost;
	}

	public int cooldownTicks() {
		return this.cooldownTicks;
	}

	public Vec3 sourcePos() {
		return this.sourcePos;
	}

	public float sourceYaw() {
		return this.sourceYaw;
	}
}
