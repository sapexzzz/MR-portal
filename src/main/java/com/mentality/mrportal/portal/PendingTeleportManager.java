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
	private static final double PORTAL_RADIUS = 1.2D;
	private static final double PORTAL_HEIGHT = 2.4D;

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

			int pearlCost = calculatePearlCost(player.position(), new Vec3(destination.x(), destination.y(), destination.z()));
			if (!consumeEnderPearls(player, pearlCost)) {
				player.displayClientMessage(Component.translatable("message.mr_portal.not_enough_pearls", pearlCost), true);
				return false;
			}

			player.getCooldowns().addCooldown(staff.getItem(), config.cooldownSeconds * 20);
		}

		Vec3 sourcePos = player.position();
		PortalSession session = new PortalSession(
			UUID.randomUUID(),
			player.getUUID(),
			player.serverLevel().dimension(),
			sourcePos,
			destination.dimension(),
			new Vec3(destination.x(), destination.y(), destination.z()),
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

			if (isInsidePortal(player.position(), session.sourcePos())) {
				ServerLevel destinationLevel = server.getLevel(session.destinationDimension());
				if (destinationLevel == null) {
					expired.add(session.playerId());
					MRPortalNetworking.broadcastPortalClose(server, session.sessionId());
					continue;
				}

				Vec3 target = session.destinationPos();
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

	private static boolean isInsidePortal(Vec3 playerPos, Vec3 portalPos) {
		double dx = playerPos.x - portalPos.x;
		double dz = playerPos.z - portalPos.z;
		double horizontalDistanceSq = dx * dx + dz * dz;
		return horizontalDistanceSq <= PORTAL_RADIUS * PORTAL_RADIUS && Math.abs(playerPos.y - portalPos.y) <= PORTAL_HEIGHT / 2.0D;
	}

	private static int calculatePearlCost(Vec3 from, Vec3 to) {
		double distance = from.distanceTo(to);
		int cost = (int) Math.ceil(distance / 500.0D);
		return Math.max(1, Math.min(10, cost));
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