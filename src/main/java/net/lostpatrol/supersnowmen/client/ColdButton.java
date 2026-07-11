package net.lostpatrol.supersnowmen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ColdButton extends AbstractButton {
    private static final int NORMAL = 0xFF46566E;
    private static final int HOVERED = 0xFF5E81AC;
    private static final int BORDER = 0xFF8FBCBB;
    private static final int TEXT = 0xFFECEFF4;

    private final Runnable action;

    public ColdButton(int x, int y, int width, int height, Component message, Runnable action) {
        super(x, y, width, height, message);
        this.action = action;
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int border = isHoveredOrFocused() ? 0xFFC5E4E2 : BORDER;
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), border);
        graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1,
                isHoveredOrFocused() ? HOVERED : NORMAL);
        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, TEXT);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
