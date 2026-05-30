package com.mentality.mrportal.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mentality.mrportal.MRPortal;
import com.mentality.mrportal.util.DimensionUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

public final class ServerWaypointStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type DATA_TYPE = new TypeToken<Map<String, List<StoredWaypoint>>>() { } .getType();
	private static final Map<MinecraftServer, ServerWaypointStore> INSTANCES = new WeakHashMap<>();

	private final Path filePath;
	private final Map<UUID, List<WaypointData>> byPlayer = new HashMap<>();

	private ServerWaypointStore(Path filePath) {
		this.filePath = filePath;
		this.load();
	}

	public static synchronized ServerWaypointStore get(MinecraftServer server) {
		return INSTANCES.computeIfAbsent(server, current -> {
			Path root = current.getWorldPath(LevelResource.ROOT);
			return new ServerWaypointStore(root.resolve("mr_portal_waypoints.json"));
		});
	}

	public synchronized List<WaypointData> getWaypoints(UUID playerId) {
		return List.copyOf(this.byPlayer.getOrDefault(playerId, List.of()));
	}

	public synchronized Optional<WaypointData> getWaypoint(UUID playerId, UUID waypointId) {
		return this.byPlayer.getOrDefault(playerId, List.of()).stream()
			.filter(waypoint -> waypoint.id().equals(waypointId))
			.findFirst();
	}

	public synchronized WaypointData addWaypoint(UUID playerId, WaypointData waypointData) {
		List<WaypointData> waypoints = new ArrayList<>(this.byPlayer.getOrDefault(playerId, List.of()));
		waypoints.add(waypointData);
		this.byPlayer.put(playerId, waypoints);
		this.save();
		return waypointData;
	}

	public synchronized boolean deleteWaypoint(UUID playerId, UUID waypointId) {
		List<WaypointData> waypoints = new ArrayList<>(this.byPlayer.getOrDefault(playerId, List.of()));
		boolean removed = waypoints.removeIf(waypoint -> waypoint.id().equals(waypointId));
		if (removed) {
			this.byPlayer.put(playerId, waypoints);
			this.save();
		}
		return removed;
	}

	public synchronized boolean renameWaypoint(UUID playerId, UUID waypointId, String newName) {
		List<WaypointData> waypoints = new ArrayList<>(this.byPlayer.getOrDefault(playerId, List.of()));
		for (int i = 0; i < waypoints.size(); i++) {
			WaypointData wp = waypoints.get(i);
			if (wp.id().equals(waypointId)) {
				waypoints.set(i, new WaypointData(wp.id(), newName, wp.dimension(), wp.x(), wp.y(), wp.z(), wp.favorite()));
				this.byPlayer.put(playerId, waypoints);
				this.save();
				return true;
			}
		}
		return false;
	}

	public synchronized boolean setFavoriteWaypoint(UUID playerId, UUID waypointId) {
		List<WaypointData> current = this.byPlayer.getOrDefault(playerId, List.of());
		if (current.stream().noneMatch(waypoint -> waypoint.id().equals(waypointId))) {
			return false;
		}

		List<WaypointData> updated = new ArrayList<>(current.size());
		for (WaypointData waypoint : current) {
			updated.add(new WaypointData(
				waypoint.id(),
				waypoint.name(),
				waypoint.dimension(),
				waypoint.x(),
				waypoint.y(),
				waypoint.z(),
				waypoint.id().equals(waypointId)
			));
		}
		this.byPlayer.put(playerId, updated);
		this.save();
		return true;
	}

	public synchronized boolean clearFavoriteWaypoint(UUID playerId, UUID waypointId) {
		List<WaypointData> current = this.byPlayer.getOrDefault(playerId, List.of());
		boolean changed = false;
		List<WaypointData> updated = new ArrayList<>(current.size());
		for (WaypointData waypoint : current) {
			boolean favorite = waypoint.favorite() && !waypoint.id().equals(waypointId);
			if (favorite != waypoint.favorite()) {
				changed = true;
			}
			updated.add(new WaypointData(
				waypoint.id(),
				waypoint.name(),
				waypoint.dimension(),
				waypoint.x(),
				waypoint.y(),
				waypoint.z(),
				favorite
			));
		}
		if (!changed) {
			return false;
		}
		this.byPlayer.put(playerId, updated);
		this.save();
		return true;
	}

	public synchronized Optional<WaypointData> getFavoriteWaypoint(UUID playerId) {
		return this.byPlayer.getOrDefault(playerId, List.of()).stream()
			.filter(WaypointData::favorite)
			.findFirst();
	}

	public synchronized String nextName(UUID playerId) {
		int nextIndex = this.byPlayer.getOrDefault(playerId, List.of()).size() + 1;
		return "Point " + nextIndex;
	}

	public synchronized String normalizeName(UUID playerId, String requestedName) {
		String trimmed = requestedName == null ? "" : requestedName.trim();
		if (trimmed.isEmpty()) {
			return this.nextName(playerId);
		}
		if (trimmed.length() > 32) {
			return trimmed.substring(0, 32);
		}
		return trimmed;
	}

	private void load() {
		this.byPlayer.clear();
		if (!Files.exists(this.filePath)) {
			return;
		}
		try {
			String json = Files.readString(this.filePath);
			Map<String, List<StoredWaypoint>> loaded = GSON.fromJson(json, DATA_TYPE);
			if (loaded == null) {
				return;
			}
			for (Map.Entry<String, List<StoredWaypoint>> entry : loaded.entrySet()) {
				UUID playerId = UUID.fromString(entry.getKey());
				List<WaypointData> waypoints = entry.getValue().stream()
					.map(StoredWaypoint::toWaypointData)
					.toList();
				this.byPlayer.put(playerId, new ArrayList<>(waypoints));
			}
		} catch (IOException | RuntimeException exception) {
			MRPortal.LOGGER.error("Failed to load waypoints from {}", this.filePath, exception);
		}
	}

	private synchronized void save() {
		try {
			Files.createDirectories(this.filePath.getParent());
			Map<String, List<StoredWaypoint>> serialized = new HashMap<>();
			for (Map.Entry<UUID, List<WaypointData>> entry : this.byPlayer.entrySet()) {
				List<StoredWaypoint> waypoints = entry.getValue().stream()
					.map(StoredWaypoint::fromWaypointData)
					.toList();
				serialized.put(entry.getKey().toString(), waypoints);
			}
			Files.writeString(this.filePath, GSON.toJson(serialized, DATA_TYPE));
		} catch (IOException exception) {
			MRPortal.LOGGER.error("Failed to save waypoints to {}", this.filePath, exception);
		}
	}

	private record StoredWaypoint(String id, String name, String dimension, double x, double y, double z, boolean favorite) {
		private static StoredWaypoint fromWaypointData(WaypointData waypoint) {
			return new StoredWaypoint(
				waypoint.id().toString(),
				waypoint.name(),
				waypoint.dimension().location().toString(),
				waypoint.x(),
				waypoint.y(),
				waypoint.z(),
				waypoint.favorite()
			);
		}

		private WaypointData toWaypointData() {
			return new WaypointData(
				UUID.fromString(this.id),
				this.name,
				DimensionUtil.levelKey(this.dimension),
				this.x,
				this.y,
				this.z,
				this.favorite
			);
		}
	}
}