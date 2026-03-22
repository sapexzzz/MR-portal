package com.mentality.mrportal.client.screen;

import com.mentality.mrportal.client.render.ClientPortalEffectManager;
import com.mentality.mrportal.util.DimensionUtil;
import com.mentality.mrportal.waypoint.WaypointData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class WaypointScreen extends Screen {
	private static final int PANEL_WIDTH = 448;
	private static final int PANEL_HEIGHT = 272;
	private static final int LIST_WIDTH = 248;
	private static final int LIST_HEIGHT = 148;
	private static final int ROW_HEIGHT = 30;

	private final boolean creativeView;
	private final List<ResourceKey<Level>> dimensions;
	private final List<WaypointData> waypoints;
	private final int availablePearls;
	private final int requiredPearls;
	private final double previewX;
	private final double previewY;
	private final double previewZ;
	private final float previewYaw;
	private final float previewScale;
	private ResourceKey<Level> selectedDimension;
	private UUID selectedWaypointId;
	private int scrollOffset;
	private boolean confirmDelete;
	private Component statusMessage;
	private int statusColor = 0x9FB0D2;
	private String pendingWaypointName = "";
	private Button teleportButton;
	private Button deleteButton;
	private Button yesButton;
	private EditBox nameField;

	public WaypointScreen(boolean creativeView, ResourceKey<Level> currentDimension, List<ResourceKey<Level>> dimensions, List<WaypointData> waypoints, int availablePearls, int requiredPearls, double previewX, double previewY, double previewZ, float previewYaw, float previewScale) {
		super(Component.translatable("screen.mr_portal.title"));
		this.creativeView = creativeView;
		this.dimensions = dimensions.stream().sorted(Comparator.comparing(key -> key.location().toString())).toList();
		this.waypoints = waypoints.stream().sorted(Comparator.comparing(WaypointData::name, String.CASE_INSENSITIVE_ORDER)).toList();
		this.availablePearls = availablePearls;
		this.requiredPearls = requiredPearls;
		this.previewX = previewX;
		this.previewY = previewY;
		this.previewZ = previewZ;
		this.previewYaw = previewYaw;
		this.previewScale = previewScale;
		this.selectedDimension = currentDimension;
		this.statusMessage = Component.translatable(creativeView ? "screen.mr_portal.resource_free" : "screen.mr_portal.resource_cost", requiredPearls, availablePearls);
		this.statusColor = creativeView || availablePearls >= requiredPearls ? 0x99E2BE : 0xF08585;
	}

	@Override
	protected void init() {
		if (this.minecraft != null && this.minecraft.level != null) {
			ClientPortalEffectManager.showPreviewSpark(this.minecraft.level.dimension(), this.previewX, this.previewY, this.previewZ, this.previewYaw, this.previewScale);
		}
		this.rebuildWidgets();
	}

	@Override
	public void onClose() {
		ClientPortalEffectManager.hidePreviewSpark();
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected void rebuildWidgets() {
		if (this.nameField != null) {
			this.pendingWaypointName = this.nameField.getValue();
		}
		this.clearWidgets();
		int left = this.left();
		int top = this.top();
		int bottom = top + PANEL_HEIGHT - 28;
		int sideLeft = left + 286;

		if (this.creativeView) {
			int tabX = left + 18;
			for (ResourceKey<Level> dimension : this.dimensions) {
				this.addRenderableWidget(Button.builder(Component.literal(shortDimensionName(dimension)), button -> {
					this.selectedDimension = dimension;
					this.selectedWaypointId = null;
					this.scrollOffset = 0;
					this.rebuildWidgets();
				}).bounds(tabX, top + 48, 72, 20).build());
				tabX += 76;
			}
		}

		this.nameField = this.addRenderableWidget(new EditBox(this.font, sideLeft + 10, top + 190, PANEL_WIDTH - (sideLeft - left) - 38, 18, Component.translatable("screen.mr_portal.name_hint")));
		this.nameField.setMaxLength(32);
		this.nameField.setValue(this.pendingWaypointName);
		this.nameField.setHint(Component.translatable("screen.mr_portal.name_hint"));

		this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.add"), button -> MRPortalClientNetworking.sendAddWaypoint(this.nameField.getValue()))
			.bounds(left + 18, bottom, 96, 20)
			.build());

		this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.delete"), button -> {
			if (this.selectedWaypointId != null) {
				this.confirmDelete = true;
				this.rebuildWidgets();
			}
		})
			.bounds(left + 120, bottom, 96, 20)
			.build());

		this.teleportButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.teleport"), button -> this.tryTeleport())
			.bounds(left + 222, bottom, 96, 20)
			.build());

		this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.close"), button -> this.onClose())
			.bounds(left + PANEL_WIDTH - 58, bottom, 40, 20)
			.build());

		if (this.confirmDelete) {
			this.yesButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.yes"), button -> {
				if (this.selectedWaypointId != null) {
					MRPortalClientNetworking.sendDeleteWaypoint(this.selectedWaypointId);
				}
				this.confirmDelete = false;
			})
				.bounds(left + 156, top + 108, 72, 20)
				.build());

			this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.no"), button -> {
				this.confirmDelete = false;
				this.rebuildWidgets();
			})
				.bounds(left + 236, top + 108, 72, 20)
				.build());
		}

		this.updateButtonState();
	}

	private void tryTeleport() {
		if (this.selectedWaypointId == null) {
			this.statusMessage = Component.translatable("screen.mr_portal.select_waypoint");
			this.statusColor = 0xF6D47A;
			return;
		}
		if (!this.creativeView && this.availablePearls < this.requiredPearls) {
			this.statusMessage = Component.translatable("screen.mr_portal.not_enough_resources", this.requiredPearls, this.availablePearls);
			this.statusColor = 0xF08585;
			return;
		}

		MRPortalClientNetworking.sendTeleportRequest(this.selectedWaypointId);
		this.onClose();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.confirmDelete && button == 0) {
			int listLeft = this.left() + 18;
			int listTop = this.top() + 74;
			List<WaypointData> filtered = this.filteredWaypoints();
			int visibleRows = LIST_HEIGHT / ROW_HEIGHT;
			for (int index = 0; index < visibleRows; index++) {
				int dataIndex = index + this.scrollOffset;
				if (dataIndex >= filtered.size()) {
					break;
				}
				int rowTop = listTop + index * ROW_HEIGHT;
				if (mouseX >= listLeft && mouseX <= listLeft + LIST_WIDTH && mouseY >= rowTop && mouseY <= rowTop + ROW_HEIGHT - 2) {
					this.selectedWaypointId = filtered.get(dataIndex).id();
					this.statusMessage = Component.translatable("screen.mr_portal.ready");
					this.statusColor = 0x99E2BE;
					this.updateButtonState();
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		int maxOffset = Math.max(0, this.filteredWaypoints().size() - (LIST_HEIGHT / ROW_HEIGHT));
		this.scrollOffset = Mth.clamp(this.scrollOffset - (int) Math.signum(delta), 0, maxOffset);
		return true;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		int left = this.left();
		int top = this.top();
		int listLeft = left + 18;
		int listTop = top + 86;
		int sideLeft = left + 286;
		int sideWidth = PANEL_WIDTH - (sideLeft - left) - 18;

		graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xD010131E);
		graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + PANEL_HEIGHT - 2, 0xD51D2635);
		graphics.fill(left + 18, top + 18, left + PANEL_WIDTH - 18, top + 62, 0x8036495F);
		graphics.fill(listLeft, listTop, listLeft + LIST_WIDTH, listTop + LIST_HEIGHT, 0x70212936);
		graphics.fill(sideLeft, listTop, left + PANEL_WIDTH - 18, listTop + LIST_HEIGHT, 0x70313A4B);
		graphics.fill(sideLeft, top + 178, left + PANEL_WIDTH - 18, top + 214, 0x70313A4B);

		graphics.drawString(this.font, this.title, left + 22, top + 22, 0xFFF4E2, false);
		graphics.drawString(this.font, Component.translatable(this.creativeView ? "screen.mr_portal.mode_creative" : "screen.mr_portal.mode_survival"), left + 22, top + 38, 0xC9D6F7, false);
		this.drawWrapped(graphics, Component.translatable("screen.mr_portal.preview_hint"), sideLeft, top + 22, sideWidth, 0xD4E7FF);

		List<WaypointData> filtered = this.filteredWaypoints();
		int visibleRows = LIST_HEIGHT / ROW_HEIGHT;
		if (filtered.isEmpty()) {
			graphics.drawCenteredString(this.font, Component.translatable("screen.mr_portal.empty"), listLeft + LIST_WIDTH / 2, listTop + 64, 0xAAB4D4);
		} else {
			for (int index = 0; index < visibleRows; index++) {
				int dataIndex = index + this.scrollOffset;
				if (dataIndex >= filtered.size()) {
					break;
				}
				WaypointData waypoint = filtered.get(dataIndex);
				int rowTop = listTop + index * ROW_HEIGHT;
				boolean selected = waypoint.id().equals(this.selectedWaypointId);
				graphics.fill(listLeft + 4, rowTop + 4, listLeft + LIST_WIDTH - 4, rowTop + ROW_HEIGHT - 2, selected ? 0xC04B5E7D : 0x70262F3F);
				graphics.drawString(this.font, Component.literal(waypoint.name()), listLeft + 12, rowTop + 8, 0xFFF0D6, false);
				String secondary = String.format(Locale.ROOT, "%.1f %.1f %.1f | %s", waypoint.x(), waypoint.y(), waypoint.z(), shortDimensionName(waypoint.dimension()));
				graphics.pose().pushPose();
				graphics.pose().scale(0.75F, 0.75F, 1.0F);
				graphics.drawString(this.font, secondary, (int) ((listLeft + 12) / 0.75F), (int) ((rowTop + 24) / 0.75F), 0xAEB9D8, false);
				graphics.pose().popPose();
			}
		}

		WaypointData selectedWaypoint = this.selectedWaypoint();
		graphics.drawString(this.font, Component.translatable("screen.mr_portal.selected_title"), sideLeft + 10, listTop + 10, 0xFFF4E2, false);
		if (selectedWaypoint == null) {
			this.drawWrapped(graphics, Component.translatable("screen.mr_portal.select_waypoint"), sideLeft + 10, listTop + 28, sideWidth - 14, 0xAAB4D4);
		} else {
			graphics.drawString(this.font, Component.literal(selectedWaypoint.name()), sideLeft + 10, listTop + 28, 0xFFE7BE, false);
			graphics.drawString(this.font, Component.literal(DimensionUtil.prettifyDimension(selectedWaypoint.dimension())), sideLeft + 10, listTop + 44, 0xC9D6F7, false);
			graphics.drawString(this.font, Component.literal(String.format(Locale.ROOT, "X %.1f", selectedWaypoint.x())), sideLeft + 10, listTop + 64, 0xDDE5F9, false);
			graphics.drawString(this.font, Component.literal(String.format(Locale.ROOT, "Y %.1f", selectedWaypoint.y())), sideLeft + 10, listTop + 80, 0xDDE5F9, false);
			graphics.drawString(this.font, Component.literal(String.format(Locale.ROOT, "Z %.1f", selectedWaypoint.z())), sideLeft + 10, listTop + 96, 0xDDE5F9, false);
		}

		graphics.drawString(this.font, Component.translatable("screen.mr_portal.resources_title"), sideLeft + 10, listTop + 122, 0xFFF4E2, false);
		Component resourcesLine = this.creativeView ? Component.translatable("screen.mr_portal.resource_free") : Component.translatable("screen.mr_portal.resource_cost", this.requiredPearls, this.availablePearls);
		this.drawWrapped(graphics, resourcesLine, sideLeft + 10, listTop + 138, sideWidth - 14, this.creativeView || this.availablePearls >= this.requiredPearls ? 0x99E2BE : 0xF08585);
		graphics.drawString(this.font, Component.translatable("screen.mr_portal.name_title"), sideLeft + 10, top + 182, 0xFFF4E2, false);

		graphics.fill(left + 18, top + PANEL_HEIGHT - 58, left + PANEL_WIDTH - 18, top + PANEL_HEIGHT - 36, 0x70212936);
		this.drawWrapped(graphics, this.statusMessage, left + 26, top + PANEL_HEIGHT - 54, PANEL_WIDTH - 92, this.statusColor);

		if (this.confirmDelete) {
			graphics.fill(left + 118, top + 88, left + PANEL_WIDTH - 118, top + 138, 0xF0212631);
			graphics.drawCenteredString(this.font, Component.translatable("screen.mr_portal.confirm_delete"), left + PANEL_WIDTH / 2, top + 96, 0xFFFFFF);
		}

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void updateButtonState() {
		boolean hasSelection = this.selectedWaypointId != null;
		boolean hasResources = this.creativeView || this.availablePearls >= this.requiredPearls;
		if (this.teleportButton != null) {
			this.teleportButton.active = hasSelection && hasResources;
		}
		if (this.deleteButton != null) {
			this.deleteButton.active = hasSelection;
		}
		if (this.yesButton != null) {
			this.yesButton.active = hasSelection;
		}
	}

	private List<WaypointData> filteredWaypoints() {
		List<WaypointData> filtered = new ArrayList<>();
		for (WaypointData waypoint : this.waypoints) {
			if (waypoint.dimension().equals(this.selectedDimension)) {
				filtered.add(waypoint);
			}
		}
		if (this.selectedWaypointId != null && filtered.stream().noneMatch(waypoint -> waypoint.id().equals(this.selectedWaypointId))) {
			this.selectedWaypointId = null;
		}
		return filtered;
	}

	private WaypointData selectedWaypoint() {
		if (this.selectedWaypointId == null) {
			return null;
		}
		for (WaypointData waypoint : this.waypoints) {
			if (waypoint.id().equals(this.selectedWaypointId)) {
				return waypoint;
			}
		}
		return null;
	}

	private int left() {
		return (this.width - PANEL_WIDTH) / 2;
	}

	private int top() {
		return (this.height - PANEL_HEIGHT) / 2;
	}

	private static String shortDimensionName(ResourceKey<Level> dimension) {
		String pretty = DimensionUtil.prettifyDimension(dimension);
		return pretty.length() > 11 ? pretty.substring(0, 11) : pretty;
	}

	private void drawWrapped(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
		int lineY = y;
		for (var line : this.font.split(text, maxWidth)) {
			graphics.drawString(this.font, line, x, lineY, color, false);
			lineY += 10;
		}
	}
}