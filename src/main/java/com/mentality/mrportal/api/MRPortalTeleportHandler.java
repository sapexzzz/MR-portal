package com.mentality.mrportal.api;

import net.minecraft.resources.ResourceLocation;

/**
 * Public teleport execution hook for MR-Portal addons.
 *
 * <p>Higher priority handlers win. The built-in default handler will use a low
 * priority in a later phase. Handlers must not consume pearls, scrolls, or
 * cooldowns in {@link #canStartTeleport(MRPortalTeleportContext)}.</p>
 */
public interface MRPortalTeleportHandler {
	/**
	 * Stable unique id for this handler.
	 */
	ResourceLocation id();

	/**
	 * Handler priority. Higher values are selected first.
	 */
	int priority();

	/**
	 * Lightweight predicate for future routing phases. Must not consume resources.
	 */
	default boolean canStartTeleport(MRPortalTeleportContext context) {
		return true;
	}

	/**
	 * Executes teleport behavior for a future base-validated and paid request.
	 *
	 * @return true if the handler accepted and started its teleport behavior
	 */
	boolean startTeleport(MRPortalTeleportContext context);

	/**
	 * Whether base MR-Portal should use its default waypoint and quick-favorite
	 * preview spark for this handler.
	 *
	 * <p>Returning {@code false} means the handler owns its own preview visuals.
	 * This does not affect validation, payment, cooldowns, or teleport execution.</p>
	 */
	default boolean usesDefaultPreviewSpark() {
		return true;
	}
}
