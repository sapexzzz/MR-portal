package com.mentality.mrportal.client.config;

import com.mentality.mrportal.config.MRPortalConfig;
import com.mentality.mrportal.config.MRPortalConfigManager;
import com.mentality.mrportal.util.ModTranslation;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
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
			});

		ConfigEntryBuilder entry = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(ModTranslation.get("config.mr_portal.category.general"));

		general.addEntry(entry.startSelector(
				ModTranslation.get("config.mr_portal.language"),
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
			.setTooltip(ModTranslation.get("config.mr_portal.language.tooltip"))
			.build());

		general.addEntry(entry.startIntField(ModTranslation.get("config.mr_portal.cooldown_seconds"), config.cooldownSeconds)
			.setDefaultValue(defaults.cooldownSeconds)
			.setMin(0)
			.setSaveConsumer(val -> config.cooldownSeconds = val)
			.build());

		general.addEntry(entry.startIntField(ModTranslation.get("config.mr_portal.max_waypoints"), config.maxWaypoints)
			.setDefaultValue(defaults.maxWaypoints)
			.setMin(1)
			.setSaveConsumer(val -> config.maxWaypoints = val)
			.build());

		general.addEntry(entry.startIntField(ModTranslation.get("config.mr_portal.portal_pearl_cost"), config.portalPearlCost)
			.setDefaultValue(defaults.portalPearlCost)
			.setMin(0)
			.setSaveConsumer(val -> config.portalPearlCost = val)
			.build());

		ConfigCategory portal = builder.getOrCreateCategory(ModTranslation.get("config.mr_portal.category.portal"));

		portal.addEntry(entry.startIntField(ModTranslation.get("config.mr_portal.portal_open_delay"), config.portalOpenDelay)
			.setDefaultValue(defaults.portalOpenDelay)
			.setMin(1)
			.setSaveConsumer(val -> config.portalOpenDelay = val)
			.setTooltip(ModTranslation.get("config.mr_portal.portal_open_delay.tooltip"))
			.build());

		portal.addEntry(entry.startIntField(ModTranslation.get("config.mr_portal.portal_active_ticks"), config.portalActiveTicks)
			.setDefaultValue(defaults.portalActiveTicks)
			.setMin(1)
			.setSaveConsumer(val -> config.portalActiveTicks = val)
			.setTooltip(ModTranslation.get("config.mr_portal.portal_active_ticks.tooltip"))
			.build());

		portal.addEntry(entry.startIntField(ModTranslation.get("config.mr_portal.portal_close_delay"), config.portalCloseDelayTicks)
			.setDefaultValue(defaults.portalCloseDelayTicks)
			.setMin(1)
			.setSaveConsumer(val -> config.portalCloseDelayTicks = val)
			.setTooltip(ModTranslation.get("config.mr_portal.portal_close_delay.tooltip"))
			.build());

		portal.addEntry(entry.startDoubleField(ModTranslation.get("config.mr_portal.portal_scale"), config.portalScale)
			.setDefaultValue(defaults.portalScale)
			.setMin(0.5D)
			.setSaveConsumer(val -> config.portalScale = val)
			.build());

		portal.addEntry(entry.startDoubleField(ModTranslation.get("config.mr_portal.portal_spawn_distance"), config.portalSpawnDistance)
			.setDefaultValue(defaults.portalSpawnDistance)
			.setMin(0.0D)
			.setSaveConsumer(val -> config.portalSpawnDistance = val)
			.build());

		portal.addEntry(entry.startDoubleField(ModTranslation.get("config.mr_portal.portal_vertical_offset"), config.portalVerticalOffset)
			.setDefaultValue(defaults.portalVerticalOffset)
			.setMin(-2.0D)
			.setMax(5.0D)
			.setSaveConsumer(val -> config.portalVerticalOffset = val)
			.setTooltip(ModTranslation.get("config.mr_portal.portal_vertical_offset.tooltip"))
			.build());

		portal.addEntry(entry.startDoubleField(ModTranslation.get("config.mr_portal.portal_animation_speed"), config.portalAnimationSpeed)
			.setDefaultValue(defaults.portalAnimationSpeed)
			.setMin(0.1D)
			.setMax(5.0D)
			.setSaveConsumer(val -> config.portalAnimationSpeed = val)
			.setTooltip(ModTranslation.get("config.mr_portal.portal_animation_speed.tooltip"))
			.build());

		portal.addEntry(entry.startBooleanToggle(ModTranslation.get("config.mr_portal.apply_blindness"), config.applyBlindnessOnTeleport)
			.setDefaultValue(defaults.applyBlindnessOnTeleport)
			.setSaveConsumer(val -> config.applyBlindnessOnTeleport = val)
			.build());

		portal.addEntry(entry.startIntField(ModTranslation.get("config.mr_portal.blindness_duration"), config.blindnessDuration)
			.setDefaultValue(defaults.blindnessDuration)
			.setMin(0)
			.setSaveConsumer(val -> config.blindnessDuration = val)
			.setTooltip(ModTranslation.get("config.mr_portal.blindness_duration.tooltip"))
			.build());

		return builder.build();
	}
}
