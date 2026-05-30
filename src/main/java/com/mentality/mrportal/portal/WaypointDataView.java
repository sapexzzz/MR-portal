package com.mentality.mrportal.portal;

import com.mentality.mrportal.api.MRPortalWaypointView;
import com.mentality.mrportal.waypoint.WaypointData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;

/**
 * Package-private read-only adapter from internal waypoint data to public API view.
 */
final class WaypointDataView implements MRPortalWaypointView {
	private final WaypointData waypoint;

	WaypointDataView(WaypointData waypoint) {
		this.waypoint = Objects.requireNonNull(waypoint, "waypoint");
	}

	WaypointData waypointData() {
		return this.waypoint;
	}

	@Override
	public UUID id() {
		return this.waypoint.id();
	}

	@Override
	public String name() {
		return this.waypoint.name();
	}

	@Override
	public ResourceKey<Level> dimension() {
		return this.waypoint.dimension();
	}

	@Override
	public double x() {
		return this.waypoint.x();
	}

	@Override
	public double y() {
		return this.waypoint.y();
	}

	@Override
	public double z() {
		return this.waypoint.z();
	}

	@Override
	public boolean favorite() {
		return this.waypoint.favorite();
	}
}
