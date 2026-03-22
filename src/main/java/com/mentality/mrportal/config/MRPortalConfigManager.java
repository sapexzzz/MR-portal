package com.mentality.mrportal.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mentality.mrportal.MRPortal;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MRPortalConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mr_portal.json");
	private static MRPortalConfig config = new MRPortalConfig();

	private MRPortalConfigManager() {
	}

	public static void load() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			if (Files.exists(CONFIG_PATH)) {
				config = GSON.fromJson(Files.readString(CONFIG_PATH), MRPortalConfig.class);
				if (config == null) {
					config = new MRPortalConfig();
				}
			} else {
				config = new MRPortalConfig();
			}
			save();
		} catch (IOException exception) {
			MRPortal.LOGGER.error("Failed to load MR Portal config", exception);
			config = new MRPortalConfig();
		}
	}

	public static void save() {
		try {
			Files.writeString(CONFIG_PATH, GSON.toJson(config));
		} catch (IOException exception) {
			MRPortal.LOGGER.error("Failed to save MR Portal config", exception);
		}
	}

	public static MRPortalConfig get() {
		return config;
	}
}