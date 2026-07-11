package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.menu.SnowmanUpgradeMenu;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeInventory;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        drawProjectileBar(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderProjectileBarTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
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
