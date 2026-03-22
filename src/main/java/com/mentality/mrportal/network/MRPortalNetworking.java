package com.mentality.mrportal.network;

import com.mentality.mrportal.MRPortal;
import com.mentality.mrportal.config.MRPortalConfigManager;
import com.mentality.mrportal.item.MRPortalItems;
import com.mentality.mrportal.portal.PendingTeleportManager;
import com.mentality.mrportal.portal.PortalSession;
import com.mentality.mrportal.waypoint.ServerWaypointStore;
import com.mentality.mrportal.waypoint.WaypointData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MRPortalNetworking {
	public static final ResourceLocation OPEN_SCREEN_S2C = id("open_screen");
	public static final ResourceLocation PORTAL_CREATE_S2C = id("portal_create");
	public static final ResourceLocation PORTAL_CLOSE_S2C = id("portal_close");
	public static final ResourceLocation ADD_WAYPOINT_C2S = id("add_waypoint");
	public static final ResourceLocation DELETE_WAYPOINT_C2S = id("delete_waypoint");
	public static final ResourceLocation TELEPORT_REQUEST_C2S = id("teleport_request");

	private MRPortalNetworking() {
	}

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(ADD_WAYPOINT_C2S, (server, player, handler, buffer, responseSender) -> server.execute(() -> {
			ServerWaypointStore store = ServerWaypointStore.get(server);
			List<WaypointData> waypoints = store.getWaypoints(player.getUUID());
			if (waypoints.size() >= MRPortalConfigManager.get().maxWaypoints) {
				player.displayClientMessage(Component.translatable("message.mr_portal.max_waypoints", MRPortalConfigManager.get().maxWaypoints), true);
				return;
			}

			WaypointData waypoint = new WaypointData(
				UUID.randomUUID(),
				store.nextName(player.getUUID()),
				player.serverLevel().dimension(),
				player.getX(),
				player.getY(),
				player.getZ()
			);
			store.addWaypoint(player.getUUID(), waypoint);
			sendWaypointScreen(player, hasCreativeView(player));
		}));

		ServerPlayNetworking.registerGlobalReceiver(DELETE_WAYPOINT_C2S, (server, player, handler, buffer, responseSender) -> {
			UUID waypointId = buffer.readUUID();
			server.execute(() -> {
				ServerWaypointStore.get(server).deleteWaypoint(player.getUUID(), waypointId);
				sendWaypointScreen(player, hasCreativeView(player));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(TELEPORT_REQUEST_C2S, (server, player, handler, buffer, responseSender) -> {
			UUID waypointId = buffer.readUUID();
			server.execute(() -> {
				ServerWaypointStore store = ServerWaypointStore.get(server);
				store.getWaypoint(player.getUUID(), waypointId).ifPresent(waypoint -> {
					boolean creativeView = hasCreativeView(player);
					if (!creativeView && !waypoint.dimension().equals(player.serverLevel().dimension())) {
						player.displayClientMessage(Component.translatable("message.mr_portal.same_dimension_only"), true);
						return;
					}

					PendingTeleportManager manager = PendingTeleportManager.get(server);
					if (manager.beginTeleport(player, waypoint, creativeView)) {
						player.closeContainer();
					}
				});
			});
		});
	}

	public static void sendWaypointScreen(ServerPlayer player, boolean creativeView) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeBoolean(creativeView);
		buffer.writeUtf(player.serverLevel().dimension().location().toString());

		List<String> dimensions = new ArrayList<>();
		for (ServerLevel level : player.server.getAllLevels()) {
			dimensions.add(level.dimension().location().toString());
		}
		dimensions.sort(String::compareTo);
		buffer.writeVarInt(dimensions.size());
		for (String dimension : dimensions) {
			buffer.writeUtf(dimension);
		}

		List<WaypointData> waypoints = ServerWaypointStore.get(player.server).getWaypoints(player.getUUID());
		buffer.writeVarInt(waypoints.size());
		for (WaypointData waypoint : waypoints) {
			waypoint.write(buffer);
		}

		ServerPlayNetworking.send(player, OPEN_SCREEN_S2C, buffer);
	}

	public static void broadcastPortalCreate(MinecraftServer server, PortalSession session) {
		int openDelay = MRPortalConfigManager.get().portalOpenDelay;
		int lifetime = MRPortalConfigManager.get().portalLifetimeTicks();
		float scale = (float) MRPortalConfigManager.get().portalScale;
		broadcastSinglePortal(server, session.sessionId(), session.sourceDimension().location().toString(), session.sourcePos(), session.sourceYaw(), true, openDelay, lifetime, scale);
		broadcastSinglePortal(server, session.sessionId(), session.destinationDimension().location().toString(), session.destinationPos(), session.destinationYaw(), false, openDelay, lifetime, scale);
	}

	public static void broadcastPortalClose(MinecraftServer server, UUID sessionId) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			FriendlyByteBuf buffer = PacketByteBufs.create();
			buffer.writeUUID(sessionId);
			ServerPlayNetworking.send(player, PORTAL_CLOSE_S2C, buffer);
		}
	}

	private static void broadcastSinglePortal(MinecraftServer server, UUID sessionId, String dimensionId, Vec3 pos, float yaw, boolean source, int openDelay, int lifetime, float scale) {
		ServerLevel level = server.getLevel(com.mentality.mrportal.util.DimensionUtil.levelKey(dimensionId));
		if (level == null) {
			return;
		}

		for (ServerPlayer nearbyPlayer : PlayerLookup.around(level, pos, 64.0D)) {
			FriendlyByteBuf buffer = PacketByteBufs.create();
			buffer.writeUUID(sessionId);
			buffer.writeBoolean(source);
			buffer.writeUtf(dimensionId);
			buffer.writeDouble(pos.x);
			buffer.writeDouble(pos.y);
			buffer.writeDouble(pos.z);
			buffer.writeFloat(yaw);
			buffer.writeVarInt(openDelay);
			buffer.writeVarInt(lifetime);
			buffer.writeFloat(scale);
			ServerPlayNetworking.send(nearbyPlayer, PORTAL_CREATE_S2C, buffer);
		}
	}

	private static boolean hasCreativeView(ServerPlayer player) {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if (MRPortalItems.isInfinite(stack) || player.getAbilities().instabuild) {
				return true;
			}
		}
		return false;
	}

	private static ResourceLocation id(String path) {
		return new ResourceLocation(MRPortal.MOD_ID, path);
	}
}