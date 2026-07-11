package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Arrays;
import java.util.List;

public final class UpgradeDisplay {
    private UpgradeDisplay() {
    }

    public static SnowmanUpgradeType canonicalType(SnowmanUpgradeType type) {
        return type == SnowmanUpgradeType.SPLASH_POTION ? SnowmanUpgradeType.POTION : type;
    }

    public static List<SnowmanUpgradeType> guideTypes() {
        return Arrays.stream(SnowmanUpgradeType.values())
                .filter(type -> type != SnowmanUpgradeType.SPLASH_POTION)
                .toList();
    }

    public static ItemStack representativeStack(SnowmanUpgradeType type) {
        ItemStack stack = new ItemStack(type.item());
        if (type == SnowmanUpgradeType.TIPPED_ARROW || type == SnowmanUpgradeType.POTION) {
            PotionUtils.setPotion(stack, Potions.STRONG_HEALING);
        }
        return stack;
    }

    public static Component displayName(SnowmanUpgradeType type) {
        if (type == SnowmanUpgradeType.TIPPED_ARROW) {
            return Component.translatable("gui.super_snowmen.guide.any_tipped_arrow");
        }
        if (type == SnowmanUpgradeType.POTION || type == SnowmanUpgradeType.SPLASH_POTION) {
            return Component.translatable("gui.super_snowmen.guide.any_potion");
        }
        return representativeStack(type).getHoverName();
    }
}
