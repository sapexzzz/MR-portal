package com.mentality.mrportal.item;

import com.mentality.mrportal.network.MRPortalNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PortalStaffItem extends Item {
	private final boolean infinite;

	public PortalStaffItem(Properties properties, boolean infinite) {
		super(properties);
		this.infinite = infinite;
	}

	public boolean isInfinite() {
		return this.infinite;
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
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
		tooltipComponents.add(Component.translatable(this.infinite ? "item.mr_portal.infinite_portal_staff.tooltip" : "item.mr_portal.portal_staff.tooltip").withStyle(ChatFormatting.GRAY));
	}
}