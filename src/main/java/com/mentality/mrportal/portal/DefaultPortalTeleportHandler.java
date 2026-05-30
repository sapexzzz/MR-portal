package com.mentality.mrportal.portal;

import com.mentality.mrportal.MRPortal;
import com.mentality.mrportal.api.MRPortalTeleportContext;
import com.mentality.mrportal.api.MRPortalTeleportHandler;
import net.minecraft.resources.ResourceLocation;

/**
 * Built-in fallback handler for MR-Portal's default portal teleport behavior.
 *
 * <p>This phase only registers the handler. Gameplay routing is deferred to the
 * request-service phase so existing validation, payment, cooldown, and portal
 * session behavior stays unchanged.</p>
 */
public final class DefaultPortalTeleportHandler implements MRPortalTeleportHandler {
	public static final ResourceLocation ID = new ResourceLocation(MRPortal.MOD_ID, "default_portal");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public int priority() {
		return 0;
	}

	@Override
	public boolean canStartTeleport(MRPortalTeleportContext context) {
		return true;
	}

	/**
	 * Starts the already validated and paid default portal session.
	 */
	@Override
	public boolean startTeleport(MRPortalTeleportContext context) {
		if (!(context.waypoint() instanceof WaypointDataView waypointView)) {
			MRPortal.LOGGER.warn("Default MR-Portal teleport handler requires an internal waypoint view");
			return false;
		}
		boolean infinite = context.activator() == com.mentality.mrportal.api.MRPortalTeleportActivator.INFINITE_STAFF
			|| context.activator() == com.mentality.mrportal.api.MRPortalTeleportActivator.CREATIVE;
		return PendingTeleportManager.get(context.server()).startPaidTeleportSession(context.player(), waypointView.waypointData(), infinite, context.sourcePos(), context.sourceYaw());
	}

	@Override
	public boolean usesDefaultPreviewSpark() {
		return true;
	}
}
