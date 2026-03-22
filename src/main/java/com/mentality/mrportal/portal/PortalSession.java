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
	ResourceKey<Level> destinationDimension,
	Vec3 destinationPos,
	long createdTick,
	boolean infinite
) {
}