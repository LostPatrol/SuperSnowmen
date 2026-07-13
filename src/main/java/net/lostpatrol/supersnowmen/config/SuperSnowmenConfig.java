package net.lostpatrol.supersnowmen.config;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = SuperSnowmen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SuperSnowmenConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_UPGRADES = BUILDER
            .comment("Enable Super Snowmen upgrades.")
            .define("enableUpgrades", true);

    private static final ForgeConfigSpec.BooleanValue CONSUME_PROJECTILE_ITEMS = BUILDER
            .comment("Consume one matching upgrade item when a replacement projectile is fired.")
            .define("consumeProjectileItems", false);

    private static final ForgeConfigSpec.BooleanValue CONSUME_POTION_PROJECTILES = BUILDER
            .comment("Consume tipped arrows and potions when they are selected as replacement projectiles.")
            .define("consumePotionProjectiles", true);

    private static final ForgeConfigSpec.BooleanValue BYPASS_DAMAGE_COOLDOWN = BUILDER
            .comment("Allow snow golem attacks to bypass the target's damage cooldown.")
            .define("bypassDamageCooldown", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableUpgrades = true;
    public static boolean consumeProjectileItems = false;
    public static boolean consumePotionProjectiles = true;
    public static boolean bypassDamageCooldown = true;

    private SuperSnowmenConfig() {
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        enableUpgrades = ENABLE_UPGRADES.get();
        consumeProjectileItems = CONSUME_PROJECTILE_ITEMS.get();
        consumePotionProjectiles = CONSUME_POTION_PROJECTILES.get();
        bypassDamageCooldown = BYPASS_DAMAGE_COOLDOWN.get();
    }

    public static void setEnableUpgrades(boolean value) {
        ENABLE_UPGRADES.set(value);
        ENABLE_UPGRADES.save();
        enableUpgrades = value;
    }

    public static void setConsumeProjectileItems(boolean value) {
        CONSUME_PROJECTILE_ITEMS.set(value);
        CONSUME_PROJECTILE_ITEMS.save();
        consumeProjectileItems = value;
    }

    public static void setConsumePotionProjectiles(boolean value) {
        CONSUME_POTION_PROJECTILES.set(value);
        CONSUME_POTION_PROJECTILES.save();
        consumePotionProjectiles = value;
    }

    public static void setBypassDamageCooldown(boolean value) {
        BYPASS_DAMAGE_COOLDOWN.set(value);
        BYPASS_DAMAGE_COOLDOWN.save();
        bypassDamageCooldown = value;
    }
}
