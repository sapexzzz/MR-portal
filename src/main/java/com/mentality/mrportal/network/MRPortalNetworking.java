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
	public static final ResourceLocation PREVIEW_SPARK_S2C = id("preview_spark");
	public static final ResourceLocation PREVIEW_SPARK_REMOVE_S2C = id("preview_spark_remove");
	public static final ResourceLocation ADD_WAYPOINT_C2S = id("add_waypoint");
	public static final ResourceLocation DELETE_WAYPOINT_C2S = id("delete_waypoint");
	public static final ResourceLocation RENAME_WAYPOINT_C2S = id("rename_waypoint");
	public static final ResourceLocation TELEPORT_REQUEST_C2S = id("teleport_request");
	public static final ResourceLocation SCREEN_CLOSED_C2S = id("screen_closed");
	public static final ResourceLocation OPEN_BY_KEYBIND_C2S = id("open_by_keybind");

	private static final java.util.Map<UUID, Vec3> activePreviewSparks = new java.util.HashMap<>();
	private static final java.util.Map<UUID, String> activePreviewDimensions = new java.util.HashMap<>();

	private MRPortalNetworking() {
	}

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(ADD_WAYPOINT_C2S, (server, player, handler, buffer, responseSender) -> {
			String requestedName = buffer.readUtf(64).trim();
			server.execute(() -> {
			ServerWaypointStore store = ServerWaypointStore.get(server);
			List<WaypointData> waypoints = store.getWaypoints(player.getUUID());
			if (waypoints.size() >= MRPortalConfigManager.get().maxWaypoints) {
				player.displayClientMessage(Component.translatable("message.mr_portal.max_waypoints", MRPortalConfigManager.get().maxWaypoints), true);
				return;
			}

			WaypointData waypoint = new WaypointData(
				UUID.randomUUID(),
				store.normalizeName(player.getUUID(), requestedName),
				player.serverLevel().dimension(),
				player.getX(),
				player.getY(),
				player.getZ()
			);
			store.addWaypoint(player.getUUID(), waypoint);
			sendWaypointScreen(player, hasCreativeView(player));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(DELETE_WAYPOINT_C2S, (server, player, handler, buffer, responseSender) -> {
			UUID waypointId = buffer.readUUID();
			server.execute(() -> {
				ServerWaypointStore.get(server).deleteWaypoint(player.getUUID(), waypointId);
				sendWaypointScreen(player, hasCreativeView(player));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(RENAME_WAYPOINT_C2S, (server, player, handler, buffer, responseSender) -> {
			UUID waypointId = buffer.readUUID();
			String requestedName = buffer.readUtf(64).trim();
			server.execute(() -> {
				ServerWaypointStore store = ServerWaypointStore.get(server);
				String name = store.normalizeName(player.getUUID(), requestedName);
				store.renameWaypoint(player.getUUID(), waypointId, name);
				sendWaypointScreen(player, hasCreativeView(player));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(SCREEN_CLOSED_C2S, (server, player, handler, buffer, responseSender) -> {
			server.execute(() -> removePreviewSpark(server, player));
		});

		ServerPlayNetworking.registerGlobalReceiver(TELEPORT_REQUEST_C2S, (server, player, handler, buffer, responseSender) -> {
			UUID waypointId = buffer.readUUID();
			boolean useScroll = buffer.readBoolean();
			server.execute(() -> {
				ServerWaypointStore store = ServerWaypointStore.get(server);
				store.getWaypoint(player.getUUID(), waypointId).ifPresent(waypoint -> {
					boolean creativeView = hasCreativeView(player);
					if (!creativeView && !waypoint.dimension().equals(player.serverLevel().dimension())) {
						player.displayClientMessage(Component.translatable("message.mr_portal.same_dimension_only"), true);
						return;
					}

					PendingTeleportManager manager = PendingTeleportManager.get(server);
					if (useScroll && !creativeView) {
						if (manager.beginScrollTeleport(player, waypoint)) {
							removePreviewSpark(server, player);
							player.closeContainer();
						}
					} else {
						if (manager.beginTeleport(player, waypoint, creativeView)) {
							removePreviewSpark(server, player);
							player.closeContainer();
						}
					}
				});
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(OPEN_BY_KEYBIND_C2S, (server, player, handler, buffer, responseSender) -> {
			server.execute(() -> {
				boolean creativeView = hasCreativeView(player);
				boolean hasStaff = false;
				for (net.minecraft.world.item.ItemStack stack : player.getInventory().items) {
					if (MRPortalItems.isPortalStaff(stack)) {
						hasStaff = true;
						break;
					}
				}
				for (InteractionHand hand : InteractionHand.values()) {
					if (MRPortalItems.isPortalStaff(player.getItemInHand(hand))) {
						hasStaff = true;
						break;
					}
				}
				if (hasStaff || creativeView) {
					sendWaypointScreen(player, creativeView);
					return;
				}
				boolean hasScroll = false;
				for (net.minecraft.world.item.ItemStack stack : player.getInventory().items) {
					if (MRPortalItems.isTeleportScroll(stack)) {
						hasScroll = true;
						break;
					}
				}
				for (InteractionHand hand : InteractionHand.values()) {
					if (MRPortalItems.isTeleportScroll(player.getItemInHand(hand))) {
						hasScroll = true;
						break;
					}
				}
				if (hasScroll) {
					sendWaypointScreen(player, false);
				}
			});
		});
	}

	public static void sendWaypointScreen(ServerPlayer player, boolean creativeView) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		Vec3 previewPos = PendingTeleportManager.calculateSourcePortalCenter(player, MRPortalConfigManager.get());
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

		buffer.writeVarInt(PendingTeleportManager.countEnderPearls(player));
		buffer.writeVarInt(PendingTeleportManager.getRequiredPearls(creativeView));
		buffer.writeDouble(previewPos.x);
		buffer.writeDouble(previewPos.y);
		buffer.writeDouble(previewPos.z);
		buffer.writeFloat(player.getYRot());
		buffer.writeFloat((float) MRPortalConfigManager.get().portalScale);

		ServerPlayNetworking.send(player, OPEN_SCREEN_S2C, buffer);
		broadcastPreviewSpark(player, previewPos);
	}

	public static void broadcastPortalCreate(MinecraftServer server, PortalSession session) {
		int openDelay = MRPortalConfigManager.get().portalOpenDelay;
		int lifetime = MRPortalConfigManager.get().portalLifetimeTicks() + 40;
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

	public static void sendPortalCreateToPlayer(ServerPlayer player, PortalSession session) {
		int openDelay = MRPortalConfigManager.get().portalOpenDelay;
		int lifetime = MRPortalConfigManager.get().portalLifetimeTicks() + 40;
		float scale = (float) MRPortalConfigManager.get().portalScale;
		long age = player.server.getTickCount() - session.createdTick();
		int remaining = Math.max(1, lifetime - (int) age);
		int adjustedOpenDelay = Math.max(0, openDelay - (int) age);

		sendSinglePortalToPlayer(player, session.sessionId(), session.destinationDimension().location().toString(), session.destinationPos(), session.destinationYaw(), false, adjustedOpenDelay, remaining, scale);
		sendSinglePortalToPlayer(player, session.sessionId(), session.sourceDimension().location().toString(), session.sourcePos(), session.sourceYaw(), true, adjustedOpenDelay, remaining, scale);
	}

	private static void sendSinglePortalToPlayer(ServerPlayer player, UUID sessionId, String dimensionId, Vec3 pos, float yaw, boolean source, int openDelay, int lifetime, float scale) {
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
		ServerPlayNetworking.send(player, PORTAL_CREATE_S2C, buffer);
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

	private static void broadcastPreviewSpark(ServerPlayer player, Vec3 pos) {
		activePreviewSparks.put(player.getUUID(), pos);
		String dimensionId = player.serverLevel().dimension().location().toString();
		activePreviewDimensions.put(player.getUUID(), dimensionId);
		float scale = (float) MRPortalConfigManager.get().portalScale;
		float yaw = player.getYRot();

		for (ServerPlayer nearby : PlayerLookup.around(player.serverLevel(), pos, 64.0D)) {
			if (nearby.equals(player)) {
				continue;
			}
			FriendlyByteBuf buf = PacketByteBufs.create();
			buf.writeUUID(player.getUUID());
			buf.writeUtf(dimensionId);
			buf.writeDouble(pos.x);
			buf.writeDouble(pos.y);
			buf.writeDouble(pos.z);
			buf.writeFloat(yaw);
			buf.writeFloat(scale);
			ServerPlayNetworking.send(nearby, PREVIEW_SPARK_S2C, buf);
		}
	}

	public static void removePreviewSpark(MinecraftServer server, ServerPlayer player) {
		Vec3 pos = activePreviewSparks.remove(player.getUUID());
		String dimensionId = activePreviewDimensions.remove(player.getUUID());
		if (pos == null || dimensionId == null) {
			return;
		}
		ServerLevel level = server.getLevel(com.mentality.mrportal.util.DimensionUtil.levelKey(dimensionId));
		if (level == null) {
			return;
		}
		for (ServerPlayer nearby : PlayerLookup.around(level, pos, 64.0D)) {
			if (nearby.equals(player)) {
				continue;
			}
			FriendlyByteBuf buf = PacketByteBufs.create();
			buf.writeUUID(player.getUUID());
			ServerPlayNetworking.send(nearby, PREVIEW_SPARK_REMOVE_S2C, buf);
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