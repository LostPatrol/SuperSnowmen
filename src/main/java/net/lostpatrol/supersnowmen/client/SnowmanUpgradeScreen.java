package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.menu.LimitedSlotItemHandler;
import net.lostpatrol.supersnowmen.menu.SnowmanUpgradeMenu;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeInventory;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SnowmanUpgradeScreen extends AbstractContainerScreen<SnowmanUpgradeMenu> {
    private static final int PANEL = 0xFF3B4252;
    private static final int BORDER = 0xFF8FBCBB;
    private static final int TEXT = 0xFFECEFF4;
    private static final int EMPTY_BAR = 0xFF4C566A;
    private static final int BAR_X = 17;
    private static final int BAR_Y = 94;
    private static final int BAR_WIDTH = 162;
    private static final int BAR_HEIGHT = 6;

    private List<BarSegment> barSegments = List.of();

    public SnowmanUpgradeScreen(SnowmanUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 196;
        imageHeight = 196;
        inventoryLabelY = 102;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, BORDER);
        graphics.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, BORDER);
        graphics.fill(leftPos, topPos, leftPos + 1, topPos + imageHeight, BORDER);
        graphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + imageHeight, BORDER);
        drawSlotFrames(graphics);
        drawSlotHints(graphics);
        drawProjectileBar(graphics);
    }

    @Override
    protected void init() {
        super.init();
        Button guideButton = Button.builder(Component.literal("?"),
                        button -> minecraft.setScreen(new UpgradeGuideScreen(this)))
                .bounds(leftPos + 41, topPos + 12, 16, 16)
                .build();
        guideButton.setTooltip(Tooltip.create(Component.translatable("gui.super_snowmen.guide.open")));
        addRenderableWidget(guideButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!renderUpgradeSlotTooltip(graphics, mouseX, mouseY)) {
            renderTooltip(graphics, mouseX, mouseY);
        }
        renderProjectileBarTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("gui.super_snowmen.base"), 14, 16, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.super_snowmen.plugins"), 61, 7, TEXT, false);
        graphics.drawString(font, Component.translatable("gui.super_snowmen.special"), 151, 16, TEXT, false);
    }

    private void drawSlotFrames(GuiGraphics graphics) {
        for (var slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            graphics.fill(x, y, x + 18, y + 18, 0xFF2E3440);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF434C5E);
        }
    }

    private void drawSlotHints(GuiGraphics graphics) {
        for (var slot : menu.slots) {
            if (!(slot instanceof LimitedSlotItemHandler) || slot.hasItem()) {
                continue;
            }
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            switch (slot.getSlotIndex()) {
                case SnowmanUpgradeInventory.BASE_PUMPKIN_SLOT -> drawPumpkinHint(graphics, x, y);
                case SnowmanUpgradeInventory.BASE_SNOW_SLOT -> drawHeartHint(graphics, x, y);
                case SnowmanUpgradeInventory.BASE_DIAMOND_SLOT -> drawDiamondHint(graphics, x, y);
                default -> {
                    if (SnowmanUpgradeInventory.isSpecialSlot(slot.getSlotIndex())) {
                        drawShieldHint(graphics, x, y);
                    }
                }
            }
        }
    }

    private void drawPumpkinHint(GuiGraphics graphics, int x, int y) {
        int color = 0xFF70798A;
        graphics.fill(x + 7, y + 1, x + 10, y + 3, color);
        graphics.fill(x + 3, y + 3, x + 13, y + 5, color);
        graphics.fill(x + 2, y + 5, x + 4, y + 13, color);
        graphics.fill(x + 12, y + 5, x + 14, y + 13, color);
        graphics.fill(x + 4, y + 13, x + 12, y + 15, color);
        graphics.fill(x + 5, y + 7, x + 7, y + 9, color);
        graphics.fill(x + 9, y + 7, x + 11, y + 9, color);
    }

    private void drawHeartHint(GuiGraphics graphics, int x, int y) {
        int color = 0xFF70798A;
        graphics.fill(x + 3, y + 3, x + 7, y + 5, color);
        graphics.fill(x + 9, y + 3, x + 13, y + 5, color);
        graphics.fill(x + 2, y + 5, x + 14, y + 9, color);
        graphics.fill(x + 4, y + 9, x + 12, y + 11, color);
        graphics.fill(x + 6, y + 11, x + 10, y + 13, color);
        graphics.fill(x + 7, y + 13, x + 9, y + 15, color);
    }

    private void drawDiamondHint(GuiGraphics graphics, int x, int y) {
        int color = 0xFF70798A;
        graphics.fill(x + 6, y + 2, x + 10, y + 4, color);
        graphics.fill(x + 4, y + 4, x + 12, y + 6, color);
        graphics.fill(x + 2, y + 6, x + 14, y + 9, color);
        graphics.fill(x + 4, y + 9, x + 12, y + 11, color);
        graphics.fill(x + 6, y + 11, x + 10, y + 13, color);
        graphics.fill(x + 7, y + 13, x + 9, y + 15, color);
    }

    private void drawShieldHint(GuiGraphics graphics, int x, int y) {
        int color = 0xFF70798A;
        graphics.fill(x + 3, y + 2, x + 13, y + 4, color);
        graphics.fill(x + 2, y + 4, x + 4, y + 10, color);
        graphics.fill(x + 12, y + 4, x + 14, y + 10, color);
        graphics.fill(x + 4, y + 10, x + 12, y + 12, color);
        graphics.fill(x + 6, y + 12, x + 10, y + 14, color);
        graphics.fill(x + 7, y + 14, x + 9, y + 16, color);
    }

    private boolean renderUpgradeSlotTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!(hoveredSlot instanceof LimitedSlotItemHandler)) {
            return false;
        }
        String key = switch (hoveredSlot.getSlotIndex()) {
            case SnowmanUpgradeInventory.BASE_PUMPKIN_SLOT -> "gui.super_snowmen.hint.pumpkin";
            case SnowmanUpgradeInventory.BASE_SNOW_SLOT -> "gui.super_snowmen.hint.snow";
            case SnowmanUpgradeInventory.BASE_DIAMOND_SLOT -> "gui.super_snowmen.hint.diamond";
            default -> SnowmanUpgradeInventory.isSpecialSlot(hoveredSlot.getSlotIndex())
                    ? "gui.super_snowmen.hint.ice" : null;
        };
        if (key == null) {
            return false;
        }
        List<Component> lines = new ArrayList<>();
        if (hoveredSlot.hasItem()) {
            lines.add(hoveredSlot.getItem().getHoverName());
        }
        lines.add(Component.translatable(key));
        graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        return true;
    }

    private void drawProjectileBar(GuiGraphics graphics) {
        int x = leftPos + BAR_X;
        int y = topPos + BAR_Y;
        Map<SnowmanUpgradeType, Integer> counts = new LinkedHashMap<>();
        for (int slot = SnowmanUpgradeInventory.PLUGIN_START; slot < SnowmanUpgradeInventory.PLUGIN_START + SnowmanUpgradeInventory.PLUGIN_COUNT; slot++) {
            ItemStack stack = menu.upgrades().getStackInSlot(slot);
            SnowmanUpgradeType type = SnowmanUpgradeType.byItem(stack.getItem());
            if (type != null) {
                counts.merge(type, stack.getCount(), Integer::sum);
            }
        }

        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, EMPTY_BAR);
        List<BarSegment> segments = new ArrayList<>();
        int usedSlots = 0;
        for (Map.Entry<SnowmanUpgradeType, Integer> entry : counts.entrySet()) {
            int start = Math.round(BAR_WIDTH * (usedSlots / (float)SnowmanUpgradeInventory.PLUGIN_COUNT));
            usedSlots += entry.getValue();
            int end = Math.round(BAR_WIDTH * (usedSlots / (float)SnowmanUpgradeInventory.PLUGIN_COUNT));
            graphics.fill(x + start, y, x + end, y + BAR_HEIGHT, 0xFF000000 | entry.getKey().color());
            segments.add(new BarSegment(entry.getKey(), x + start, x + end));
        }
        barSegments = List.copyOf(segments);
        graphics.fill(x, y, x + BAR_WIDTH, y + 1, 0xFF2E3440);
        graphics.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF2E3440);
    }

    private void renderProjectileBarTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int y = topPos + BAR_Y;
        if (mouseY < y || mouseY >= y + BAR_HEIGHT) {
            return;
        }
        for (BarSegment segment : barSegments) {
            if (mouseX >= segment.startX() && mouseX < segment.endX()) {
                graphics.renderTooltip(font, new ItemStack(segment.type().item()).getHoverName(), mouseX, mouseY);
                return;
            }
        }
    }

    private record BarSegment(SnowmanUpgradeType type, int startX, int endX) {
    }
}
