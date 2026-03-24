package com.mentality.mrportal.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mentality.mrportal.config.MRPortalConfigManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class ModTranslation {
	private static final Gson GSON = new Gson();
	private static Map<String, String> enTranslations = new HashMap<>();
	private static Map<String, String> ruTranslations = new HashMap<>();
	private static boolean loaded = false;

	private ModTranslation() {
	}

	public static void load() {
		if (loaded) {
			return;
		}
		enTranslations = loadLang("en_us");
		ruTranslations = loadLang("ru_ru");
		loaded = true;
	}

	private static Map<String, String> loadLang(String locale) {
		try (InputStream stream = ModTranslation.class.getResourceAsStream("/assets/mr_portal/lang/" + locale + ".json")) {
			if (stream == null) {
				return new HashMap<>();
			}
			Map<String, String> map = GSON.fromJson(
				new InputStreamReader(stream, StandardCharsets.UTF_8),
				new TypeToken<Map<String, String>>(){}.getType()
			);
			return map != null ? map : new HashMap<>();
		} catch (Exception e) {
			return new HashMap<>();
		}
	}

	public static MutableComponent get(String key, Object... args) {
		String language = MRPortalConfigManager.get().language;
		if ("auto".equals(language)) {
			return Component.translatable(key, args);
		}
		load();
		Map<String, String> translations = "ru".equals(language) ? ruTranslations : enTranslations;
		String template = translations.getOrDefault(key, key);
		if (args.length > 0) {
			try {
				template = String.format(template, args);
			} catch (Exception ignored) {
			}
		}
		return Component.literal(template);
	}
}
