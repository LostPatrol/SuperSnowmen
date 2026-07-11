package net.lostpatrol.supersnowmen.snowman;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.lostpatrol.supersnowmen.menu.SnowmanUpgradeMenu;
import net.lostpatrol.supersnowmen.projectile.ProjectileReplacement;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;

public final class SnowmanEvents {
    private SnowmanEvents() {
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof SnowGolem snowman) {
            event.addCapability(SnowmanUpgradeProvider.ID, new SnowmanUpgradeProvider(snowman));
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        handleSnowmanInteract(event, event.getTarget());
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        handleSnowmanInteract(event, event.getTarget());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getItemStack().is(Items.GLASS_BOTTLE)) {
            return;
        }
        boolean blockedCloudNearby = !event.getLevel().getEntitiesOfClass(
                AreaEffectCloud.class,
                event.getEntity().getBoundingBox().inflate(2.0D),
                cloud -> cloud.isAlive() && cloud.getPersistentData().getBoolean(ProjectileReplacement.NO_BOTTLE_DRAGON_BREATH_TAG)
        ).isEmpty();
        if (blockedCloudNearby) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        }
    }

    private static void handleSnowmanInteract(PlayerInteractEvent event, Entity target) {
        if (!SuperSnowmenConfig.enableUpgrades || !(target instanceof SnowGolem snowman)) {
            return;
        }
        Player player = event.getEntity();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            if (player.isShiftKeyDown()) {
                withdrawAll(serverPlayer, inventory);
                SnowmanUpgradeEffects.apply(snowman, inventory);
            } else {
                NetworkHooks.openScreen(
                        serverPlayer,
                        new SimpleMenuProvider(
                                (containerId, playerInventory, p) -> new SnowmanUpgradeMenu(containerId, playerInventory, snowman.getId()),
                                Component.translatable("container.super_snowmen.snowman_upgrade")
                        ),
                        buf -> buf.writeInt(snowman.getId())
                );
            }
        });
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof SnowGolem snowman) {
            installAttackGoal(snowman);
            SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> SnowmanUpgradeEffects.apply(snowman, inventory));
        } else if (event.getEntity() instanceof AreaEffectCloud cloud) {
            ProjectileReplacement.tagDragonBreathCloud(cloud);
        }
        ProjectileReplacement.replaceSnowball(event);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof SnowGolem snowman)) {
            return;
        }
        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            SnowmanUpgradeEffects.ArmorTier tier = SnowmanUpgradeEffects.armorTier(inventory);
            String damageId = event.getSource().typeHolder().unwrapKey().map(key -> key.location().toString()).orElse("");
            if ((tier.warmImmune && damageId.endsWith("on_fire"))
                    || (tier.wetImmune && damageId.endsWith("drown"))
                    || (tier.fireproof && (damageId.endsWith("in_fire") || damageId.endsWith("on_fire") || damageId.endsWith("lava") || damageId.endsWith("hot_floor")))) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Entity directSource = event.getExplosion().getDirectSourceEntity();
        if (directSource != null && directSource.getPersistentData().getBoolean(ProjectileReplacement.NO_BLOCK_DAMAGE_TAG)) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("supersnowmen")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("enable")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean value = BoolArgumentType.getBool(ctx, "value");
                                    SuperSnowmenConfig.setEnableUpgrades(value);
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.super_snowmen.enable", value), true);
                                    return 1;
                                })))
                .then(Commands.literal("consumeProjectiles")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean value = BoolArgumentType.getBool(ctx, "value");
                                    SuperSnowmenConfig.setConsumeProjectileItems(value);
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.super_snowmen.consume_projectiles", value), true);
                                    return 1;
                                })))
                .then(Commands.literal("consumePotionProjectiles")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean value = BoolArgumentType.getBool(ctx, "value");
                                    SuperSnowmenConfig.setConsumePotionProjectiles(value);
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.super_snowmen.consume_potion_projectiles", value), true);
                                    return 1;
                                }))));
    }

    private static void withdrawAll(ServerPlayer player, SnowmanUpgradeInventory inventory) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.extractItem(slot, 64, false);
            if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    private static void installAttackGoal(SnowGolem snowman) {
        boolean installed = snowman.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof SnowmanRangedAttackGoal);
        if (installed) {
            return;
        }
        snowman.goalSelector.removeAllGoals(goal -> goal instanceof RangedAttackGoal);
        snowman.goalSelector.addGoal(1, new SnowmanRangedAttackGoal(snowman, 1.25D, 20, 10.0F));
    }
}
