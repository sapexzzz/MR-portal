package com.mentality.mrportal.waypoint;

import com.mentality.mrportal.util.DimensionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record WaypointData(UUID id, String name, ResourceKey<Level> dimension, double x, double y, double z, boolean favorite) {
	public CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("id", this.id);
		tag.putString("name", this.name);
		tag.putString("dimension", this.dimension.location().toString());
		tag.putDouble("x", this.x);
		tag.putDouble("y", this.y);
		tag.putDouble("z", this.z);
		tag.putBoolean("favorite", this.favorite);
		return tag;
	}

	public static WaypointData fromTag(CompoundTag tag) {
		return new WaypointData(
			tag.getUUID("id"),
			tag.getString("name"),
			DimensionUtil.levelKey(tag.getString("dimension")),
			tag.getDouble("x"),
			tag.getDouble("y"),
			tag.getDouble("z"),
			tag.getBoolean("favorite")
		);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(this.id);
		buffer.writeUtf(this.name);
		buffer.writeUtf(this.dimension.location().toString());
		buffer.writeDouble(this.x);
		buffer.writeDouble(this.y);
		buffer.writeDouble(this.z);
		buffer.writeBoolean(this.favorite);
	}

	public static WaypointData read(FriendlyByteBuf buffer) {
		return new WaypointData(
			buffer.readUUID(),
			buffer.readUtf(128),
			DimensionUtil.levelKey(buffer.readUtf(128)),
			buffer.readDouble(),
			buffer.readDouble(),
			buffer.readDouble(),
			buffer.readBoolean()
		);
	}
}