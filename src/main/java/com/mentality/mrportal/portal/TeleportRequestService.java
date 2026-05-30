package com.mentality.mrportal.portal;

import com.mentality.mrportal.api.MRPortalApi;
import com.mentality.mrportal.api.MRPortalTeleportActivator;
import com.mentality.mrportal.api.MRPortalTeleportContext;
import com.mentality.mrportal.api.MRPortalTeleportHandler;
import com.mentality.mrportal.config.MRPortalConfig;
import com.mentality.mrportal.config.MRPortalConfigManager;
import com.mentality.mrportal.item.MRPortalItems;
import com.mentality.mrportal.network.MRPortalNetworking;
import com.mentality.mrportal.util.ModTranslation;
import com.mentality.mrportal.waypoint.ServerWaypointStore;
import com.mentality.mrportal.waypoint.WaypointData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

/**
 * Internal server-side orchestrator for future teleport requests.
 *
 * <p>GUI teleport requests are routed through this service. Quick favorite
 * routing remains on the existing direct flow until a later phase.</p>
 */
public final class TeleportRequestService {
	private TeleportRequestService() {
	}

	public static TeleportRequestResult handleGuiTeleportRequest(
		MinecraftServer server,
		ServerPlayer player,
		UUID waypointId,
		boolean useScroll,
		boolean creativeView
	) {
		Objects.requireNonNull(server, "server");
		Objects.requireNonNull(waypointId, "waypointId");
		if (player == null) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.INVALID_PLAYER);
		}

		WaypointData waypoint = ServerWaypointStore.get(server).getWaypoint(player.getUUID(), waypointId).orElse(null);
		if (waypoint == null) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_WAYPOINT);
		}
		boolean sameDimension = waypoint.dimension().equals(player.serverLevel().dimension());

		PendingTeleportManager manager = PendingTeleportManager.get(server);
		if (manager.hasActiveSession(player)) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.portal_active"), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.ACTIVE_SESSION, true);
		}

		boolean useScrollPath = useScroll && !creativeView;
		ItemStack staffStack = useScrollPath ? ItemStack.EMPTY : PendingTeleportManager.findPortalStaff(player);
		ItemStack scrollStack = useScrollPath ? PendingTeleportManager.findTeleportScroll(player) : ItemStack.EMPTY;
		MRPortalTeleportActivator activator = classifyActivator(player, useScrollPath, staffStack);
		boolean creativeBypass = activator == MRPortalTeleportActivator.CREATIVE;
		boolean infinite = activator == MRPortalTeleportActivator.INFINITE_STAFF;

		if (!creativeBypass && !infinite && !sameDimension) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.same_dimension_only"), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.SAME_DIMENSION_ONLY, true);
		}

		TeleportPaymentPlan paymentPlan = buildPaymentPlan(activator, infinite, creativeBypass, useScrollPath, staffStack, scrollStack);
		if (useScrollPath) {
			if (paymentPlan.scrollStack().isEmpty()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.staff_required"), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_TELEPORT_ITEM, true);
			}
		} else if (!paymentPlan.infinite() && !paymentPlan.creativeBypass()) {
			if (paymentPlan.staffStack().isEmpty()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.staff_required"), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_TELEPORT_ITEM, true);
			}
			if (player.getCooldowns().isOnCooldown(paymentPlan.staffStack().getItem())) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.cooldown_active"), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.COOLDOWN_ACTIVE, true);
			}
			if (PendingTeleportManager.countEnderPearls(player) < paymentPlan.pearlCost()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.not_enough_pearls", paymentPlan.pearlCost()), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.NOT_ENOUGH_PEARLS, true);
			}
		}

		MRPortalTeleportContext context = buildContext(new TeleportActivationInfo(
			player,
			waypoint,
			activator,
			creativeBypass || infinite,
			useScrollPath,
			false,
			infinite,
			PendingTeleportManager.calculateSourcePortalCenter(player, MRPortalConfigManager.get()),
			player.getYRot(),
			server.getTickCount()
		), paymentPlan);

		MRPortalTeleportHandler handler = MRPortalApi.getActiveTeleportHandler();
		if (handler == null) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.HANDLER_FAILED);
		}
		boolean accepted = handler.canStartTeleport(context);
		if (!accepted) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.HANDLER_REJECTED);
		}

		TeleportRequestResult paymentResult = commitPayment(player, paymentPlan);
		if (!paymentResult.successful()) {
			return paymentResult;
		}

		boolean started = handler.startTeleport(context);
		if (!started) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.HANDLER_FAILED);
		}
		return TeleportRequestResult.success();
	}

	public static TeleportRequestResult queueQuickFavoriteRequest(MinecraftServer server, ServerPlayer player) {
		Objects.requireNonNull(server, "server");
		if (player == null) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.INVALID_PLAYER);
		}

		WaypointData favorite = ServerWaypointStore.get(server).getFavoriteWaypoint(player.getUUID()).orElse(null);
		if (favorite == null) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.favorite_missing"), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_WAYPOINT, true);
		}

		PendingTeleportManager manager = PendingTeleportManager.get(server);
		if (manager.hasActiveSession(player) || manager.hasPendingFavoriteActivation(player)) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.portal_active"), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.ACTIVE_SESSION, true);
		}

		boolean creativeView = player.getAbilities().instabuild;
		ItemStack activatorStack = PendingTeleportManager.findPreferredTeleportItem(player);
		boolean hasInfinite = creativeView || MRPortalItems.isInfinite(activatorStack);
		boolean hasRegularStaff = activatorStack.is(MRPortalItems.PORTAL_STAFF);
		boolean hasScroll = MRPortalItems.isTeleportScroll(activatorStack);

		if (!hasInfinite && !favorite.dimension().equals(player.serverLevel().dimension())) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.same_dimension_only"), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.SAME_DIMENSION_ONLY, true);
		}

		if (!hasInfinite && !hasRegularStaff && !hasScroll) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.favorite_item_required"), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_TELEPORT_ITEM, true);
		}

		boolean queued = manager.queueFavoriteTeleport(
			player,
			favorite,
			hasInfinite,
			hasScroll && !hasInfinite && !hasRegularStaff,
			MRPortalNetworking.usesDefaultPreviewSpark()
		);
		return queued ? TeleportRequestResult.success() : TeleportRequestResult.failed(TeleportRequestFailureReason.PENDING_FAVORITE, true);
	}

	public static TeleportRequestResult activateQueuedQuickFavorite(
		MinecraftServer server,
		ServerPlayer player,
		WaypointData waypoint,
		boolean queuedInfinite,
		boolean useScroll,
		Vec3 sourcePos,
		float sourceYaw
	) {
		Objects.requireNonNull(server, "server");
		Objects.requireNonNull(waypoint, "waypoint");
		Objects.requireNonNull(sourcePos, "sourcePos");
		if (player == null || !player.isAlive()) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.INVALID_PLAYER);
		}

		PendingTeleportManager manager = PendingTeleportManager.get(server);
		if (manager.hasActiveSession(player)) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.portal_active"), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.ACTIVE_SESSION, true);
		}

		boolean creativeBypass = player.getAbilities().instabuild;
		boolean infinite = queuedInfinite || creativeBypass;
		if (!infinite && !waypoint.dimension().equals(player.serverLevel().dimension())) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.same_dimension_only"), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.SAME_DIMENSION_ONLY, true);
		}

		ItemStack staffStack = ItemStack.EMPTY;
		ItemStack scrollStack = ItemStack.EMPTY;
		MRPortalTeleportActivator activator;
		if (useScroll) {
			activator = MRPortalTeleportActivator.TELEPORT_SCROLL;
			scrollStack = PendingTeleportManager.findTeleportScroll(player);
			if (scrollStack.isEmpty()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.favorite_item_required"), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_TELEPORT_ITEM, true);
			}
		} else if (creativeBypass) {
			activator = MRPortalTeleportActivator.CREATIVE;
		} else if (queuedInfinite) {
			activator = MRPortalTeleportActivator.INFINITE_STAFF;
			staffStack = PendingTeleportManager.findPreferredTeleportItem(player);
			if (!MRPortalItems.isInfinite(staffStack)) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.favorite_item_required"), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_TELEPORT_ITEM, true);
			}
		} else {
			activator = MRPortalTeleportActivator.REGULAR_STAFF;
			staffStack = PendingTeleportManager.findPortalStaff(player);
			if (staffStack.isEmpty()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.favorite_item_required"), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_TELEPORT_ITEM, true);
			}
			if (player.getCooldowns().isOnCooldown(staffStack.getItem())) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.cooldown_active"), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.COOLDOWN_ACTIVE, true);
			}
		}

		TeleportPaymentPlan paymentPlan = buildPaymentPlan(activator, activator == MRPortalTeleportActivator.INFINITE_STAFF, creativeBypass, useScroll, staffStack, scrollStack);
		if (activator == MRPortalTeleportActivator.REGULAR_STAFF && PendingTeleportManager.countEnderPearls(player) < paymentPlan.pearlCost()) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.not_enough_pearls", paymentPlan.pearlCost()), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.NOT_ENOUGH_PEARLS, true);
		}

		MRPortalTeleportContext context = buildContext(new TeleportActivationInfo(
			player,
			waypoint,
			activator,
			creativeBypass || activator == MRPortalTeleportActivator.INFINITE_STAFF,
			useScroll,
			true,
			activator == MRPortalTeleportActivator.INFINITE_STAFF,
			sourcePos,
			sourceYaw,
			server.getTickCount()
		), paymentPlan);

		MRPortalTeleportHandler handler = MRPortalApi.getActiveTeleportHandler();
		if (handler == null) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.HANDLER_FAILED);
		}
		if (!handler.canStartTeleport(context)) {
			return TeleportRequestResult.failed(TeleportRequestFailureReason.HANDLER_REJECTED);
		}

		TeleportRequestResult paymentResult = commitPayment(player, paymentPlan);
		if (!paymentResult.successful()) {
			return paymentResult;
		}
		return handler.startTeleport(context) ? TeleportRequestResult.success() : TeleportRequestResult.failed(TeleportRequestFailureReason.HANDLER_FAILED);
	}

	static MRPortalTeleportContext buildContext(TeleportActivationInfo activationInfo, TeleportPaymentPlan paymentPlan) {
		Objects.requireNonNull(activationInfo, "activationInfo");
		Objects.requireNonNull(paymentPlan, "paymentPlan");
		return new MRPortalTeleportContext(
			activationInfo.player().server,
			activationInfo.player(),
			new WaypointDataView(activationInfo.waypoint()),
			activationInfo.creativeView(),
			activationInfo.useScroll(),
			activationInfo.quickTeleport(),
			activationInfo.activator(),
			paymentPlan.pearlCost(),
			paymentPlan.cooldownTicks(),
			activationInfo.sourcePos(),
			activationInfo.sourceYaw()
		);
	}

	private static MRPortalTeleportActivator classifyActivator(ServerPlayer player, boolean useScrollPath, ItemStack staffStack) {
		if (player.getAbilities().instabuild) {
			return MRPortalTeleportActivator.CREATIVE;
		}
		if (useScrollPath) {
			return MRPortalTeleportActivator.TELEPORT_SCROLL;
		}
		return MRPortalItems.isInfinite(staffStack) ? MRPortalTeleportActivator.INFINITE_STAFF : MRPortalTeleportActivator.REGULAR_STAFF;
	}

	private static TeleportPaymentPlan buildPaymentPlan(
		MRPortalTeleportActivator activator,
		boolean infinite,
		boolean creativeBypass,
		boolean useScrollPath,
		ItemStack staffStack,
		ItemStack scrollStack
	) {
		if (useScrollPath) {
			return new TeleportPaymentPlan(activator, false, false, 0, 0, ItemStack.EMPTY, scrollStack);
		}
		if (infinite || creativeBypass) {
			return new TeleportPaymentPlan(activator, infinite, creativeBypass, 0, 0, staffStack, ItemStack.EMPTY);
		}
		MRPortalConfig config = MRPortalConfigManager.get();
		return new TeleportPaymentPlan(
			activator,
			false,
			false,
			PendingTeleportManager.getRequiredPearls(false),
			config.cooldownSeconds * 20,
			staffStack,
			ItemStack.EMPTY
		);
	}

	private static TeleportRequestResult commitPayment(ServerPlayer player, TeleportPaymentPlan paymentPlan) {
		if (paymentPlan.activator() == MRPortalTeleportActivator.TELEPORT_SCROLL) {
			if (paymentPlan.scrollStack().isEmpty()) {
				player.displayClientMessage(ModTranslation.get("message.mr_portal.staff_required"), true);
				return TeleportRequestResult.failed(TeleportRequestFailureReason.MISSING_TELEPORT_ITEM, true);
			}
			paymentPlan.scrollStack().shrink(1);
			syncInventory(player);
			return TeleportRequestResult.success();
		}
		if (paymentPlan.infinite() || paymentPlan.creativeBypass()) {
			return TeleportRequestResult.success();
		}
		if (!PendingTeleportManager.consumeEnderPearls(player, paymentPlan.pearlCost())) {
			player.displayClientMessage(ModTranslation.get("message.mr_portal.not_enough_pearls", paymentPlan.pearlCost()), true);
			return TeleportRequestResult.failed(TeleportRequestFailureReason.NOT_ENOUGH_PEARLS, true);
		}
		player.getCooldowns().addCooldown(paymentPlan.staffStack().getItem(), paymentPlan.cooldownTicks());
		syncInventory(player);
		return TeleportRequestResult.success();
	}

	private static void syncInventory(ServerPlayer player) {
		player.getInventory().setChanged();
		player.inventoryMenu.broadcastChanges();
		player.containerMenu.broadcastChanges();
	}
}
