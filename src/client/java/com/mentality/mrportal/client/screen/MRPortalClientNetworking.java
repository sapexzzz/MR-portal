package com.mentality.mrportal.client.screen;

import com.mentality.mrportal.client.render.ClientPortalEffectManager;
import com.mentality.mrportal.item.MRPortalItems;
import com.mentality.mrportal.network.MRPortalNetworking;
import com.mentality.mrportal.util.DimensionUtil;
import com.mentality.mrportal.waypoint.WaypointData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MRPortalClientNetworking {
	private MRPortalClientNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(MRPortalNetworking.OPEN_SCREEN_S2C, (client, handler, buffer, responseSender) -> {
			boolean creativeView = buffer.readBoolean();
			ResourceKey<Level> currentDimension = DimensionUtil.levelKey(buffer.readUtf(128));
			int dimensionCount = buffer.readVarInt();
			List<ResourceKey<Level>> dimensions = new ArrayList<>();
			for (int index = 0; index < dimensionCount; index++) {
				dimensions.add(DimensionUtil.levelKey(buffer.readUtf(128)));
			}

			int waypointCount = buffer.readVarInt();
			List<WaypointData> waypoints = new ArrayList<>();
			for (int index = 0; index < waypointCount; index++) {
				waypoints.add(WaypointData.read(buffer));
			}

			int availablePearls = buffer.readVarInt();
			int requiredPearls = buffer.readVarInt();
			double previewX = buffer.readDouble();
			double previewY = buffer.readDouble();
			double previewZ = buffer.readDouble();
			float previewYaw = buffer.readFloat();
			float previewScale = buffer.readFloat();

			client.execute(() -> {
				boolean scrollMode = false;
				if (!creativeView && client.player != null) {
					boolean hasStaff = MRPortalItems.isPortalStaff(client.player.getMainHandItem()) || MRPortalItems.isPortalStaff(client.player.getOffhandItem());
					if (!hasStaff) {
						scrollMode = true;
					}
				}
				WaypointScreen screen = new WaypointScreen(creativeView, currentDimension, dimensions, waypoints, availablePearls, requiredPearls, previewX, previewY, previewZ, previewYaw, previewScale);
				screen.setUseScroll(scrollMode);
				client.setScreen(screen);
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(MRPortalNetworking.PORTAL_CREATE_S2C, (client, handler, buffer, responseSender) -> {
			UUID sessionId = buffer.readUUID();
			boolean source = buffer.readBoolean();
			ResourceKey<Level> dimension = DimensionUtil.levelKey(buffer.readUtf(128));
			double x = buffer.readDouble();
			double y = buffer.readDouble();
			double z = buffer.readDouble();
			float yaw = buffer.readFloat();
			int openDelay = buffer.readVarInt();
			int lifetime = buffer.readVarInt();
			float scale = buffer.readFloat();
			float animSpeed = buffer.readFloat();
			client.execute(() -> ClientPortalEffectManager.addEffect(sessionId, source, dimension, x, y, z, yaw, openDelay, lifetime, scale, animSpeed));
		});

		ClientPlayNetworking.registerGlobalReceiver(MRPortalNetworking.PORTAL_CLOSE_S2C, (client, handler, buffer, responseSender) -> {
			UUID sessionId = buffer.readUUID();
			client.execute(() -> ClientPortalEffectManager.closeSession(sessionId));
		});

		ClientPlayNetworking.registerGlobalReceiver(MRPortalNetworking.PREVIEW_SPARK_S2C, (client, handler, buffer, responseSender) -> {
			UUID playerId = buffer.readUUID();
			ResourceKey<Level> dimension = DimensionUtil.levelKey(buffer.readUtf(128));
			double x = buffer.readDouble();
			double y = buffer.readDouble();
			double z = buffer.readDouble();
			float yaw = buffer.readFloat();
			float scale = buffer.readFloat();
			client.execute(() -> ClientPortalEffectManager.showRemoteSpark(playerId, dimension, x, y, z, yaw, scale));
		});

		ClientPlayNetworking.registerGlobalReceiver(MRPortalNetworking.PREVIEW_SPARK_REMOVE_S2C, (client, handler, buffer, responseSender) -> {
			UUID playerId = buffer.readUUID();
			client.execute(() -> ClientPortalEffectManager.hideRemoteSpark(playerId));
		});
	}

	public static void sendAddWaypoint() {
		sendAddWaypoint("");
	}

	public static void sendAddWaypoint(String name) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeUtf(name, 64);
		ClientPlayNetworking.send(MRPortalNetworking.ADD_WAYPOINT_C2S, buffer);
	}

	public static void sendDeleteWaypoint(UUID waypointId) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeUUID(waypointId);
		ClientPlayNetworking.send(MRPortalNetworking.DELETE_WAYPOINT_C2S, buffer);
	}

	public static void sendRenameWaypoint(UUID waypointId, String name) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeUUID(waypointId);
		buffer.writeUtf(name, 64);
		ClientPlayNetworking.send(MRPortalNetworking.RENAME_WAYPOINT_C2S, buffer);
	}

	public static void sendTeleportRequest(UUID waypointId, boolean useScroll) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buffer.writeUUID(waypointId);
		buffer.writeBoolean(useScroll);
		ClientPlayNetworking.send(MRPortalNetworking.TELEPORT_REQUEST_C2S, buffer);
	}

	public static void sendScreenClosed() {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		ClientPlayNetworking.send(MRPortalNetworking.SCREEN_CLOSED_C2S, buffer);
	}

	public static void sendOpenByKeybind() {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		ClientPlayNetworking.send(MRPortalNetworking.OPEN_BY_KEYBIND_C2S, buffer);
	}
}