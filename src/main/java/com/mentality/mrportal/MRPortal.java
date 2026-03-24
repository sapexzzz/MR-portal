package com.mentality.mrportal;

import com.mentality.mrportal.config.MRPortalConfigManager;
import com.mentality.mrportal.item.MRPortalItems;
import com.mentality.mrportal.network.MRPortalNetworking;
import com.mentality.mrportal.portal.PendingTeleportManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MRPortal implements ModInitializer {
	public static final String MOD_ID = "mr_portal";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MRPortalConfigManager.load();
		MRPortalItems.register();
		MRPortalNetworking.registerServer();
		ServerTickEvents.END_SERVER_TICK.register(PendingTeleportManager::tick);

		LOGGER.info("MR Portal initialized");
	}
}