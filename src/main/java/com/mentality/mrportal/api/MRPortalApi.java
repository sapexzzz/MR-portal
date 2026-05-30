package com.mentality.mrportal.api;

import com.mentality.mrportal.MRPortal;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Public entry point for MR-Portal addon integrations.
 *
 * <p>This API stores teleport handlers only. Gameplay is not routed through it
 * until later phases. Base MR-Portal keeps payment, cooldown, item detection,
 * and waypoint ownership.</p>
 */
public final class MRPortalApi {
	public static final int API_VERSION = 1;

	private static final Map<ResourceLocation, MRPortalTeleportHandler> TELEPORT_HANDLERS = new LinkedHashMap<>();

	private MRPortalApi() {
	}

	/**
	 * Registers or replaces a teleport handler.
	 *
	 * <p>If the same id is registered again, the latest handler replaces the
	 * previous one and a warning is logged.</p>
	 */
	public static synchronized void registerTeleportHandler(MRPortalTeleportHandler handler) {
		Objects.requireNonNull(handler, "handler");
		ResourceLocation id = Objects.requireNonNull(handler.id(), "handler id");
		MRPortalTeleportHandler previous = TELEPORT_HANDLERS.put(id, handler);
		if (previous != null) {
			MRPortal.LOGGER.warn("Replacing MR-Portal teleport handler {}", id);
		} else {
			MRPortal.LOGGER.info("Registered MR-Portal teleport handler {} with priority {}", id, handler.priority());
		}
	}

	/**
	 * Returns the active handler by priority.
	 *
	 * <p>Higher priority wins. Same-priority ties are deterministic by handler id
	 * string, and a warning is logged. Returns {@code null} when no handler is
	 * registered; a built-in default handler will be registered in a later phase.</p>
	 */
	public static synchronized MRPortalTeleportHandler getActiveTeleportHandler() {
		if (TELEPORT_HANDLERS.isEmpty()) {
			return null;
		}

		List<MRPortalTeleportHandler> handlers = new ArrayList<>(TELEPORT_HANDLERS.values());
		handlers.sort(
			Comparator.comparingInt(MRPortalTeleportHandler::priority).reversed()
				.thenComparing(handler -> handler.id().toString())
		);

		MRPortalTeleportHandler selected = handlers.get(0);
		if (handlers.size() > 1 && handlers.get(1).priority() == selected.priority()) {
			MRPortal.LOGGER.warn(
				"Multiple MR-Portal teleport handlers share priority {}; selecting {} deterministically",
				selected.priority(),
				selected.id()
			);
		}
		return selected;
	}

	/**
	 * Returns an immutable snapshot of registered teleport handlers.
	 */
	public static synchronized Collection<MRPortalTeleportHandler> getRegisteredTeleportHandlers() {
		return List.copyOf(TELEPORT_HANDLERS.values());
	}
}
