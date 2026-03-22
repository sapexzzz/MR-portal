package com.mentality.mrportal.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class DimensionUtil {
	private DimensionUtil() {
	}

	public static ResourceKey<Level> levelKey(String id) {
		return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(id));
	}

	public static String prettifyDimension(ResourceKey<Level> dimension) {
		String path = dimension.location().getPath();
		String[] parts = path.split("[_/]");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0)));
			if (part.length() > 1) {
				builder.append(part.substring(1));
			}
		}
		return builder.toString();
	}
}