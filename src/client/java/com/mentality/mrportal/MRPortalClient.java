package com.mentality.mrportal;

import com.mentality.mrportal.client.render.ClientPortalEffectManager;
import com.mentality.mrportal.client.screen.MRPortalClientNetworking;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class MRPortalClient implements ClientModInitializer {
	private static KeyMapping openPortalKey;

	@Override
	public void onInitializeClient() {
		MRPortalClientNetworking.register();
		ClientPortalEffectManager.register();

		openPortalKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.mr_portal.open_gui",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_V,
			"category.mr_portal"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openPortalKey.consumeClick()) {
				if (client.player != null && client.screen == null) {
					MRPortalClientNetworking.sendOpenByKeybind();
				}
			}
		});
	}
}