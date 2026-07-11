package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.menu.LimitedSlotItemHandler;
import net.lostpatrol.supersnowmen.menu.SnowmanUpgradeMenu;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeInventory;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeEffects;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

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
    private static final int BAR_Y = 104;
    private static final int BAR_WIDTH = 162;
    private static final int BAR_HEIGHT = 6;

    private List<BarSegment> barSegments = List.of();
    private ColdButton effectsButton;

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
        ColdButton guideButton = new ColdButton(leftPos + 160, topPos + 4, 14, 14,
                Component.literal("?"), () -> minecraft.setScreen(new UpgradeGuideScreen(this)));
        guideButton.setTooltip(Tooltip.create(Component.translatable("gui.super_snowmen.guide.open")));
        addRenderableWidget(guideButton);
        effectsButton = addRenderableWidget(new ColdButton(leftPos + 177, topPos + 4, 14, 14,
                Component.literal("i"), () -> {
                }));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!renderUpgradeSlotTooltip(graphics, mouseX, mouseY)) {
            renderTooltip(graphics, mouseX, mouseY);
        }
        renderProjectileBarTooltip(graphics, mouseX, mouseY);
        renderActiveEffectsTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, Component.literal("Super Snowman"), imageWidth / 2, 7, TEXT);
        graphics.drawCenteredString(font, Component.translatable("gui.super_snowmen.composition"),
                imageWidth / 2, 92, TEXT);
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
            default -> specialSlotTooltipKey();
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

    private String specialSlotTooltipKey() {
        if (!SnowmanUpgradeInventory.isSpecialSlot(hoveredSlot.getSlotIndex())) {
            return null;
        }
        ItemStack stack = hoveredSlot.getItem();
        if (stack.isEmpty()) {
            return "gui.super_snowmen.hint.ice_generic";
        }
        if (stack.is(net.minecraft.world.item.Items.PACKED_ICE)) {
            return "gui.super_snowmen.hint.packed_ice";
        }
        if (stack.is(net.minecraft.world.item.Items.BLUE_ICE)) {
            return "gui.super_snowmen.hint.blue_ice";
        }
        return "gui.super_snowmen.hint.ice";
    }

    private void drawProjectileBar(GuiGraphics graphics) {
        int x = leftPos + BAR_X;
        int y = topPos + BAR_Y;
        Map<SnowmanUpgradeType, Integer> counts = new LinkedHashMap<>();
        int installed = 0;
        boolean hasTrident = !menu.upgrades().findPreferredTrident().isEmpty();
        boolean hasFirework = menu.upgrades().hasProjectileUpgrade(SnowmanUpgradeType.FIREWORK_ROCKET);
        for (int slot = SnowmanUpgradeInventory.PLUGIN_START; slot < SnowmanUpgradeInventory.PLUGIN_START + SnowmanUpgradeInventory.PLUGIN_COUNT; slot++) {
            ItemStack stack = menu.upgrades().getStackInSlot(slot);
            SnowmanUpgradeType type = SnowmanUpgradeType.byItem(stack.getItem());
            if (type == SnowmanUpgradeType.LIGHTNING_ROD) {
                type = hasTrident ? SnowmanUpgradeType.TRIDENT : null;
            } else if (type == SnowmanUpgradeType.BOW) {
                type = SnowmanUpgradeType.ARROW;
            } else if (type == SnowmanUpgradeType.CROSSBOW) {
                type = hasFirework ? SnowmanUpgradeType.FIREWORK_ROCKET : null;
            }
            if (type != null) {
                counts.merge(UpgradeDisplay.canonicalType(type), stack.getCount(), Integer::sum);
                installed += stack.getCount();
            }
        }

        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, EMPTY_BAR);
        List<CompositionEntry> entries = new ArrayList<>();
        for (Map.Entry<SnowmanUpgradeType, Integer> entry : counts.entrySet()) {
            entries.add(new CompositionEntry(
                    UpgradeDisplay.projectileName(entry.getKey()),
                    entry.getKey().color(),
                    entry.getValue()
            ));
        }
        if (installed < SnowmanUpgradeInventory.PLUGIN_COUNT) {
            entries.add(new CompositionEntry(
                    Component.translatable("gui.super_snowmen.projectile.snowball"),
                    0xDCEEF7,
                    SnowmanUpgradeInventory.PLUGIN_COUNT - installed
            ));
        }
        List<BarSegment> segments = new ArrayList<>();
        int usedSlots = 0;
        for (CompositionEntry entry : entries) {
            int start = Math.round(BAR_WIDTH * (usedSlots / (float)SnowmanUpgradeInventory.PLUGIN_COUNT));
            usedSlots += entry.count();
            int end = Math.round(BAR_WIDTH * (usedSlots / (float)SnowmanUpgradeInventory.PLUGIN_COUNT));
            graphics.fill(x + start, y, x + end, y + BAR_HEIGHT, 0xFF000000 | entry.color());
            segments.add(new BarSegment(entry.name(), entry.count() * 5, x + start, x + end));
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
                graphics.renderTooltip(font, Component.translatable(
                        "gui.super_snowmen.composition.tooltip", segment.name(), segment.percentage()), mouseX, mouseY);
                return;
            }
        }
    }

    private void renderActiveEffectsTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (effectsButton == null || !effectsButton.isHovered()) {
            return;
        }
        List<Component> effects = getActiveEffects();
        if (effects.isEmpty()) {
            effects = List.of(colored("gui.super_snowmen.effects.none", 0xAAB4C2));
        }
        int contentWidth = effects.stream().mapToInt(font::width).max().orElse(0);
        int tooltipWidth = contentWidth + 8;
        int tooltipHeight = effects.size() * 10 + 6;
        int x = mouseX + 12;
        if (x + tooltipWidth > width - 4) {
            x = mouseX - tooltipWidth - 12;
        }
        x = Math.max(4, Math.min(x, width - tooltipWidth - 4));
        int y = Math.max(4, Math.min(mouseY - 12, height - tooltipHeight - 4));
        graphics.fill(x, y, x + tooltipWidth, y + tooltipHeight, BORDER);
        graphics.fill(x + 1, y + 1, x + tooltipWidth - 1, y + tooltipHeight - 1, 0xF02E3440);
        for (int line = 0; line < effects.size(); line++) {
            graphics.drawString(font, effects.get(line), x + 4, y + 4 + line * 10, TEXT, false);
        }
    }

    private List<Component> getActiveEffects() {
        List<Component> effects = new ArrayList<>();
        var upgrades = menu.upgrades();
        int setBonusLevel = upgrades.areAllPluginSlotsFilled()
                ? upgrades.areAllAttributeSlotsActive() ? 2 : 1
                : 0;
        if (setBonusLevel > 0) {
            effects.add(colored(setBonusLevel == 2
                    ? "gui.super_snowmen.effects.regeneration_ii"
                    : "gui.super_snowmen.effects.regeneration_i", 0xCD5CAB));
            effects.add(colored(setBonusLevel == 2
                    ? "gui.super_snowmen.effects.resistance_ii"
                    : "gui.super_snowmen.effects.resistance_i", 0xCD5CAB));
        }
        int pumpkins = upgrades.getStackInSlot(SnowmanUpgradeInventory.BASE_PUMPKIN_SLOT).getCount();
        int snowBlocks = upgrades.getStackInSlot(SnowmanUpgradeInventory.BASE_SNOW_SLOT).getCount();
        int diamonds = upgrades.getStackInSlot(SnowmanUpgradeInventory.BASE_DIAMOND_SLOT).getCount();
        if (pumpkins > 0) {
            effects.add(colored("gui.super_snowmen.effects.attack_speed", 0xF59E42, pumpkins * 4));
        }
        if (snowBlocks > 0) {
            effects.add(colored("gui.super_snowmen.effects.max_health", 0xEF5B6C, snowBlocks * 2));
        }
        if (diamonds > 0) {
            effects.add(colored("gui.super_snowmen.effects.projectile_damage", 0x55DDE0, diamonds));
            effects.add(colored("gui.super_snowmen.effects.protection", 0xFFFFFF, romanLevel(diamonds)));
        }

        ItemStack bow = upgrades.findBow();
        if (!bow.isEmpty()) {
            int bowColor = SnowmanUpgradeType.BOW.color();
            effects.add(colored("gui.super_snowmen.effects.archery", bowColor));
            addBowEnchantment(effects, bow, Enchantments.FLAMING_ARROWS, bowColor);
            addBowEnchantment(effects, bow, Enchantments.POWER_ARROWS, bowColor);
            addBowEnchantment(effects, bow, Enchantments.PUNCH_ARROWS, bowColor);
        }
        SnowmanUpgradeEffects.ArmorTier tier = SnowmanUpgradeEffects.armorTier(upgrades);
        boolean shulker = upgrades.hasProjectileUpgrade(SnowmanUpgradeType.SHULKER_SHELL);
        int armor = Math.min(30, (int)tier.armor + (shulker ? 20 : 0));
        if (armor > 0) {
            effects.add(colored("gui.super_snowmen.effects.armor", 0xFFFFFF, armor));
        }
        if (tier.toughness > 0.0D) {
            effects.add(colored("gui.super_snowmen.effects.toughness", 0xFFFFFF, (int)tier.toughness));
        }
        if (!upgrades.findCrossbow().isEmpty()
                && upgrades.hasProjectileUpgrade(SnowmanUpgradeType.FIREWORK_ROCKET)) {
            effects.add(rainbow("gui.super_snowmen.effects.firework_party"));
        }
        if (tier.climateImmune) {
            effects.add(colored("gui.super_snowmen.effects.climate_immunity", 0xFFB347));
        }
        if (tier.wetImmune) {
            effects.add(colored("gui.super_snowmen.effects.drowning_immunity", 0x4AA3FF));
        }
        if (upgrades.hasProjectileUpgrade(SnowmanUpgradeType.FIRE_CHARGE)) {
            effects.add(colored("gui.super_snowmen.effects.fire_immunity", 0xFF7A24));
        }
        if (upgrades.hasProjectileUpgrade(SnowmanUpgradeType.DRAGON_BREATH)) {
            effects.add(colored("gui.super_snowmen.effects.dragon_breath_immunity", 0xC36BFF));
        }
        if (upgrades.hasProjectileUpgrade(SnowmanUpgradeType.TNT)) {
            effects.add(colored("gui.super_snowmen.effects.explosion_immunity", 0xF04444));
        }
        if (upgrades.hasProjectileUpgrade(SnowmanUpgradeType.WITHER_SKULL)) {
            effects.add(colored("gui.super_snowmen.effects.wither_immunity", 0xA8A8B3));
            effects.add(colored("gui.super_snowmen.effects.projectile_field", 0x7F8C9D));
            effects.add(colored("gui.super_snowmen.effects.kill_healing", 0x67C587));
        }
        if (shulker) {
            effects.add(colored("gui.super_snowmen.effects.levitation_immunity", 0xD78BE6));
        }
        if (upgrades.hasProjectileUpgrade(SnowmanUpgradeType.EGG)) {
            effects.add(colored("gui.super_snowmen.effects.cluck", 0xF2D16B));
            effects.add(colored("gui.super_snowmen.effects.slow_falling", 0xD7F0FF));
        }
        if (upgrades.hasProjectileUpgrade(SnowmanUpgradeType.LIGHTNING_ROD)
                && !upgrades.findPreferredTrident().isEmpty()) {
            effects.add(colored("gui.super_snowmen.effects.extra_trident", 0xF2D15C));
            if (upgrades.hasChannelingTrident()) {
                effects.add(colored("gui.super_snowmen.effects.weatherproof_channeling", 0xF2D15C));
            }
        }
        if (!effects.isEmpty()) {
            effects.add(0, colored("gui.super_snowmen.effects.super_snowman", 0x7AD7F0));
        }
        return effects;
    }

    private Component colored(String key, int color, Object... args) {
        return Component.translatable(key, args).withStyle(style -> style.withColor(color));
    }

    private Component rainbow(String key) {
        String text = Component.translatable(key).getString();
        MutableComponent result = Component.empty();
        float animationPhase = (Util.getMillis() % 3000L) / 3000.0F;
        for (int index = 0; index < text.length(); index++) {
            float characterPhase = index / (float)Math.max(1, text.length());
            int color = Mth.hsvToRgb((animationPhase + characterPhase) % 1.0F, 0.8F, 1.0F);
            result.append(Component.literal(text.substring(index, index + 1))
                    .withStyle(style -> style.withColor(color)));
        }
        return result;
    }

    private void addBowEnchantment(List<Component> effects, ItemStack bow, Enchantment enchantment, int color) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(enchantment, bow);
        if (level <= 0) {
            return;
        }
        Component name = Component.translatable(enchantment.getDescriptionId());
        if (enchantment.getMaxLevel() > 1) {
            name = name.copy().append(" ").append(romanLevel(level));
        }
        effects.add(name.copy().withStyle(style -> style.withColor(color)));
    }

    private String romanLevel(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> "V";
        };
    }

    private record CompositionEntry(Component name, int color, int count) {
    }

    private record BarSegment(Component name, int percentage, int startX, int endX) {
    }
}
