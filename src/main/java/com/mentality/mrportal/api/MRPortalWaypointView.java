package com.mentality.mrportal.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Read-only waypoint data exposed to MR-Portal addons.
 *
 * <p>This view intentionally does not expose NBT, storage mutation, or the
 * internal waypoint store.</p>
 */
public interface MRPortalWaypointView {
	UUID id();

	String name();

	ResourceKey<Level> dimension();

	double x();

	double y();

	double z();

	boolean favorite();
}
