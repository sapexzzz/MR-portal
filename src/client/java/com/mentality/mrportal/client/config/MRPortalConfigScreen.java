package com.mentality.mrportal.client.config;

import com.mentality.mrportal.config.MRPortalConfig;
import com.mentality.mrportal.config.MRPortalConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MRPortalConfigScreen {
	private MRPortalConfigScreen() {
	}

	public static Screen create(Screen parent) {
		MRPortalConfig config = MRPortalConfigManager.get();
		MRPortalConfig defaults = new MRPortalConfig();

		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.literal("MR Portal"))
			.setSavingRunnable(() -> {
				MRPortalConfigManager.get().sanitize();
				MRPortalConfigManager.save();
				applyLanguage(MRPortalConfigManager.get().language);
			});

		ConfigEntryBuilder entry = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.mr_portal.category.general"));

		general.addEntry(entry.startSelector(
				Component.translatable("config.mr_portal.language"),
				new String[]{"auto", "en", "ru"},
				config.language
			)
			.setDefaultValue(defaults.language)
			.setNameProvider(val -> {
				if ("auto".equals(val)) return Component.literal("Auto");
				if ("ru".equals(val)) return Component.literal("Русский");
				return Component.literal("English");
			})
			.setSaveConsumer(val -> config.language = val)
			.setTooltip(Component.translatable("config.mr_portal.language.tooltip"))
			.build());

		general.addEntry(entry.startIntField(Component.translatable("config.mr_portal.cooldown_seconds"), config.cooldownSeconds)
			.setDefaultValue(defaults.cooldownSeconds)
			.setMin(0)
			.setSaveConsumer(val -> config.cooldownSeconds = val)
			.build());

		general.addEntry(entry.startIntField(Component.translatable("config.mr_portal.max_waypoints"), config.maxWaypoints)
			.setDefaultValue(defaults.maxWaypoints)
			.setMin(1)
			.setSaveConsumer(val -> config.maxWaypoints = val)
			.build());

		general.addEntry(entry.startIntField(Component.translatable("config.mr_portal.portal_pearl_cost"), config.portalPearlCost)
			.setDefaultValue(defaults.portalPearlCost)
			.setMin(0)
			.setSaveConsumer(val -> config.portalPearlCost = val)
			.build());

		ConfigCategory portal = builder.getOrCreateCategory(Component.translatable("config.mr_portal.category.portal"));

		portal.addEntry(entry.startIntField(Component.translatable("config.mr_portal.portal_open_delay"), config.portalOpenDelay)
			.setDefaultValue(defaults.portalOpenDelay)
			.setMin(1)
			.setSaveConsumer(val -> config.portalOpenDelay = val)
			.setTooltip(Component.translatable("config.mr_portal.portal_open_delay.tooltip"))
			.build());

		portal.addEntry(entry.startIntField(Component.translatable("config.mr_portal.portal_active_ticks"), config.portalActiveTicks)
			.setDefaultValue(defaults.portalActiveTicks)
			.setMin(1)
			.setSaveConsumer(val -> config.portalActiveTicks = val)
			.setTooltip(Component.translatable("config.mr_portal.portal_active_ticks.tooltip"))
			.build());

		portal.addEntry(entry.startIntField(Component.translatable("config.mr_portal.portal_close_delay"), config.portalCloseDelayTicks)
			.setDefaultValue(defaults.portalCloseDelayTicks)
			.setMin(1)
			.setSaveConsumer(val -> config.portalCloseDelayTicks = val)
			.setTooltip(Component.translatable("config.mr_portal.portal_close_delay.tooltip"))
			.build());

		portal.addEntry(entry.startDoubleField(Component.translatable("config.mr_portal.portal_scale"), config.portalScale)
			.setDefaultValue(defaults.portalScale)
			.setMin(0.5D)
			.setSaveConsumer(val -> config.portalScale = val)
			.build());

		portal.addEntry(entry.startDoubleField(Component.translatable("config.mr_portal.portal_spawn_distance"), config.portalSpawnDistance)
			.setDefaultValue(defaults.portalSpawnDistance)
			.setMin(0.0D)
			.setSaveConsumer(val -> config.portalSpawnDistance = val)
			.build());

		portal.addEntry(entry.startBooleanToggle(Component.translatable("config.mr_portal.apply_blindness"), config.applyBlindnessOnTeleport)
			.setDefaultValue(defaults.applyBlindnessOnTeleport)
			.setSaveConsumer(val -> config.applyBlindnessOnTeleport = val)
			.build());

		portal.addEntry(entry.startIntField(Component.translatable("config.mr_portal.blindness_duration"), config.blindnessDuration)
			.setDefaultValue(defaults.blindnessDuration)
			.setMin(0)
			.setSaveConsumer(val -> config.blindnessDuration = val)
			.setTooltip(Component.translatable("config.mr_portal.blindness_duration.tooltip"))
			.build());

		return builder.build();
	}

	private static void applyLanguage(String language) {
		if ("auto".equals(language)) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		String targetLang = "ru".equals(language) ? "ru_ru" : "en_us";
		if (!minecraft.getLanguageManager().getSelected().equals(targetLang)) {
			minecraft.getLanguageManager().setSelected(targetLang);
			minecraft.reloadResourcePacks();
		}
	}
}
