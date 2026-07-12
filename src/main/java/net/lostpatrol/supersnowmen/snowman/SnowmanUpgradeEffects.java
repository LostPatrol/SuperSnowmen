package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;

public final class SnowmanUpgradeEffects {
    private static final double BASE_MAX_HEALTH = 4.0D;
    private static final String EGG_TIMER_TAG = "SuperSnowmenEggTimer";
    private static final String SET_BONUS_LEVEL_TAG = "SuperSnowmenSetBonusLevel";
    private static final String GHAST_REGENERATION_AMPLIFIER_TAG = "SuperSnowmenGhastRegenerationAmplifier";

    private SnowmanUpgradeEffects() {
    }

    public static void apply(SnowGolem snowman, SnowmanUpgradeInventory inventory) {
        int snowBlocks = inventory.getStackInSlot(SnowmanUpgradeInventory.BASE_SNOW_SLOT).getCount();
        setBaseValue(snowman.getAttribute(Attributes.MAX_HEALTH), BASE_MAX_HEALTH + snowBlocks * 2.0D);
        if (snowman.getHealth() > snowman.getMaxHealth()) {
            snowman.setHealth(snowman.getMaxHealth());
        }

        ArmorTier tier = ArmorTier.from(inventory);
        double shulkerArmor = inventory.hasProjectileUpgrade(SnowmanUpgradeType.SHULKER_SHELL) ? 20.0D : 0.0D;
        setBaseValue(snowman.getAttribute(Attributes.ARMOR), tier.armor + shulkerArmor);
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
        maintainSetBonus(snowman, inventory);
        if (inventory.hasProjectileUpgrade(SnowmanUpgradeType.FIRE_CHARGE)) {
            MobEffectInstance current = snowman.getEffect(MobEffects.FIRE_RESISTANCE);
            if (current == null || (current.getAmplifier() == 0 && current.getDuration() <= 20)) {
                snowman.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, true));
            }
        }
        if (inventory.hasProjectileUpgrade(SnowmanUpgradeType.EGG)) {
            refreshEffect(snowman, MobEffects.SLOW_FALLING, 40);
            layEggLikeChicken(snowman);
        } else {
            snowman.getPersistentData().remove(EGG_TIMER_TAG);
        }
        if (inventory.hasProjectileUpgrade(SnowmanUpgradeType.GHAST_TEAR)) {
            maintainGhastRegeneration(snowman);
        } else {
            snowman.getPersistentData().remove(GHAST_REGENERATION_AMPLIFIER_TAG);
        }
    }

    private static void maintainSetBonus(SnowGolem snowman, SnowmanUpgradeInventory inventory) {
        boolean allPlugins = inventory.areAllPluginSlotsFilled();
        int level = allPlugins && inventory.areAllAttributeSlotsActive() ? 2 : allPlugins ? 1 : 0;
        int previousLevel = snowman.getPersistentData().getInt(SET_BONUS_LEVEL_TAG);
        if (level != previousLevel) {
            if (previousLevel > 0) {
                snowman.removeEffect(MobEffects.REGENERATION);
                snowman.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
            snowman.getPersistentData().putInt(SET_BONUS_LEVEL_TAG, level);
            snowman.getPersistentData().remove(GHAST_REGENERATION_AMPLIFIER_TAG);
        }
        if (level > 0) {
            refreshEffect(snowman, MobEffects.REGENERATION, 40, level - 1);
            refreshEffect(snowman, MobEffects.DAMAGE_RESISTANCE, 40, level - 1);
        } else {
            snowman.getPersistentData().remove(SET_BONUS_LEVEL_TAG);
        }
    }

    private static void maintainGhastRegeneration(SnowGolem snowman) {
        MobEffectInstance current = snowman.getEffect(MobEffects.REGENERATION);
        boolean tracked = snowman.getPersistentData().contains(GHAST_REGENERATION_AMPLIFIER_TAG);
        int previousAmplifier = snowman.getPersistentData().getInt(GHAST_REGENERATION_AMPLIFIER_TAG);
        int amplifier = current == null ? 0 : current.getAmplifier();
        if (current != null && (!tracked || amplifier != previousAmplifier || current.getDuration() > 40)) {
            amplifier++;
        }
        if (current == null || current.getAmplifier() != amplifier || current.getDuration() <= 20) {
            snowman.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, amplifier, false, true));
        }
        snowman.getPersistentData().putInt(GHAST_REGENERATION_AMPLIFIER_TAG, amplifier);
    }

    private static void refreshEffect(SnowGolem snowman, net.minecraft.world.effect.MobEffect effect, int duration) {
        refreshEffect(snowman, effect, duration, 0);
    }

    private static void refreshEffect(SnowGolem snowman, net.minecraft.world.effect.MobEffect effect,
                                      int duration, int amplifier) {
        MobEffectInstance current = snowman.getEffect(effect);
        if (current == null || current.getAmplifier() < amplifier
                || (current.getAmplifier() == amplifier && current.getDuration() <= 20)) {
            snowman.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true));
        }
    }

    private static void layEggLikeChicken(SnowGolem snowman) {
        if (snowman.level().isClientSide()) {
            return;
        }
        int timer = snowman.getPersistentData().contains(EGG_TIMER_TAG)
                ? snowman.getPersistentData().getInt(EGG_TIMER_TAG)
                : snowman.getRandom().nextInt(6000) + 6000;
        timer--;
        if (timer <= 0) {
            snowman.playSound(SoundEvents.CHICKEN_EGG, 1.0F,
                    (snowman.getRandom().nextFloat() - snowman.getRandom().nextFloat()) * 0.2F + 1.0F);
            snowman.spawnAtLocation(Items.EGG);
            snowman.gameEvent(GameEvent.ENTITY_PLACE);
            timer = snowman.getRandom().nextInt(6000) + 6000;
        }
        snowman.getPersistentData().putInt(EGG_TIMER_TAG, timer);
    }

    public static ArmorTier armorTier(SnowmanUpgradeInventory inventory) {
        return ArmorTier.from(inventory);
    }

    private static void setBaseValue(AttributeInstance attribute, double value) {
        if (attribute != null && attribute.getBaseValue() != value) {
            attribute.setBaseValue(value);
        }
    }

    public static final class ArmorTier {
        public final double armor;
        public final double toughness;
        public final boolean climateImmune;
        public final boolean wetImmune;

        ArmorTier(double armor, double toughness, boolean climateImmune, boolean wetImmune) {
            this.armor = armor;
            this.toughness = toughness;
            this.climateImmune = climateImmune;
            this.wetImmune = wetImmune;
        }

        static ArmorTier from(SnowmanUpgradeInventory inventory) {
            double armor = 0.0D;
            double toughness = 0.0D;
            boolean climateImmune = false;
            boolean wetImmune = false;
            for (int slot = SnowmanUpgradeInventory.SPECIAL_START; slot < SnowmanUpgradeInventory.SPECIAL_START + SnowmanUpgradeInventory.SPECIAL_COUNT; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.getCount() < 64) {
                    continue;
                }
                if (stack.is(Items.BLUE_ICE)) {
                    armor += 15.0D;
                    toughness += 5.0D;
                    climateImmune = true;
                    wetImmune = true;
                } else if (stack.is(Items.PACKED_ICE)) {
                    armor += 10.0D;
                    climateImmune = true;
                    wetImmune = true;
                } else if (stack.is(Items.ICE)) {
                    armor += 5.0D;
                    climateImmune = true;
                }
            }
            return new ArmorTier(armor, toughness, climateImmune, wetImmune);
        }
    }
}
