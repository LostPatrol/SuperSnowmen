package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.menu.SnowmanUpgradeMenu;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeInventory;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class SnowmanUpgradeScreen extends AbstractContainerScreen<SnowmanUpgradeMenu> {
    private static final int PANEL = 0xFF3B4252;
    private static final int BORDER = 0xFF8FBCBB;
    private static final int TEXT = 0xFFECEFF4;
    private static final int EMPTY_BAR = 0xFF4C566A;

    public SnowmanUpgradeScreen(SnowmanUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 196;
        imageHeight = 188;
        inventoryLabelY = 94;
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
        int x = leftPos + 17;
        int y = topPos + 84;
        int width = 162;
        int height = 8;
        Map<SnowmanUpgradeType, Integer> counts = new LinkedHashMap<>();
        int total = 0;
        for (int slot = SnowmanUpgradeInventory.PLUGIN_START; slot < SnowmanUpgradeInventory.PLUGIN_START + SnowmanUpgradeInventory.PLUGIN_COUNT; slot++) {
            ItemStack stack = menu.upgrades().getStackInSlot(slot);
            SnowmanUpgradeType type = SnowmanUpgradeType.byItem(stack.getItem());
            if (type != null) {
                counts.merge(type, stack.getCount(), Integer::sum);
                total += stack.getCount();
            }
        }

        graphics.fill(x, y, x + width, y + height, EMPTY_BAR);
        int used = Math.min(total, SnowmanUpgradeInventory.PLUGIN_COUNT);
        int offset = 0;
        for (Map.Entry<SnowmanUpgradeType, Integer> entry : counts.entrySet()) {
            int segment = used == 0 ? 0 : Math.round(width * (entry.getValue() / (float) SnowmanUpgradeInventory.PLUGIN_COUNT));
            if (segment <= 0) {
                continue;
            }
            graphics.fill(x + offset, y, Math.min(x + width, x + offset + segment), y + height, 0xFF000000 | entry.getKey().color());
            offset += segment;
        }
        graphics.fill(x, y, x + width, y + 1, 0xFF2E3440);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF2E3440);
    }
}
