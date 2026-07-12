package net.lostpatrol.supersnowmen.config;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SuperSnowmen.MOD_ID)
public final class SuperSnowmenConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_UPGRADES = BUILDER
            .comment("Enable Super Snowmen upgrades.")
            .define("enableUpgrades", true);

    private static final ModConfigSpec.BooleanValue CONSUME_PROJECTILE_ITEMS = BUILDER
            .comment("Consume one matching upgrade item when a replacement projectile is fired.")
            .define("consumeProjectileItems", false);

    private static final ModConfigSpec.BooleanValue CONSUME_POTION_PROJECTILES = BUILDER
            .comment("Consume tipped arrows and potions when they are selected as replacement projectiles.")
            .define("consumePotionProjectiles", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableUpgrades = true;
    public static boolean consumeProjectileItems = false;
    public static boolean consumePotionProjectiles = true;

    private SuperSnowmenConfig() {
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        enableUpgrades = ENABLE_UPGRADES.get();
        consumeProjectileItems = CONSUME_PROJECTILE_ITEMS.get();
        consumePotionProjectiles = CONSUME_POTION_PROJECTILES.get();
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
}
