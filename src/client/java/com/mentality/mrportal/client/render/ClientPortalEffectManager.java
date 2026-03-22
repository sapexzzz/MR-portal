package com.mentality.mrportal.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientPortalEffectManager {
	private static final int SPARK_DURATION = 8;
	private static final int CLOSE_DURATION = 12;
	private static final Map<UUID, List<PortalEffect>> EFFECTS = new HashMap<>();

	private ClientPortalEffectManager() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(ClientPortalEffectManager::tick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> render(context.matrixStack(), context.camera()));
	}

	public static void addEffect(UUID sessionId, boolean source, ResourceKey<Level> dimension, double x, double y, double z, int openDelay, int lifetime) {
		PortalEffect effect = new PortalEffect(sessionId, source, dimension, new Vec3(x, y, z), openDelay, lifetime);
		EFFECTS.computeIfAbsent(sessionId, ignored -> new ArrayList<>()).add(effect);
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null && minecraft.level.dimension().equals(dimension)) {
			minecraft.level.playLocalSound(x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.15F, false);
		}
	}

	public static void closeSession(UUID sessionId) {
		List<PortalEffect> effects = EFFECTS.get(sessionId);
		if (effects == null) {
			return;
		}
		for (PortalEffect effect : effects) {
			effect.closing = true;
			effect.closeTick = 0;
		}
	}

	private static void tick(Minecraft client) {
		Iterator<Map.Entry<UUID, List<PortalEffect>>> iterator = EFFECTS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, List<PortalEffect>> entry = iterator.next();
			entry.getValue().removeIf(effect -> {
				effect.age++;
				if (!effect.playedOpenSound && effect.age >= SPARK_DURATION && client.level != null && client.level.dimension().equals(effect.dimension)) {
					client.level.playLocalSound(effect.position.x, effect.position.y, effect.position.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.85F, 1.0F, false);
					effect.playedOpenSound = true;
				}
				if (effect.closing) {
					effect.closeTick++;
				}
				return effect.age > effect.lifetime || effect.closeTick > CLOSE_DURATION;
			});
			if (entry.getValue().isEmpty()) {
				iterator.remove();
			}
		}
	}

	private static void render(PoseStack poseStack, Camera camera) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			EFFECTS.clear();
			return;
		}

		Vec3 cameraPos = camera.getPosition();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		for (List<PortalEffect> effects : EFFECTS.values()) {
			for (PortalEffect effect : effects) {
				if (!effect.dimension.equals(minecraft.level.dimension())) {
					continue;
				}

				float closeAlpha = effect.closing ? 1.0F - Mth.clamp(effect.closeTick / (float) CLOSE_DURATION, 0.0F, 1.0F) : 1.0F;
				float openProgress = Mth.clamp((effect.age - SPARK_DURATION) / (float) Math.max(1, effect.openDelay - SPARK_DURATION), 0.0F, 1.0F);
				float sparkAlpha = effect.age < SPARK_DURATION ? (1.0F - effect.age / (float) SPARK_DURATION) * closeAlpha : 0.0F;
				float portalScale = Mth.lerp(openProgress, 0.08F, 1.0F) * closeAlpha;

				if (sparkAlpha > 0.0F) {
					renderBillboardDisk(poseStack, camera, cameraPos, effect.position.add(0.0D, 0.14D, 0.0D), 0.13F, 0.15F, 1.0F, 0.98F, 0.88F, sparkAlpha);
					renderBillboardDisk(poseStack, camera, cameraPos, effect.position.add(0.0D, 0.14D, 0.0D), 0.24F, 0.26F, 1.0F, 0.98F, 0.72F, sparkAlpha * 0.45F);
				}

				if (portalScale > 0.0F) {
					Vec3 portalCenter = effect.position.add(0.0D, 1.0D, 0.0D);
					renderBillboardDisk(poseStack, camera, cameraPos, portalCenter, 1.18F * portalScale, 1.26F * portalScale, 0.02F, 0.02F, 0.04F, 0.96F * closeAlpha);
					renderBillboardDisk(poseStack, camera, cameraPos, portalCenter, 1.28F * portalScale, 1.34F * portalScale, 0.18F, 0.18F, 0.22F, 0.18F * closeAlpha);
				}
			}
		}

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private static void renderBillboardDisk(PoseStack poseStack, Camera camera, Vec3 cameraPos, Vec3 worldPos, float innerRadius, float outerRadius, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		poseStack.translate(worldPos.x - cameraPos.x, worldPos.y - cameraPos.y, worldPos.z - cameraPos.z);
		poseStack.mulPose(camera.rotation());

		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
		int segments = 40;
		for (int index = 0; index <= segments; index++) {
			float angle = (float) (Math.PI * 2.0D * index / segments);
			float cos = Mth.cos(angle);
			float sin = Mth.sin(angle);
			bufferBuilder.vertex(matrix, cos * outerRadius, sin * outerRadius, 0.0F).color(red, green, blue, 0.0F).endVertex();
			bufferBuilder.vertex(matrix, cos * innerRadius, sin * innerRadius, 0.0F).color(red, green, blue, alpha).endVertex();
		}
		BufferUploader.drawWithShader(bufferBuilder.end());

		bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).color(red, green, blue, alpha).endVertex();
		for (int index = 0; index <= segments; index++) {
			float angle = (float) (Math.PI * 2.0D * index / segments);
			float cos = Mth.cos(angle);
			float sin = Mth.sin(angle);
			bufferBuilder.vertex(matrix, cos * innerRadius, sin * innerRadius, 0.0F).color(red, green, blue, alpha).endVertex();
		}
		BufferUploader.drawWithShader(bufferBuilder.end());
		poseStack.popPose();
	}

	private static final class PortalEffect {
		private final ResourceKey<Level> dimension;
		private final Vec3 position;
		private final int openDelay;
		private final int lifetime;
		private int age;
		private boolean closing;
		private int closeTick;
		private boolean playedOpenSound;

		private PortalEffect(UUID sessionId, boolean source, ResourceKey<Level> dimension, Vec3 position, int openDelay, int lifetime) {
			this.dimension = dimension;
			this.position = position;
			this.openDelay = openDelay;
			this.lifetime = lifetime;
		}
	}
}