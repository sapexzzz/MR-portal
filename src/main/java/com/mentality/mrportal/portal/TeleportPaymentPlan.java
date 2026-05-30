package com.mentality.mrportal.portal;

import com.mentality.mrportal.api.MRPortalTeleportActivator;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Internal base-owned payment and cooldown plan.
 *
 * <p>The contained {@link ItemStack} references are mutable live stacks and must
 * remain internal. This record does not consume items or apply cooldowns.</p>
 */
record TeleportPaymentPlan(
	MRPortalTeleportActivator activator,
	boolean infinite,
	boolean creativeBypass,
	int pearlCost,
	int cooldownTicks,
	ItemStack staffStack,
	ItemStack scrollStack
) {
	TeleportPaymentPlan {
		Objects.requireNonNull(activator, "activator");
		Objects.requireNonNull(staffStack, "staffStack");
		Objects.requireNonNull(scrollStack, "scrollStack");
		if (pearlCost < 0) {
			throw new IllegalArgumentException("pearlCost must be non-negative");
		}
		if (cooldownTicks < 0) {
			throw new IllegalArgumentException("cooldownTicks must be non-negative");
		}
	}
}
