package com.mentality.mrportal.item;

import com.mentality.mrportal.MRPortal;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MRPortalItems {
	public static final Item PORTAL_STAFF = new PortalStaffItem(new Item.Properties().stacksTo(1), false);
	public static final Item INFINITE_PORTAL_STAFF = new PortalStaffItem(new Item.Properties().stacksTo(1), true);
	public static CreativeModeTab ITEM_GROUP;

	private MRPortalItems() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.ITEM, id("portal_staff"), PORTAL_STAFF);
		Registry.register(BuiltInRegistries.ITEM, id("infinite_portal_staff"), INFINITE_PORTAL_STAFF);
		ITEM_GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("main"), FabricItemGroup.builder()
			.title(Component.translatable("itemGroup.mr_portal.main"))
			.icon(() -> new ItemStack(PORTAL_STAFF))
			.displayItems((parameters, output) -> {
				output.accept(PORTAL_STAFF);
				output.accept(INFINITE_PORTAL_STAFF);
			})
			.build());
	}

	public static boolean isPortalStaff(ItemStack stack) {
		return stack.is(PORTAL_STAFF) || stack.is(INFINITE_PORTAL_STAFF);
	}

	public static boolean isInfinite(ItemStack stack) {
		return stack.is(INFINITE_PORTAL_STAFF);
	}

	private static ResourceLocation id(String path) {
		return new ResourceLocation(MRPortal.MOD_ID, path);
	}
}