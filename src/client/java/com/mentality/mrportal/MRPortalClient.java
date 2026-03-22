package com.mentality.mrportal;

import com.mentality.mrportal.client.render.ClientPortalEffectManager;
import com.mentality.mrportal.client.screen.MRPortalClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public class MRPortalClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MRPortalClientNetworking.register();
		ClientPortalEffectManager.register();
	}
}