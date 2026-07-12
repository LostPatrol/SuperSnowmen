package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.List;

public final class UpgradeDisplay {
    private UpgradeDisplay() {
    }

    public static SnowmanUpgradeType canonicalType(SnowmanUpgradeType type) {
        return type;
    }

    public static List<SnowmanUpgradeType> guideTypes() {
        return List.of(SnowmanUpgradeType.values());
    }

    public static ItemStack representativeStack(SnowmanUpgradeType type) {
        if (type == SnowmanUpgradeType.TIPPED_ARROW
                || type == SnowmanUpgradeType.POTION
                || type == SnowmanUpgradeType.SPLASH_POTION
                || type == SnowmanUpgradeType.LINGERING_POTION) {
            return PotionContents.createItemStack(type.item(), Potions.STRONG_HEALING);
        }
        return new ItemStack(type.item());
    }

    public static Component displayName(SnowmanUpgradeType type) {
        if (type == SnowmanUpgradeType.TIPPED_ARROW) {
            return Component.translatable("gui.super_snowmen.guide.any_tipped_arrow");
        }
        if (type == SnowmanUpgradeType.POTION
                || type == SnowmanUpgradeType.SPLASH_POTION
                || type == SnowmanUpgradeType.LINGERING_POTION) {
            return Component.translatable("gui.super_snowmen.guide.any_" + type.name().toLowerCase(java.util.Locale.ROOT));
        }
        return representativeStack(type).getHoverName();
    }

    public static Component projectileName(SnowmanUpgradeType type) {
        return Component.translatable("gui.super_snowmen.projectile." + canonicalType(type).name().toLowerCase(java.util.Locale.ROOT));
    }
}
