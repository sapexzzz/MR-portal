package com.mentality.mrportal.portal;

import com.mentality.mrportal.config.MRPortalConfig;
import com.mentality.mrportal.config.MRPortalConfigManager;
import com.mentality.mrportal.item.MRPortalItems;
import com.mentality.mrportal.network.MRPortalNetworking;
import com.mentality.mrportal.waypoint.WaypointData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class PendingTeleportManager {
	private static final Map<MinecraftServer, PendingTeleportManager> INSTANCES = new WeakHashMap<>();
	private static final double BASE_PORTAL_RADIUS = 1.34D;
	private static final double BASE_PORTAL_HALF_HEIGHT = 1.34D;
	private static final Vec3 VERTICAL_PORTAL_OFFSET = new Vec3(0.0D, 1.0D, 0.0D);

	private final Map<UUID, PortalSession> sessionsByPlayer = new HashMap<>();

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

	public boolean beginTeleport(ServerPlayer player, WaypointData destination, boolean infinite) {
		if (this.hasActiveSession(player)) {
			player.displayClientMessage(Component.translatable("message.mr_portal.portal_active"), true);
			return false;
		}

		MRPortalConfig config = MRPortalConfigManager.get();
		if (!infinite && !player.getAbilities().instabuild) {
			ItemStack staff = findPortalStaff(player);
			if (staff.isEmpty()) {
				player.displayClientMessage(Component.translatable("message.mr_portal.staff_required"), true);
				return false;
			}
			if (player.getCooldowns().isOnCooldown(staff.getItem())) {
				player.displayClientMessage(Component.translatable("message.mr_portal.cooldown_active"), true);
				return false;
			}

			Vec3 sourceCenter = calculateSourcePortalCenter(player, config);
			Vec3 destinationCenter = calculateDestinationPortalCenter(destination);
			int pearlCost = calculatePearlCost(sourceCenter, destinationCenter);
			if (!consumeEnderPearls(player, pearlCost)) {
				player.displayClientMessage(Component.translatable("message.mr_portal.not_enough_pearls", pearlCost), true);
				return false;
			}

			player.getCooldowns().addCooldown(staff.getItem(), config.cooldownSeconds * 20);
		}

		Vec3 sourcePos = calculateSourcePortalCenter(player, config);
		Vec3 destinationPos = calculateDestinationPortalCenter(destination);
		float portalYaw = player.getYRot();
		PortalSession session = new PortalSession(
			UUID.randomUUID(),
			player.getUUID(),
			player.serverLevel().dimension(),
			sourcePos,
			portalYaw,
			destination.dimension(),
			destinationPos,
			portalYaw,
			player.serverLevel().getGameTime(),
			infinite
		);
		this.sessionsByPlayer.put(player.getUUID(), session);
		MRPortalNetworking.broadcastPortalCreate(player.server, session);
		return true;
	}

	private void tickInternal(MinecraftServer server) {
		MRPortalConfig config = MRPortalConfigManager.get();
		List<UUID> expired = new ArrayList<>();

		for (PortalSession session : this.sessionsByPlayer.values()) {
			ServerPlayer player = server.getPlayerList().getPlayer(session.playerId());
			if (player == null || !player.isAlive()) {
				expired.add(session.playerId());
				MRPortalNetworking.broadcastPortalClose(server, session.sessionId());
				continue;
			}

			long age = player.serverLevel().getGameTime() - session.createdTick();
			if (age > config.portalLifetimeTicks()) {
				expired.add(session.playerId());
				MRPortalNetworking.broadcastPortalClose(server, session.sessionId());
				continue;
			}

			if (age < config.portalOpenDelay) {
				continue;
			}

			if (!player.serverLevel().dimension().equals(session.sourceDimension())) {
				continue;
			}

			if (isInsidePortal(player.position(), session.sourcePos(), config.portalScale)) {
				ServerLevel destinationLevel = server.getLevel(session.destinationDimension());
				if (destinationLevel == null) {
					expired.add(session.playerId());
					MRPortalNetworking.broadcastPortalClose(server, session.sessionId());
					continue;
				}

				Vec3 target = session.destinationPos().subtract(VERTICAL_PORTAL_OFFSET);
				player.serverLevel().playSound(null, session.sourcePos().x, session.sourcePos().y, session.sourcePos().z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
				player.teleportTo(destinationLevel, target.x, target.y, target.z, player.getYRot(), player.getXRot());
				destinationLevel.playSound(null, target.x, target.y, target.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
				player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, config.blindnessDuration, 0, false, false, true));
				MRPortalNetworking.broadcastPortalClose(server, session.sessionId());
				expired.add(session.playerId());
			}
		}

		for (UUID playerId : expired) {
			this.sessionsByPlayer.remove(playerId);
		}
	}

	private static boolean isInsidePortal(Vec3 playerPos, Vec3 portalPos, double scale) {
		double dx = playerPos.x - portalPos.x;
		double dz = playerPos.z - portalPos.z;
		double horizontalDistanceSq = dx * dx + dz * dz;
		double radius = BASE_PORTAL_RADIUS * scale;
		double halfHeight = BASE_PORTAL_HALF_HEIGHT * scale;
		return horizontalDistanceSq <= radius * radius && Math.abs((playerPos.y + 1.0D) - portalPos.y) <= halfHeight;
	}

	private static int calculatePearlCost(Vec3 from, Vec3 to) {
		double distance = from.distanceTo(to);
		int cost = (int) Math.ceil(distance / 500.0D);
		return Math.max(1, Math.min(10, cost));
	}

	private static Vec3 calculateSourcePortalCenter(ServerPlayer player, MRPortalConfig config) {
		Vec3 look = player.getLookAngle();
		Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
		if (horizontal.lengthSqr() < 1.0E-4D) {
			horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
			horizontal = new Vec3(horizontal.x, 0.0D, horizontal.z);
		}
		Vec3 offset = horizontal.normalize().scale(config.portalSpawnDistance);
		return player.position().add(offset).add(VERTICAL_PORTAL_OFFSET);
	}

	private static Vec3 calculateDestinationPortalCenter(WaypointData destination) {
		return new Vec3(destination.x(), destination.y(), destination.z()).add(VERTICAL_PORTAL_OFFSET);
	}

	private static boolean consumeEnderPearls(Player player, int count) {
		int available = 0;
		for (ItemStack stack : player.getInventory().items) {
			if (stack.is(Items.ENDER_PEARL)) {
				available += stack.getCount();
			}
		}
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
		return ItemStack.EMPTY;
	}
}