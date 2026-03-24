package com.mentality.mrportal.portal;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record PortalSession(
	UUID sessionId,
	UUID playerId,
	ResourceKey<Level> sourceDimension,
	Vec3 sourcePos,
	float sourceYaw,
	ResourceKey<Level> destinationDimension,
	Vec3 destinationPos,
	Vec3 destinationExitPos,
	float destinationYaw,
	long createdTick,
	boolean infinite,
	boolean teleported,
	long closeTick
) {
	public PortalSession markTeleported(long closeTick) {
		return new PortalSession(
			this.sessionId,
			this.playerId,
			this.sourceDimension,
			this.sourcePos,
			this.sourceYaw,
			this.destinationDimension,
			this.destinationPos,
			this.destinationExitPos,
			this.destinationYaw,
			this.createdTick,
			this.infinite,
			true,
			closeTick
		);
	}
}