package com.mentality.mrportal.client.screen;

import com.mentality.mrportal.client.render.ClientPortalEffectManager;
import com.mentality.mrportal.util.DimensionUtil;
import com.mentality.mrportal.util.ModTranslation;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class WaypointScreen extends Screen {
	private static final int PANEL_WIDTH = 420;
	private static final int PANEL_HEIGHT = 260;
	private static final int LIST_WIDTH = 230;
	private static final int LIST_HEIGHT = 170;
	private static final int ROW_HEIGHT = 24;
	private static final int GROUP_HEADER_HEIGHT = 18;

	private final boolean creativeView;
	private final ResourceKey<Level> currentDimension;
	private final List<ResourceKey<Level>> dimensions;
	private final List<WaypointData> waypoints;
	private final int availablePearls;
	private final int requiredPearls;
	private final double previewX;
	private final double previewY;
	private final double previewZ;
	private final float previewYaw;
	private final float previewScale;
	private UUID selectedWaypointId;
	private int scrollOffset;
	private Component statusMessage;
	private int statusColor = 0x9FB0D2;

	private Button teleportButton;
	private Button deleteButton;
	private Button addButton;
	private Button renameButton;

	private enum Dialog { NONE, CONFIRM_DELETE, NAME_INPUT, RENAME_INPUT }
	private Dialog activeDialog = Dialog.NONE;
	private EditBox dialogNameField;
	private boolean useScroll;

	public WaypointScreen(boolean creativeView, ResourceKey<Level> currentDimension, List<ResourceKey<Level>> dimensions, List<WaypointData> waypoints, int availablePearls, int requiredPearls, double previewX, double previewY, double previewZ, float previewYaw, float previewScale) {
		super(ModTranslation.get("screen.mr_portal.title"));
		this.creativeView = creativeView;
		this.currentDimension = currentDimension;
		this.dimensions = dimensions.stream().sorted(Comparator.comparing(key -> key.location().toString())).toList();
		this.waypoints = waypoints.stream()
			.sorted(Comparator.comparing(WaypointData::favorite).reversed().thenComparing(WaypointData::name, String.CASE_INSENSITIVE_ORDER))
			.toList();
		this.availablePearls = availablePearls;
		this.requiredPearls = requiredPearls;
		this.previewX = previewX;
		this.previewY = previewY;
		this.previewZ = previewZ;
		this.previewYaw = previewYaw;
		this.previewScale = previewScale;
		this.statusMessage = ModTranslation.get(creativeView ? "screen.mr_portal.resource_free" : "screen.mr_portal.resource_cost", requiredPearls, availablePearls);
		this.statusColor = creativeView || availablePearls >= requiredPearls ? 0x99E2BE : 0xF08585;
		this.useScroll = false;
	}

	public void setUseScroll(boolean useScroll) {
		this.useScroll = useScroll;
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
		MRPortalClientNetworking.sendScreenClosed();
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected void rebuildWidgets() {
		this.clearWidgets();
		int left = this.left();
		int top = this.top();
		int btnY = top + PANEL_HEIGHT - 28;
		int sideWidth = PANEL_WIDTH - LIST_WIDTH - 42;

		int btnX = left + 12;
		int btnW = 68;
		int btnGap = 4;

		this.addButton = this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.add"), button -> {
			this.activeDialog = Dialog.NAME_INPUT;
			this.rebuildWidgets();
		}).bounds(btnX, btnY, btnW, 20).build());
		btnX += btnW + btnGap;

		this.renameButton = this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.rename"), button -> {
			if (this.selectedWaypointId != null) {
				this.activeDialog = Dialog.RENAME_INPUT;
				this.rebuildWidgets();
			}
		}).bounds(btnX, btnY, btnW, 20).build());
		btnX += btnW + btnGap;

		this.deleteButton = this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.delete"), button -> {
			if (this.selectedWaypointId != null) {
				this.activeDialog = Dialog.CONFIRM_DELETE;
				this.rebuildWidgets();
			}
		}).bounds(btnX, btnY, btnW, 20).build());
		btnX += btnW + btnGap;

		this.teleportButton = this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.teleport"), button -> this.tryTeleport())
			.bounds(btnX, btnY, btnW, 20).build());

		this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.close"), button -> this.onClose())
			.bounds(left + PANEL_WIDTH - 42, btnY, 30, 20).build());

		if (this.activeDialog == Dialog.CONFIRM_DELETE) {
			int dlgLeft = left + PANEL_WIDTH / 2 - 90;
			int dlgTop = top + PANEL_HEIGHT / 2 - 30;
			this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.yes"), button -> {
				if (this.selectedWaypointId != null) {
					MRPortalClientNetworking.sendDeleteWaypoint(this.selectedWaypointId);
				}
				this.activeDialog = Dialog.NONE;
			}).bounds(dlgLeft + 16, dlgTop + 30, 72, 20).build());
			this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.no"), button -> {
				this.activeDialog = Dialog.NONE;
				this.rebuildWidgets();
			}).bounds(dlgLeft + 96, dlgTop + 30, 72, 20).build());
		}

		if (this.activeDialog == Dialog.NAME_INPUT || this.activeDialog == Dialog.RENAME_INPUT) {
			int dlgLeft = left + PANEL_WIDTH / 2 - 100;
			int dlgTop = top + PANEL_HEIGHT / 2 - 36;
			this.dialogNameField = this.addRenderableWidget(new EditBox(this.font, dlgLeft + 10, dlgTop + 24, 180, 18, ModTranslation.get("screen.mr_portal.name_hint")));
			this.dialogNameField.setMaxLength(32);
			this.dialogNameField.setHint(ModTranslation.get("screen.mr_portal.name_hint"));
			if (this.activeDialog == Dialog.RENAME_INPUT) {
				WaypointData wp = this.selectedWaypoint();
				if (wp != null) {
					this.dialogNameField.setValue(wp.name());
				}
			}
			this.dialogNameField.setFocused(true);
			this.setFocused(this.dialogNameField);

			this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.dialog_ok"), button -> {
				String name = this.dialogNameField.getValue();
				if (this.activeDialog == Dialog.NAME_INPUT) {
					MRPortalClientNetworking.sendAddWaypoint(name);
				} else if (this.activeDialog == Dialog.RENAME_INPUT && this.selectedWaypointId != null) {
					MRPortalClientNetworking.sendRenameWaypoint(this.selectedWaypointId, name);
				}
				this.activeDialog = Dialog.NONE;
			}).bounds(dlgLeft + 10, dlgTop + 48, 86, 20).build());

			this.addRenderableWidget(Button.builder(ModTranslation.get("screen.mr_portal.dialog_cancel"), button -> {
				this.activeDialog = Dialog.NONE;
				this.rebuildWidgets();
			}).bounds(dlgLeft + 104, dlgTop + 48, 86, 20).build());
		}

		this.updateButtonState();
	}

	private void tryTeleport() {
		if (this.selectedWaypointId == null) {
			this.statusMessage = ModTranslation.get("screen.mr_portal.select_waypoint");
			this.statusColor = 0xF6D47A;
			return;
		}
		if (!this.creativeView) {
			WaypointData wp = this.selectedWaypoint();
			if (wp != null && !wp.dimension().equals(this.currentDimension)) {
				this.statusMessage = ModTranslation.get("message.mr_portal.same_dimension_only");
				this.statusColor = 0xF08585;
				return;
			}
		}
		if (!this.creativeView && this.availablePearls < this.requiredPearls) {
			this.statusMessage = ModTranslation.get("screen.mr_portal.not_enough_resources", this.requiredPearls, this.availablePearls);
			this.statusColor = 0xF08585;
			return;
		}
		MRPortalClientNetworking.sendTeleportRequest(this.selectedWaypointId, this.useScroll);
		this.onClose();
	}

	private void toggleFavorite() {
		WaypointData waypoint = this.selectedWaypoint();
		if (waypoint == null) {
			return;
		}
		this.statusMessage = ModTranslation.get("screen.mr_portal.ready");
		this.statusColor = 0x99E2BE;
		this.updateButtonState();
		MRPortalClientNetworking.sendSetFavoriteWaypoint(waypoint.id(), !waypoint.favorite());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.activeDialog != Dialog.NONE) {
			return super.mouseClicked(mouseX, mouseY, button);
		}
		if (button == 0) {
			int listLeft = this.left() + 12;
			int listTop = this.top() + 46;
			List<ListEntry> entries = this.buildListEntries();
			int y = listTop;
			for (int i = this.scrollOffset; i < entries.size() && y < listTop + LIST_HEIGHT; i++) {
				ListEntry entry = entries.get(i);
				if (entry.waypoint == null) {
					y += GROUP_HEADER_HEIGHT;
				} else {
					if (mouseY >= y && mouseY < y + ROW_HEIGHT && mouseX >= listLeft && mouseX <= listLeft + LIST_WIDTH) {
						this.selectedWaypointId = entry.waypoint.id();
						this.statusMessage = ModTranslation.get("screen.mr_portal.ready");
						this.statusColor = 0x99E2BE;
						this.updateButtonState();
						if (mouseX <= listLeft + 22) {
							this.toggleFavorite();
							return true;
						}
						return true;
					}
					y += ROW_HEIGHT;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (this.activeDialog != Dialog.NONE) {
			return super.mouseScrolled(mouseX, mouseY, delta);
		}
		int maxScroll = Math.max(0, this.buildListEntries().size() - 1);
		this.scrollOffset = Mth.clamp(this.scrollOffset - (int) Math.signum(delta), 0, maxScroll);
		return true;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		int left = this.left();
		int top = this.top();
		int listLeft = left + 12;
		int listTop = top + 46;
		int sideLeft = left + LIST_WIDTH + 30;
		int sideWidth = PANEL_WIDTH - LIST_WIDTH - 42;

		// Main panel
		graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xD010131E);
		graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + PANEL_HEIGHT - 2, 0xD51D2635);

		// Header
		graphics.fill(left + 12, top + 10, left + PANEL_WIDTH - 12, top + 40, 0x8036495F);
		graphics.drawString(this.font, this.title, left + 16, top + 14, 0xFFF4E2, false);
		Component modeLabel = ModTranslation.get(this.creativeView ? "screen.mr_portal.mode_creative" : "screen.mr_portal.mode_survival");
		graphics.drawString(this.font, modeLabel, left + 16, top + 28, 0xC9D6F7, false);

		// List background
		graphics.fill(listLeft, listTop, listLeft + LIST_WIDTH, listTop + LIST_HEIGHT, 0x70212936);

		// Render grouped list
		List<ListEntry> entries = this.buildListEntries();
		int y = listTop;
		boolean hasContent = false;
		for (int i = this.scrollOffset; i < entries.size() && y < listTop + LIST_HEIGHT; i++) {
			ListEntry entry = entries.get(i);
			if (entry.waypoint == null) {
				// Dimension group header
				if (y + GROUP_HEADER_HEIGHT <= listTop + LIST_HEIGHT) {
					graphics.fill(listLeft + 2, y + 1, listLeft + LIST_WIDTH - 2, y + GROUP_HEADER_HEIGHT - 1, 0x50364960);
					graphics.drawString(this.font, Component.literal(entry.dimensionName), listLeft + 8, y + 5, 0xB0C4E0, false);
					hasContent = true;
				}
				y += GROUP_HEADER_HEIGHT;
			} else {
				if (y + ROW_HEIGHT <= listTop + LIST_HEIGHT) {
					boolean selected = entry.waypoint.id().equals(this.selectedWaypointId);
					graphics.fill(listLeft + 4, y + 2, listLeft + LIST_WIDTH - 4, y + ROW_HEIGHT - 2, selected ? 0xC04B5E7D : 0x70262F3F);
					String star = entry.waypoint.favorite() ? "★" : "☆";
					graphics.drawString(this.font, Component.literal(star), listLeft + 9, y + 4, entry.waypoint.favorite() ? 0xFFF4AE : 0xC8D0E2, false);
					graphics.drawString(this.font, Component.literal(entry.waypoint.name()), listLeft + 24, y + 4, 0xFFF0D6, false);
					String coords = String.format(Locale.ROOT, "%.0f  %.0f  %.0f", entry.waypoint.x(), entry.waypoint.y(), entry.waypoint.z());
					graphics.pose().pushPose();
					graphics.pose().scale(0.75F, 0.75F, 1.0F);
					graphics.drawString(this.font, coords, (int) ((listLeft + 24) / 0.75F), (int) ((y + 15) / 0.75F), 0xAEB9D8, false);
					graphics.pose().popPose();
					hasContent = true;
				}
				y += ROW_HEIGHT;
			}
		}
		if (!hasContent) {
			graphics.drawCenteredString(this.font, ModTranslation.get("screen.mr_portal.empty"), listLeft + LIST_WIDTH / 2, listTop + LIST_HEIGHT / 2 - 4, 0xAAB4D4);
		}

		// Scrollbar
		if (!entries.isEmpty()) {
			int totalEntries = entries.size();
			int visibleEntries = countVisibleEntries();
			if (totalEntries > visibleEntries && visibleEntries > 0) {
				int barX = listLeft + LIST_WIDTH - 4;
				float ratio = (float) visibleEntries / totalEntries;
				int barHeight = Math.max(10, (int) (LIST_HEIGHT * ratio));
				int maxScroll = Math.max(1, totalEntries - visibleEntries);
				int barY = listTop + (int) ((LIST_HEIGHT - barHeight) * ((float) this.scrollOffset / maxScroll));
				graphics.fill(barX, barY, barX + 3, barY + barHeight, 0x804B5E7D);
			}
		}

		// Side panel - selected waypoint details
		graphics.fill(sideLeft, listTop, left + PANEL_WIDTH - 12, listTop + LIST_HEIGHT, 0x70313A4B);
		graphics.drawString(this.font, ModTranslation.get("screen.mr_portal.selected_title"), sideLeft + 8, listTop + 8, 0xFFF4E2, false);

		WaypointData selectedWaypoint = this.selectedWaypoint();
		if (selectedWaypoint == null) {
			this.drawWrapped(graphics, ModTranslation.get("screen.mr_portal.select_waypoint"), sideLeft + 8, listTop + 24, sideWidth - 12, 0xAAB4D4);
		} else {
			graphics.drawString(this.font, Component.literal((selectedWaypoint.favorite() ? "★ " : "") + selectedWaypoint.name()), sideLeft + 8, listTop + 24, selectedWaypoint.favorite() ? 0xFFF4AE : 0xFFE7BE, false);
			graphics.drawString(this.font, Component.literal(DimensionUtil.prettifyDimension(selectedWaypoint.dimension())), sideLeft + 8, listTop + 38, 0xC9D6F7, false);
			graphics.drawString(this.font, Component.literal(String.format(Locale.ROOT, "X: %.1f", selectedWaypoint.x())), sideLeft + 8, listTop + 56, 0xDDE5F9, false);
			graphics.drawString(this.font, Component.literal(String.format(Locale.ROOT, "Y: %.1f", selectedWaypoint.y())), sideLeft + 8, listTop + 68, 0xDDE5F9, false);
			graphics.drawString(this.font, Component.literal(String.format(Locale.ROOT, "Z: %.1f", selectedWaypoint.z())), sideLeft + 8, listTop + 80, 0xDDE5F9, false);
		}

		// Resources line
		graphics.drawString(this.font, ModTranslation.get("screen.mr_portal.resources_title"), sideLeft + 8, listTop + 104, 0xFFF4E2, false);
		Component resourcesLine = this.creativeView ? ModTranslation.get("screen.mr_portal.resource_free") : ModTranslation.get("screen.mr_portal.resource_cost", this.requiredPearls, this.availablePearls);
		this.drawWrapped(graphics, resourcesLine, sideLeft + 8, listTop + 118, sideWidth - 12, this.creativeView || this.availablePearls >= this.requiredPearls ? 0x99E2BE : 0xF08585);

		// Preview hint
		this.drawWrapped(graphics, ModTranslation.get("screen.mr_portal.preview_hint"), sideLeft + 8, listTop + 146, sideWidth - 12, 0x8899B4);

		// Status bar
		graphics.fill(left + 12, top + PANEL_HEIGHT - 52, left + PANEL_WIDTH - 12, top + PANEL_HEIGHT - 34, 0x70212936);
		this.drawWrapped(graphics, this.statusMessage, left + 18, top + PANEL_HEIGHT - 49, PANEL_WIDTH - 80, this.statusColor);

		// Dialogs overlay
		if (this.activeDialog == Dialog.CONFIRM_DELETE) {
			int dlgLeft = left + PANEL_WIDTH / 2 - 90;
			int dlgTop = top + PANEL_HEIGHT / 2 - 30;
			graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x90000000);
			graphics.fill(dlgLeft, dlgTop, dlgLeft + 180, dlgTop + 56, 0xF0212631);
			graphics.fill(dlgLeft + 1, dlgTop + 1, dlgLeft + 179, dlgTop + 55, 0xF01D2635);
			graphics.drawCenteredString(this.font, ModTranslation.get("screen.mr_portal.confirm_delete"), dlgLeft + 90, dlgTop + 10, 0xFFFFFF);
		}

		if (this.activeDialog == Dialog.NAME_INPUT || this.activeDialog == Dialog.RENAME_INPUT) {
			int dlgLeft = left + PANEL_WIDTH / 2 - 100;
			int dlgTop = top + PANEL_HEIGHT / 2 - 36;
			graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0x90000000);
			graphics.fill(dlgLeft, dlgTop, dlgLeft + 200, dlgTop + 74, 0xF0212631);
			graphics.fill(dlgLeft + 1, dlgTop + 1, dlgLeft + 199, dlgTop + 73, 0xF01D2635);
			Component dlgTitle = this.activeDialog == Dialog.NAME_INPUT
				? ModTranslation.get("screen.mr_portal.name_title")
				: ModTranslation.get("screen.mr_portal.rename_title");
			graphics.drawCenteredString(this.font, dlgTitle, dlgLeft + 100, dlgTop + 8, 0xFFF4E2);
		}

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void updateButtonState() {
		boolean hasSelection = this.selectedWaypointId != null;
		boolean hasResources = this.creativeView || this.availablePearls >= this.requiredPearls;
		boolean canTeleport = hasSelection && hasResources;

		if (canTeleport && !this.creativeView) {
			WaypointData wp = this.selectedWaypoint();
			if (wp != null && !wp.dimension().equals(this.currentDimension)) {
				canTeleport = false;
			}
		}

		if (this.teleportButton != null) {
			this.teleportButton.active = canTeleport && this.activeDialog == Dialog.NONE;
		}
		if (this.deleteButton != null) {
			this.deleteButton.active = hasSelection && this.activeDialog == Dialog.NONE;
		}
		if (this.renameButton != null) {
			this.renameButton.active = hasSelection && this.activeDialog == Dialog.NONE;
		}
		if (this.addButton != null) {
			this.addButton.active = this.activeDialog == Dialog.NONE;
		}
	}

	private List<ListEntry> buildListEntries() {
		Map<String, List<WaypointData>> groups = new LinkedHashMap<>();

		// Current dimension first
		String currentDimName = DimensionUtil.prettifyDimension(this.currentDimension);
		groups.put(currentDimName, new ArrayList<>());

		for (WaypointData wp : this.waypoints) {
			String dimName = DimensionUtil.prettifyDimension(wp.dimension());
			groups.computeIfAbsent(dimName, k -> new ArrayList<>()).add(wp);
		}

		List<ListEntry> result = new ArrayList<>();
		for (Map.Entry<String, List<WaypointData>> entry : groups.entrySet()) {
			if (entry.getValue().isEmpty()) continue;
			result.add(new ListEntry(entry.getKey(), null));
			for (WaypointData wp : entry.getValue()) {
				result.add(new ListEntry(entry.getKey(), wp));
			}
		}

		return result;
	}

	private int countVisibleEntries() {
		int height = 0;
		int count = 0;
		List<ListEntry> entries = this.buildListEntries();
		for (int i = this.scrollOffset; i < entries.size(); i++) {
			int h = entries.get(i).waypoint == null ? GROUP_HEADER_HEIGHT : ROW_HEIGHT;
			if (height + h > LIST_HEIGHT) break;
			height += h;
			count++;
		}
		return count;
	}

	private WaypointData selectedWaypoint() {
		if (this.selectedWaypointId == null) return null;
		for (WaypointData wp : this.waypoints) {
			if (wp.id().equals(this.selectedWaypointId)) return wp;
		}
		return null;
	}

	private int left() {
		return (this.width - PANEL_WIDTH) / 2;
	}

	private int top() {
		return (this.height - PANEL_HEIGHT) / 2;
	}

	private void drawWrapped(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
		int lineY = y;
		for (var line : this.font.split(text, maxWidth)) {
			graphics.drawString(this.font, line, x, lineY, color, false);
			lineY += 10;
		}
	}

	private record ListEntry(String dimensionName, WaypointData waypoint) {}
}
