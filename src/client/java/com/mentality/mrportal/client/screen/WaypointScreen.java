package com.mentality.mrportal.client.screen;

import com.mentality.mrportal.util.DimensionUtil;
import com.mentality.mrportal.waypoint.WaypointData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
	private static final int PANEL_WIDTH = 344;
	private static final int PANEL_HEIGHT = 228;
	private static final int ROW_HEIGHT = 28;
	private static final int LIST_HEIGHT = 124;

	private final boolean creativeView;
	private final List<ResourceKey<Level>> dimensions;
	private final List<WaypointData> waypoints;
	private ResourceKey<Level> selectedDimension;
	private UUID selectedWaypointId;
	private int scrollOffset;
	private boolean confirmDelete;
	private Button teleportButton;
	private Button deleteButton;
	private Button yesButton;

	public WaypointScreen(boolean creativeView, ResourceKey<Level> currentDimension, List<ResourceKey<Level>> dimensions, List<WaypointData> waypoints) {
		super(Component.translatable("screen.mr_portal.title"));
		this.creativeView = creativeView;
		this.dimensions = dimensions.stream().sorted(Comparator.comparing(key -> key.location().toString())).toList();
		this.waypoints = waypoints.stream().sorted(Comparator.comparing(WaypointData::name, String.CASE_INSENSITIVE_ORDER)).toList();
		this.selectedDimension = currentDimension;
	}

	@Override
	protected void init() {
		this.rebuildWidgets();
	}

	protected void rebuildWidgets() {
		this.clearWidgets();
		int left = this.left();
		int top = this.top();
		int bottom = top + PANEL_HEIGHT - 28;

		if (this.creativeView) {
			int tabX = left + 12;
			int tabY = top + 14;
			for (ResourceKey<Level> dimension : this.dimensions) {
				this.addRenderableWidget(Button.builder(Component.literal(shortDimensionName(dimension)), button -> {
					this.selectedDimension = dimension;
					this.selectedWaypointId = null;
					this.scrollOffset = 0;
					this.rebuildWidgets();
				}).bounds(tabX, tabY, 72, 20).build());
				tabX += 76;
			}
		}

		this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.add"), button -> MRPortalClientNetworking.sendAddWaypoint())
			.bounds(left + 12, bottom, 88, 20)
			.build());

		this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.delete"), button -> {
			if (this.selectedWaypointId != null) {
				this.confirmDelete = true;
				this.rebuildWidgets();
			}
		})
			.bounds(left + 106, bottom, 88, 20)
			.build());

		this.teleportButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.teleport"), button -> {
			if (this.selectedWaypointId != null) {
				MRPortalClientNetworking.sendTeleportRequest(this.selectedWaypointId);
				this.onClose();
			}
		})
			.bounds(left + 200, bottom, 88, 20)
			.build());

		this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.close"), button -> this.onClose())
			.bounds(left + 294, bottom, 38, 20)
			.build());

		if (this.confirmDelete) {
			this.yesButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.yes"), button -> {
				if (this.selectedWaypointId != null) {
					MRPortalClientNetworking.sendDeleteWaypoint(this.selectedWaypointId);
				}
				this.confirmDelete = false;
			})
				.bounds(left + 104, top + 96, 64, 20)
				.build());

			this.addRenderableWidget(Button.builder(Component.translatable("screen.mr_portal.no"), button -> {
				this.confirmDelete = false;
				this.rebuildWidgets();
			})
				.bounds(left + 176, top + 96, 64, 20)
				.build());
		}

		this.updateButtonState();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.confirmDelete && button == 0) {
			int listLeft = this.left() + 12;
			int listTop = this.top() + 52;
			int listWidth = PANEL_WIDTH - 24;
			List<WaypointData> filtered = this.filteredWaypoints();
			int visibleRows = LIST_HEIGHT / ROW_HEIGHT;
			for (int index = 0; index < visibleRows; index++) {
				int dataIndex = index + this.scrollOffset;
				if (dataIndex >= filtered.size()) {
					break;
				}
				int rowTop = listTop + index * ROW_HEIGHT;
				if (mouseX >= listLeft && mouseX <= listLeft + listWidth && mouseY >= rowTop && mouseY <= rowTop + ROW_HEIGHT - 2) {
					this.selectedWaypointId = filtered.get(dataIndex).id();
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
		graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xC0101018);
		graphics.fill(left + 1, top + 1, left + PANEL_WIDTH - 1, top + PANEL_HEIGHT - 1, 0xD0202434);

		graphics.drawCenteredString(this.font, this.title, left + PANEL_WIDTH / 2, top + 8, 0xF4EBD0);
		graphics.drawString(this.font, Component.translatable(this.creativeView ? "screen.mr_portal.mode_creative" : "screen.mr_portal.mode_survival"), left + 12, top + 32, 0xC5D2FF, false);

		int listLeft = left + 12;
		int listTop = top + 52;
		int listWidth = PANEL_WIDTH - 24;
		graphics.fill(listLeft, listTop, listLeft + listWidth, listTop + LIST_HEIGHT, 0x7010151F);

		List<WaypointData> filtered = this.filteredWaypoints();
		int visibleRows = LIST_HEIGHT / ROW_HEIGHT;
		if (filtered.isEmpty()) {
			graphics.drawCenteredString(this.font, Component.translatable("screen.mr_portal.empty"), left + PANEL_WIDTH / 2, listTop + 54, 0xAAB4D4);
		} else {
			for (int index = 0; index < visibleRows; index++) {
				int dataIndex = index + this.scrollOffset;
				if (dataIndex >= filtered.size()) {
					break;
				}
				WaypointData waypoint = filtered.get(dataIndex);
				int rowTop = listTop + index * ROW_HEIGHT;
				boolean selected = waypoint.id().equals(this.selectedWaypointId);
				graphics.fill(listLeft + 2, rowTop + 2, listLeft + listWidth - 2, rowTop + ROW_HEIGHT - 2, selected ? 0xB03A4666 : 0x70242A38);
				graphics.drawString(this.font, Component.literal(waypoint.name()), listLeft + 10, rowTop + 7, 0xF6F0D2, false);

				String secondary = String.format(Locale.ROOT, "%.1f %.1f %.1f  |  %s", waypoint.x(), waypoint.y(), waypoint.z(), DimensionUtil.prettifyDimension(waypoint.dimension()));
				graphics.pose().pushPose();
				graphics.pose().scale(0.75F, 0.75F, 1.0F);
				graphics.drawString(this.font, secondary, (int) ((listLeft + 10) / 0.75F), (int) ((rowTop + 20) / 0.75F), 0xAAB4D4, false);
				graphics.pose().popPose();
			}
		}

		if (this.confirmDelete) {
			graphics.fill(left + 72, top + 72, left + PANEL_WIDTH - 72, top + 132, 0xE020202A);
			graphics.drawCenteredString(this.font, Component.translatable("screen.mr_portal.confirm_delete"), left + PANEL_WIDTH / 2, top + 80, 0xFFFFFF);
		}

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void updateButtonState() {
		boolean hasSelection = this.selectedWaypointId != null;
		if (this.teleportButton != null) {
			this.teleportButton.active = hasSelection;
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

	private int left() {
		return (this.width - PANEL_WIDTH) / 2;
	}

	private int top() {
		return (this.height - PANEL_HEIGHT) / 2;
	}

	private static String shortDimensionName(ResourceKey<Level> dimension) {
		String pretty = DimensionUtil.prettifyDimension(dimension);
		return pretty.length() > 10 ? pretty.substring(0, 10) : pretty;
	}
}