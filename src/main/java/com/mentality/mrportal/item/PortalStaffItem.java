package com.mentality.mrportal.item;

import com.mentality.mrportal.config.MRPortalConfigManager;
import net.minecraft.core.particles.ParticleTypes;
import com.mentality.mrportal.network.MRPortalNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PortalStaffItem extends Item {
	private static final String TOOLTIP_READY_KEY = "MrPortalTooltipReady";
	private static final String TOOLTIP_SECONDS_KEY = "MrPortalTooltipSeconds";
	private final boolean infinite;

	public PortalStaffItem(Properties properties, boolean infinite) {
		super(properties);
		this.infinite = infinite;
	}

	public boolean isInfinite() {
		return this.infinite;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			MRPortalNetworking.sendWaypointScreen(serverPlayer, this.infinite || player.getAbilities().instabuild);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (!(entity instanceof Player player)) {
			return;
		}

		if (level.isClientSide && !this.infinite) {
			CompoundTag tag = stack.getOrCreateTag();
			boolean onCooldown = player.getCooldowns().isOnCooldown(this);
			tag.putBoolean(TOOLTIP_READY_KEY, !onCooldown);
			tag.putInt(TOOLTIP_SECONDS_KEY, onCooldown ? Math.max(1, Math.round(player.getCooldowns().getCooldownPercent(this, 0.0F) * MRPortalConfigManager.get().cooldownSeconds)) : 0);
		}

		if (!level.isClientSide) {
			return;
		}

		boolean offhand = player.getOffhandItem() == stack;
		if ((!isSelected && !offhand) || level.random.nextInt(10) != 0) {
			return;
		}
		double px = player.getX() + (level.random.nextDouble() - 0.5D) * 0.6D;
		double py = player.getY() + 1.0D + level.random.nextDouble() * 0.5D;
		double pz = player.getZ() + (level.random.nextDouble() - 0.5D) * 0.6D;
		level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0D, 0.01D, 0.0D);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
		tooltipComponents.add(Component.translatable(this.infinite ? "item.mr_portal.infinite_portal_staff.tooltip" : "item.mr_portal.portal_staff.tooltip").withStyle(ChatFormatting.GRAY));
		if (this.infinite) {
			tooltipComponents.add(Component.translatable("item.mr_portal.portal_staff.cooldown_disabled").withStyle(ChatFormatting.AQUA));
			return;
		}

		CompoundTag tag = stack.getTag();
		if (tag == null || tag.getBoolean(TOOLTIP_READY_KEY)) {
			tooltipComponents.add(Component.translatable("item.mr_portal.portal_staff.cooldown_ready").withStyle(ChatFormatting.GREEN));
			return;
		}

		int secondsLeft = Math.max(1, tag.getInt(TOOLTIP_SECONDS_KEY));
		tooltipComponents.add(Component.translatable("item.mr_portal.portal_staff.cooldown_seconds", secondsLeft).withStyle(ChatFormatting.GOLD));
	}
}