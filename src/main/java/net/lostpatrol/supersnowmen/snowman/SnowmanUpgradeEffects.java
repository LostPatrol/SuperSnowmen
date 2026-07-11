package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SnowmanUpgradeEffects {
    private static final double BASE_MAX_HEALTH = 4.0D;

    private SnowmanUpgradeEffects() {
    }

    public static void apply(SnowGolem snowman, SnowmanUpgradeInventory inventory) {
        int snowBlocks = inventory.getStackInSlot(SnowmanUpgradeInventory.BASE_SNOW_SLOT).getCount();
        setBaseValue(snowman.getAttribute(Attributes.MAX_HEALTH), BASE_MAX_HEALTH + snowBlocks * 2.0D);
        if (snowman.getHealth() > snowman.getMaxHealth()) {
            snowman.setHealth(snowman.getMaxHealth());
        }

        ArmorTier tier = ArmorTier.from(inventory);
        setBaseValue(snowman.getAttribute(Attributes.ARMOR), tier.armor);
        setBaseValue(snowman.getAttribute(Attributes.ARMOR_TOUGHNESS), tier.toughness);
        if (inventory.hasProjectileUpgrade(SnowmanUpgradeType.WITHER_SKULL)) {
            snowman.removeEffect(MobEffects.WITHER);
        }
        if (inventory.hasProjectileUpgrade(SnowmanUpgradeType.SHULKER_SHELL)) {
            snowman.removeEffect(MobEffects.LEVITATION);
        }
        maintainEffects(snowman, inventory);
    }

    public static void maintainEffects(SnowGolem snowman, SnowmanUpgradeInventory inventory) {
        if (inventory.hasProjectileUpgrade(SnowmanUpgradeType.FIRE_CHARGE)) {
            MobEffectInstance current = snowman.getEffect(MobEffects.FIRE_RESISTANCE);
            if (current == null || (current.getAmplifier() == 0 && current.getDuration() <= 20)) {
                snowman.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, true));
            }
        }
    }

    public static ArmorTier armorTier(SnowmanUpgradeInventory inventory) {
        return ArmorTier.from(inventory);
    }

    private static void setBaseValue(AttributeInstance attribute, double value) {
        if (attribute != null && attribute.getBaseValue() != value) {
            attribute.setBaseValue(value);
        }
    }

    public enum ArmorTier {
        NONE(0.0D, 0.0D, false, false),
        ICE(5.0D, 0.0D, true, false),
        PACKED_ICE(10.0D, 0.0D, true, true),
        BLUE_ICE(15.0D, 5.0D, true, true);

        public final double armor;
        public final double toughness;
        public final boolean warmImmune;
        public final boolean wetImmune;

        ArmorTier(double armor, double toughness, boolean warmImmune, boolean wetImmune) {
            this.armor = armor;
            this.toughness = toughness;
            this.warmImmune = warmImmune;
            this.wetImmune = wetImmune;
        }

        static ArmorTier from(SnowmanUpgradeInventory inventory) {
            ArmorTier result = NONE;
            for (int slot = SnowmanUpgradeInventory.SPECIAL_START; slot < SnowmanUpgradeInventory.SPECIAL_START + SnowmanUpgradeInventory.SPECIAL_COUNT; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.getCount() < 64) {
                    continue;
                }
                if (stack.is(Items.BLUE_ICE)) {
                    return BLUE_ICE;
                }
                if (stack.is(Items.PACKED_ICE)) {
                    result = PACKED_ICE;
                } else if (stack.is(Items.ICE) && result == NONE) {
                    result = ICE;
                }
            }
            return result;
        }
    }
}
