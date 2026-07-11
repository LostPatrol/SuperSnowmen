package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum SnowmanUpgradeType {
    ARROW(Items.ARROW, 0xD6D6D6),
    SPECTRAL_ARROW(Items.SPECTRAL_ARROW, 0xFFF176),
    FIRE_CHARGE(Items.FIRE_CHARGE, 0xFF6D1A),
    FIREWORK_ROCKET(Items.FIREWORK_ROCKET, 0xF54291),
    DRAGON_BREATH(Items.DRAGON_BREATH, 0xA65AD9),
    TNT(Items.TNT, 0xE03C31),
    WITHER_SKULL(Items.WITHER_SKELETON_SKULL, 0x2D2D34),
    SCULK_SHRIEKER(Items.SCULK_SHRIEKER, 0x1F6D7A),
    TOTEM(Items.TOTEM_OF_UNDYING, 0xFFD257),
    TIPPED_ARROW(Items.TIPPED_ARROW, 0x84D4FF),
    POTION(Items.POTION, 0xB681FF),
    SPLASH_POTION(Items.SPLASH_POTION, 0xB681FF),
    EGG(Items.EGG, 0xF6E8B8),
    TRIDENT(Items.TRIDENT, 0x55B7B0),
    SHULKER_SHELL(Items.SHULKER_SHELL, 0xB66DCC);

    private final Item item;
    private final int color;

    SnowmanUpgradeType(Item item, int color) {
        this.item = item;
        this.color = color;
    }

    public Item item() {
        return item;
    }

    public int color() {
        return color;
    }

    public static SnowmanUpgradeType byItem(Item item) {
        for (SnowmanUpgradeType type : values()) {
            if (type.item == item) {
                return type;
            }
        }
        return null;
    }
}
