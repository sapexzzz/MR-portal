package com.mentality.mrportal.portal;

import com.mentality.mrportal.config.MRPortalConfig;
import com.mentality.mrportal.config.MRPortalConfigManager;
import com.mentality.mrportal.item.MRPortalItems;
import com.mentality.mrportal.network.MRPortalNetworking;
import com.mentality.mrportal.util.ModTranslation;
import com.mentality.mrportal.waypoint.WaypointData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class PendingTeleportManager {
	private static final Map<MinecraftServer, PendingTeleportManager> INSTANCES = new WeakHashMap<>();
	private static final double BASE_PORTAL_RADIUS = 1.18D;
	private static final double BASE_PORTAL_HALF_HEIGHT = 1.34D;
	private static final double BASE_PORTAL_HALF_THICKNESS = 0.18D;

	private static Vec3 verticalOffset(MRPortalConfig config) {
		return new Vec3(0.0D, BASE_PORTAL_HALF_HEIGHT + config.portalVerticalOffset, 0.0D);
	}

	private static Vec3 horizontalForward(float yaw) {
		Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
		Vec3 horizontal = new Vec3(forward.x, 0.0D, forward.z);
		if (horizontal.lengthSqr() < 1.0E-4D) {
			return new Vec3(0.0D, 0.0D, 1.0D);
		}
		return horizontal.normalize();
	}

	private final Map<UUID, PortalSession> sessionsByPlayer = new HashMap<>();
	private final Map<UUID, Set<UUID>> teleportedEntities = new HashMap<>();
	private final Map<UUID, PendingFavoriteActivation> pendingFavoriteActivations = new HashMap<>();

	private PendingTeleportManager() {
	}

	public static PendingTeleportManager get(MinecraftServer server) {
		return INSTANCES.computeIfAbsent(server, current -> new PendingTeleportManager());
	}

	public static void tick(MinecraftServer server) {
		get(server).tickInternal(server);
	}

	public boolean hasActiveSession(ServerPlayer player) {
		return this.sessionsByPlayer.containsKey(player.getUUID());
	}

	public boolean hasPendingFavoriteActivation(ServerPlayer player) {
		return this.pendingFavoriteActivations.containsKey(player.getUUID());
	}

	public boolean beginTeleport(ServerPlayer player, WaypointData destination, boolean infinite) {
		if (this.hasActiveSession(player)) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.portal_active"), true);
			return false;
		}

		MRPortalConfig config = MRPortalConfigManager.get();
		int pearlCost = getRequiredPearls(infinite || player.getAbilities().instabuild);
		if (!infinite && !player.getAbilities().instabuild) {
			ItemStack staff = findPortalStaff(player);
			if (staff.isEmpty()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.staff_required"), true);
				return false;
			}
			if (player.getCooldowns().isOnCooldown(staff.getItem())) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.cooldown_active"), true);
				return false;
			}

			if (!consumeEnderPearls(player, pearlCost)) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.not_enough_pearls", pearlCost), true);
				return false;
			}

			player.getCooldowns().addCooldown(staff.getItem(), config.cooldownSeconds * 20);
		}

		return startSession(player, destination, infinite, calculateSourcePortalCenter(player, config), player.getYRot(), player.server.getTickCount());
	}

	public boolean beginScrollTeleport(ServerPlayer player, WaypointData destination) {
		if (this.hasActiveSession(player)) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.portal_active"), true);
			return false;
		}

		ItemStack scroll = findTeleportScroll(player);
		if (scroll.isEmpty()) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.staff_required"), true);
			return false;
		}

		scroll.shrink(1);
		MRPortalConfig config = MRPortalConfigManager.get();
		return startSession(player, destination, false, calculateSourcePortalCenter(player, config), player.getYRot(), player.server.getTickCount());
	}

	public boolean queueFavoriteTeleport(ServerPlayer player, WaypointData destination, boolean infinite, boolean useScroll) {
		if (this.hasActiveSession(player) || this.hasPendingFavoriteActivation(player)) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.portal_active"), true);
			return false;
		}

		MRPortalConfig config = MRPortalConfigManager.get();
		Vec3 sourcePos = calculateSourcePortalCenter(player, config);
		PendingFavoriteActivation activation = new PendingFavoriteActivation(
			player.getUUID(),
			destination,
			infinite,
			useScroll,
			sourcePos,
			player.getYRot(),
			player.serverLevel().dimension(),
			player.server.getTickCount() + config.quickFavoritePortalDelayTicks
		);
		this.pendingFavoriteActivations.put(player.getUUID(), activation);
		MRPortalNetworking.broadcastQuickPreviewSpark(player.server, player, sourcePos);
		return true;
	}

	private boolean startSession(ServerPlayer player, WaypointData destination, boolean infinite, Vec3 sourcePos, float portalYaw, long createdTick) {
		MRPortalConfig config = MRPortalConfigManager.get();
		Vec3 destinationPos = calculateDestinationPortalCenter(destination, portalYaw, config);
		Vec3 destinationExitPos = calculateDestinationExitTarget(destination);
		PortalSession session = new PortalSession(
			UUID.randomUUID(),
			player.getUUID(),
			player.serverLevel().dimension(),
			sourcePos,
			portalYaw,
			destination.dimension(),
			destinationPos,
			destinationExitPos,
			portalYaw,
			createdTick,
			infinite,
			false,
			0L
		);
		this.sessionsByPlayer.put(player.getUUID(), session);
		MRPortalNetworking.broadcastPortalCreate(player.server, session);
		return true;
	}

	private void tickInternal(MinecraftServer server) {
		MRPortalConfig config = MRPortalConfigManager.get();
		List<UUID> expired = new ArrayList<>();
		List<UUID> completedPending = new ArrayList<>();

		for (PendingFavoriteActivation activation : new ArrayList<>(this.pendingFavoriteActivations.values())) {
			ServerPlayer player = server.getPlayerList().getPlayer(activation.playerId());
			if (player == null || !player.isAlive() || !player.serverLevel().dimension().equals(activation.sourceDimension())) {
				completedPending.add(activation.playerId());
				if (player != null) {
					MRPortalNetworking.removeQuickPreviewSpark(server, player);
				}
				continue;
			}

			if (server.getTickCount() < activation.activateTick()) {
				continue;
			}

			MRPortalNetworking.removeQuickPreviewSpark(server, player);
			activatePendingFavoriteTeleport(server, player, activation, config);
			completedPending.add(activation.playerId());
		}

		for (UUID playerId : completedPending) {
			this.pendingFavoriteActivations.remove(playerId);
		}

		for (PortalSession session : new ArrayList<>(this.sessionsByPlayer.values())) {
			ServerPlayer player = server.getPlayerList().getPlayer(session.playerId());
			if (player == null || !player.isAlive()) {
				expired.add(session.playerId());
				MRPortalNetworking.broadcastPortalClose(server, session.sessionId());
				continue;
			}

			long age = server.getTickCount() - session.createdTick();

			// If already closing (teleported or active time expired), wait for close delay
			if (session.teleported()) {
				// Still allow other entities to teleport during close delay
				teleportNearbyEntities(server, session, config);
				if (server.getTickCount() >= session.closeTick()) {
					expired.add(session.playerId());
					this.teleportedEntities.remove(session.sessionId());
					MRPortalNetworking.broadcastPortalClose(server, session.sessionId());
				}
				continue;
			}

			// If active time (open + active) has elapsed and nobody entered, start closing
			long activeEnd = config.portalOpenDelay + config.portalActiveTicks;
			if (age >= activeEnd) {
				this.sessionsByPlayer.put(session.playerId(), session.markTeleported(server.getTickCount() + config.portalCloseDelayTicks));
				continue;
			}

			if (age < config.portalOpenDelay) {
				continue;
			}

			// Check the portal owner first only if they are still in the source dimension.
			if (player.serverLevel().dimension().equals(session.sourceDimension()) && isInsidePortal(player, session.sourcePos(), session.sourceYaw(), config.portalScale)) {
				ServerLevel destinationLevel = server.getLevel(session.destinationDimension());
				if (destinationLevel == null) {
					expired.add(session.playerId());
					MRPortalNetworking.broadcastPortalClose(server, session.sessionId());
					continue;
				}

				Vec3 target = session.destinationExitPos();
				player.serverLevel().playSound(null, session.sourcePos().x, session.sourcePos().y, session.sourcePos().z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
				player.teleportTo(destinationLevel, target.x, target.y, target.z, player.getYRot(), player.getXRot());
				destinationLevel.playSound(null, target.x, target.y, target.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
				if (config.applyBlindnessOnTeleport && config.blindnessDuration > 0) {
					player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, config.blindnessDuration, 0, false, false, true));
				}
				MRPortalNetworking.sendPortalCreateToPlayer(player, session);
				this.teleportedEntities.computeIfAbsent(session.sessionId(), k -> new HashSet<>()).add(player.getUUID());
				this.sessionsByPlayer.put(session.playerId(), session.markTeleported(server.getTickCount() + config.portalCloseDelayTicks));
			}

			// Also check other entities (mobs, other players) inside the portal
			teleportNearbyEntities(server, session, config);
		}

		for (UUID playerId : expired) {
			this.sessionsByPlayer.remove(playerId);
		}
	}

	private void teleportNearbyEntities(MinecraftServer server, PortalSession session, MRPortalConfig config) {
		ServerLevel sourceLevel = server.getLevel(session.sourceDimension());
		if (sourceLevel == null) {
			return;
		}
		ServerLevel destinationLevel = server.getLevel(session.destinationDimension());
		if (destinationLevel == null) {
			return;
		}

		double radius = BASE_PORTAL_RADIUS * config.portalScale;
		double halfHeight = BASE_PORTAL_HALF_HEIGHT * config.portalScale;
		double halfThickness = BASE_PORTAL_HALF_THICKNESS * config.portalScale;
		Vec3 portalPos = session.sourcePos();
		AABB searchBox = new AABB(
			portalPos.x - radius, portalPos.y - halfHeight, portalPos.z - radius,
			portalPos.x + radius, portalPos.y + halfHeight, portalPos.z + radius
		).inflate(halfThickness + 0.4D);

		Set<UUID> alreadyTeleported = this.teleportedEntities.computeIfAbsent(session.sessionId(), k -> new HashSet<>());

		List<Entity> entities = sourceLevel.getEntitiesOfClass(Entity.class, searchBox, entity ->
			!entity.isSpectator()
			&& !alreadyTeleported.contains(entity.getUUID())
			&& !entity.getUUID().equals(session.playerId())
		);

		for (Entity entity : entities) {
			if (!isInsidePortal(entity, portalPos, session.sourceYaw(), config.portalScale)) {
				continue;
			}
			Vec3 target = session.destinationExitPos();
			if (entity instanceof ServerPlayer otherPlayer) {
				otherPlayer.teleportTo(destinationLevel, target.x, target.y, target.z, otherPlayer.getYRot(), otherPlayer.getXRot());
				MRPortalNetworking.sendPortalCreateToPlayer(otherPlayer, session);
			} else {
				if (sourceLevel.dimension().equals(session.destinationDimension())) {
					entity.teleportTo(target.x, target.y, target.z);
				} else {
					Entity transferred = entity.changeDimension(destinationLevel);
					if (transferred != null) {
						transferred.teleportTo(target.x, target.y, target.z);
					}
				}
			}
			destinationLevel.playSound(null, target.x, target.y, target.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
			alreadyTeleported.add(entity.getUUID());
		}
	}

	private static boolean isInsidePortal(Entity entity, Vec3 portalPos, float yaw, double scale) {
		AABB bounds = entity.getBoundingBox();
		Vec3 center = bounds.getCenter();
		double dx = center.x - portalPos.x;
		double dy = center.y - portalPos.y;
		double dz = center.z - portalPos.z;
		double yawRad = Math.toRadians(yaw);
		double normalX = -Math.sin(yawRad);
		double normalZ = Math.cos(yawRad);
		double tangentX = Math.cos(yawRad);
		double tangentZ = Math.sin(yawRad);
		double depth = dx * normalX + dz * normalZ;
		double lateral = dx * tangentX + dz * tangentZ;
		double radius = BASE_PORTAL_RADIUS * scale;
		double halfHeight = BASE_PORTAL_HALF_HEIGHT * scale;
		double halfThickness = BASE_PORTAL_HALF_THICKNESS * scale;
		double entityHalfWidth = Math.max(bounds.getXsize(), bounds.getZsize()) * 0.5D;
		double entityHalfHeight = bounds.getYsize() * 0.5D;
		return Math.abs(depth) <= halfThickness + entityHalfWidth
			&& Math.abs(lateral) <= radius + entityHalfWidth * 0.35D
			&& Math.abs(dy) <= halfHeight + entityHalfHeight * 0.35D;
	}

	public static int getRequiredPearls(boolean ignoreCost) {
		return ignoreCost ? 0 : MRPortalConfigManager.get().portalPearlCost;
	}

	public static int countEnderPearls(Player player) {
		int available = 0;
		for (ItemStack stack : player.getInventory().items) {
			if (stack.is(Items.ENDER_PEARL)) {
				available += stack.getCount();
			}
		}
		return available;
	}

	public static Vec3 calculateSourcePortalCenter(ServerPlayer player, MRPortalConfig config) {
		Vec3 look = player.getLookAngle();
		Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
		if (horizontal.lengthSqr() < 1.0E-4D) {
			horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
			horizontal = new Vec3(horizontal.x, 0.0D, horizontal.z);
		}
		Vec3 offset = horizontal.normalize().scale(config.portalSpawnDistance);
		return player.position().add(offset).add(verticalOffset(config));
	}

	private static Vec3 calculateDestinationPortalCenter(WaypointData destination, float yaw, MRPortalConfig config) {
		Vec3 backward = horizontalForward(yaw).scale(-config.portalExitBehindDistance);
		return new Vec3(destination.x(), destination.y(), destination.z()).add(backward).add(verticalOffset(config));
	}

	private static Vec3 calculateDestinationExitTarget(WaypointData destination) {
		return new Vec3(destination.x(), destination.y(), destination.z());
	}

	private static boolean consumeEnderPearls(Player player, int count) {
		int available = countEnderPearls(player);
		if (available < count) {
			return false;
		}

		int remaining = count;
		for (ItemStack stack : player.getInventory().items) {
			if (!stack.is(Items.ENDER_PEARL)) {
				continue;
			}
			int remove = Math.min(remaining, stack.getCount());
			stack.shrink(remove);
			remaining -= remove;
			if (remaining <= 0) {
				return true;
			}
		}
		return true;
	}

	private static ItemStack findPortalStaff(Player player) {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if (MRPortalItems.isPortalStaff(stack)) {
				return stack;
			}
		}
		for (ItemStack stack : player.getInventory().items) {
			if (MRPortalItems.isPortalStaff(stack)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack findPreferredTeleportItem(Player player) {
		ItemStack infinite = findSpecificItem(player, stack -> MRPortalItems.isInfinite(stack));
		if (!infinite.isEmpty()) {
			return infinite;
		}
		ItemStack normalStaff = findSpecificItem(player, stack -> stack.is(MRPortalItems.PORTAL_STAFF));
		if (!normalStaff.isEmpty()) {
			return normalStaff;
		}
		return findTeleportScroll(player);
	}

	private static ItemStack findSpecificItem(Player player, java.util.function.Predicate<ItemStack> predicate) {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if (predicate.test(stack)) {
				return stack;
			}
		}
		for (ItemStack stack : player.getInventory().items) {
			if (predicate.test(stack)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	private static ItemStack findTeleportScroll(Player player) {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if (MRPortalItems.isTeleportScroll(stack)) {
				return stack;
			}
		}
		for (ItemStack stack : player.getInventory().items) {
			if (MRPortalItems.isTeleportScroll(stack)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	private void activatePendingFavoriteTeleport(MinecraftServer server, ServerPlayer player, PendingFavoriteActivation activation, MRPortalConfig config) {
		if (this.hasActiveSession(player)) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.portal_active"), true);
			return;
		}
		if (!activation.infinite() && !player.getAbilities().instabuild && !activation.destination().dimension().equals(player.serverLevel().dimension())) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.same_dimension_only"), true);
			return;
		}

		if (activation.useScroll()) {
			ItemStack scroll = findTeleportScroll(player);
			if (scroll.isEmpty()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.favorite_item_required"), true);
				return;
			}
			scroll.shrink(1);
			startSession(player, activation.destination(), false, activation.sourcePos(), activation.portalYaw(), server.getTickCount());
			return;
		}

		int pearlCost = getRequiredPearls(activation.infinite() || player.getAbilities().instabuild);
		if (!activation.infinite() && !player.getAbilities().instabuild) {
			ItemStack staff = findPortalStaff(player);
			if (staff.isEmpty()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.favorite_item_required"), true);
				return;
			}
			if (player.getCooldowns().isOnCooldown(staff.getItem())) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.cooldown_active"), true);
				return;
			}
			if (!consumeEnderPearls(player, pearlCost)) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.not_enough_pearls", pearlCost), true);
				return;
			}
			player.getCooldowns().addCooldown(staff.getItem(), config.cooldownSeconds * 20);
		}

		startSession(player, activation.destination(), activation.infinite(), activation.sourcePos(), activation.portalYaw(), server.getTickCount());
	}

	private record PendingFavoriteActivation(
		UUID playerId,
		WaypointData destination,
		boolean infinite,
		boolean useScroll,
		Vec3 sourcePos,
		float portalYaw,
		net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> sourceDimension,
		long activateTick
	) {}
}