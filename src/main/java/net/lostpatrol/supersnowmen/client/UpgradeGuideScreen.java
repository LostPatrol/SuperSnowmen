package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public class UpgradeGuideScreen extends Screen {
    private static final int PANEL = 0xFF3B4252;
    private static final int BORDER = 0xFF8FBCBB;
    private static final int ROW = 0xFF434C5E;
    private static final int ROW_ALT = 0xFF404858;
    private static final int TEXT = 0xFFECEFF4;
    private static final int MUTED_TEXT = 0xFFB8C0CC;
    private static final int ROW_HEIGHT = 34;

    private final Screen parent;
    private double scrollAmount;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public UpgradeGuideScreen(Screen parent) {
        super(Component.translatable("gui.super_snowmen.guide.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(380, width - 24);
        panelHeight = Math.min(230, height - 24);
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        addRenderableWidget(new ColdButton(panelLeft + panelWidth - 58, panelTop + panelHeight - 23,
                50, 16, Component.translatable("gui.done"), this::onClose));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, PANEL);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 1, BORDER);
        graphics.fill(panelLeft, panelTop + panelHeight - 1, panelLeft + panelWidth, panelTop + panelHeight, BORDER);
        graphics.fill(panelLeft, panelTop, panelLeft + 1, panelTop + panelHeight, BORDER);
        graphics.fill(panelLeft + panelWidth - 1, panelTop, panelLeft + panelWidth, panelTop + panelHeight, BORDER);
        graphics.drawCenteredString(font, title, panelLeft + panelWidth / 2, panelTop + 9, TEXT);

        int listTop = panelTop + 25;
        int listBottom = panelTop + panelHeight - 29;
        int listHeight = listBottom - listTop;
        List<SnowmanUpgradeType> types = UpgradeDisplay.guideTypes();
        int maxScroll = Math.max(0, types.size() * ROW_HEIGHT - listHeight);
        scrollAmount = Mth.clamp(scrollAmount, 0.0D, maxScroll);

        graphics.enableScissor(panelLeft + 5, listTop, panelLeft + panelWidth - 7, listBottom);
        for (int i = 0; i < types.size(); i++) {
            int y = listTop + i * ROW_HEIGHT - (int)scrollAmount;
            if (y + ROW_HEIGHT <= listTop || y >= listBottom) {
                continue;
            }
            graphics.fill(panelLeft + 6, y, panelLeft + panelWidth - 9, y + ROW_HEIGHT - 1,
                    i % 2 == 0 ? ROW : ROW_ALT);
            SnowmanUpgradeType type = types.get(i);
            ItemStack icon = UpgradeDisplay.representativeStack(type);
            graphics.renderItem(icon, panelLeft + 11, y + 9);
            graphics.drawString(font, UpgradeDisplay.displayName(type), panelLeft + 34, y + 5, TEXT, false);
            Component description = Component.translatable(
                    "gui.super_snowmen.guide." + type.name().toLowerCase(Locale.ROOT));
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(description, panelWidth - 51);
            for (int line = 0; line < Math.min(2, lines.size()); line++) {
                graphics.drawString(font, lines.get(line), panelLeft + 34, y + 17 + line * 9, MUTED_TEXT, false);
            }
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            int trackX = panelLeft + panelWidth - 6;
            int thumbHeight = Math.max(18, listHeight * listHeight / (types.size() * ROW_HEIGHT));
            int thumbY = listTop + (int)((listHeight - thumbHeight) * scrollAmount / maxScroll);
            graphics.fill(trackX, listTop, trackX + 2, listBottom, 0xFF2E3440);
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, BORDER);
        }
        for (Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listHeight = panelHeight - 54;
        int maxScroll = Math.max(0, UpgradeDisplay.guideTypes().size() * ROW_HEIGHT - listHeight);
        scrollAmount = Mth.clamp(scrollAmount - scrollY * 24.0D, 0.0D, maxScroll);
        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
