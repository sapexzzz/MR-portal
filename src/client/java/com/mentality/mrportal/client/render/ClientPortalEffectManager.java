package com.mentality.mrportal.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleTypes;
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
	private static final int SPARK_DURATION = 10;
	private static final int CLOSE_DURATION = 12;
	private static final Map<UUID, List<PortalEffect>> EFFECTS = new HashMap<>();
	private static PreviewSpark previewSpark;

	private ClientPortalEffectManager() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(ClientPortalEffectManager::tick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> render(context.matrixStack(), context.camera().getPosition()));
	}

	public static void addEffect(UUID sessionId, boolean source, ResourceKey<Level> dimension, double x, double y, double z, float yaw, int openDelay, int lifetime, float scale) {
		removeMatchingPreview(dimension, x, y, z);
		PortalEffect effect = new PortalEffect(dimension, new Vec3(x, y, z), yaw, openDelay, lifetime, scale);
		EFFECTS.computeIfAbsent(sessionId, ignored -> new ArrayList<>()).add(effect);
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null && minecraft.level.dimension().equals(dimension)) {
			minecraft.level.playLocalSound(x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.05F, false);
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

	public static void showPreviewSpark(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float scale) {
		previewSpark = new PreviewSpark(dimension, new Vec3(x, y, z), yaw, scale);
	}

	public static void hidePreviewSpark() {
		previewSpark = null;
	}

	private static void tick(Minecraft client) {
		if (client.level == null) {
			EFFECTS.clear();
			previewSpark = null;
			return;
		}

		if (previewSpark != null && client.level.dimension().equals(previewSpark.dimension)) {
			previewSpark.age++;
			spawnSparkParticles(client, previewSpark.position, previewSpark.age, true);
		}

		Iterator<Map.Entry<UUID, List<PortalEffect>>> iterator = EFFECTS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, List<PortalEffect>> entry = iterator.next();
			entry.getValue().removeIf(effect -> {
				effect.age++;
				if (!effect.playedOpenSound && effect.age >= SPARK_DURATION && client.level.dimension().equals(effect.dimension)) {
					client.level.playLocalSound(effect.position.x, effect.position.y, effect.position.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.85F, 1.0F, false);
					effect.playedOpenSound = true;
				}
				if (client.level.dimension().equals(effect.dimension)) {
					if (effect.age <= SPARK_DURATION) {
						spawnSparkParticles(client, effect.position, effect.age, false);
					}
					if (effect.closing && effect.closeTick >= CLOSE_DURATION / 2) {
						spawnSparkParticles(client, effect.position, effect.closeTick, false);
					}
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

	private static void spawnSparkParticles(Minecraft client, Vec3 position, int tick, boolean preview) {
		if (client.level == null || tick % 2 != 0) {
			return;
		}
		float swirl = tick * 0.35F;
		for (int index = 0; index < 4; index++) {
			double angle = swirl + index * (Math.PI / 2.0D);
			double offsetX = Math.cos(angle) * 0.1D;
			double offsetZ = Math.sin(angle) * 0.1D;
			client.level.addParticle(ParticleTypes.END_ROD, position.x + offsetX, position.y, position.z + offsetZ, offsetX * 0.06D, 0.01D, offsetZ * 0.06D);
		}
		client.level.addParticle(preview ? ParticleTypes.GLOW : ParticleTypes.END_ROD, position.x, position.y, position.z, 0.0D, 0.015D, 0.0D);
	}

	private static void render(PoseStack poseStack, Vec3 cameraPos) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		if (previewSpark != null && minecraft.level != null && minecraft.level.dimension().equals(previewSpark.dimension)) {
			renderDisk(poseStack, cameraPos, previewSpark.position, previewSpark.yaw, 0.08F * previewSpark.scale, 0.12F * previewSpark.scale, 1.0F, 1.0F, 1.0F, 0.95F);
		}

		for (List<PortalEffect> effects : EFFECTS.values()) {
			for (PortalEffect effect : effects) {
				if (minecraft.level == null || !effect.dimension.equals(minecraft.level.dimension())) {
					continue;
				}

				float closeProgress = effect.closing ? Mth.clamp(effect.closeTick / (float) CLOSE_DURATION, 0.0F, 1.0F) : 0.0F;
				float openProgress = Mth.clamp((effect.age - SPARK_DURATION) / (float) Math.max(1, effect.openDelay - SPARK_DURATION), 0.0F, 1.0F);
				float portalProgress = effect.closing ? 1.0F - closeProgress : openProgress;
				float closingSparkAlpha = effect.closing ? Mth.clamp((closeProgress - 0.45F) / 0.55F, 0.0F, 1.0F) * 0.95F : 0.0F;
				float openingSparkAlpha = effect.age <= SPARK_DURATION ? 1.0F - (effect.age / (float) SPARK_DURATION) : 0.0F;
				float portalScale = Mth.lerp(portalProgress, 0.04F, effect.scale);

				if (openingSparkAlpha > 0.0F || closingSparkAlpha > 0.0F) {
					renderDisk(poseStack, cameraPos, effect.position, effect.yaw, 0.08F, 0.12F, 1.0F, 1.0F, 1.0F, Math.max(openingSparkAlpha, closingSparkAlpha));
				}

				if (portalProgress > 0.0F) {
					float alpha = effect.closing ? 1.0F - closeProgress * 0.15F : 0.96F;
					renderDisk(poseStack, cameraPos, effect.position, effect.yaw, 1.18F * portalScale, 1.26F * portalScale, 0.02F, 0.02F, 0.04F, alpha);
					renderDisk(poseStack, cameraPos, effect.position, effect.yaw, 1.28F * portalScale, 1.34F * portalScale, 0.18F, 0.18F, 0.22F, 0.18F * alpha);
				}
			}
		}

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private static void renderDisk(PoseStack poseStack, Vec3 cameraPos, Vec3 worldPos, float yaw, float innerRadius, float outerRadius, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		poseStack.translate(worldPos.x - cameraPos.x, worldPos.y - cameraPos.y, worldPos.z - cameraPos.z);
		poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));

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

	private static void removeMatchingPreview(ResourceKey<Level> dimension, double x, double y, double z) {
		if (previewSpark == null) {
			return;
		}
		if (previewSpark.dimension.equals(dimension) && previewSpark.position.distanceToSqr(x, y, z) < 0.01D) {
			previewSpark = null;
		}
	}

	private static final class PortalEffect {
		private final ResourceKey<Level> dimension;
		private final Vec3 position;
		private final float yaw;
		private final int openDelay;
		private final int lifetime;
		private final float scale;
		private int age;
		private boolean closing;
		private int closeTick;
		private boolean playedOpenSound;

		private PortalEffect(ResourceKey<Level> dimension, Vec3 position, float yaw, int openDelay, int lifetime, float scale) {
			this.dimension = dimension;
			this.position = position;
			this.yaw = yaw;
			this.openDelay = openDelay;
			this.lifetime = lifetime;
			this.scale = scale;
		}
	}

	private static final class PreviewSpark {
		private final ResourceKey<Level> dimension;
		private final Vec3 position;
		private final float yaw;
		private final float scale;
		private int age;

		private PreviewSpark(ResourceKey<Level> dimension, Vec3 position, float yaw, float scale) {
			this.dimension = dimension;
			this.position = position;
			this.yaw = yaw;
			this.scale = scale;
		}
	}
}