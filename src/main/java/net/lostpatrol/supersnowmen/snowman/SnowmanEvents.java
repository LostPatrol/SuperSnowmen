package net.lostpatrol.supersnowmen.snowman;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.lostpatrol.supersnowmen.menu.SnowmanUpgradeMenu;
import net.lostpatrol.supersnowmen.projectile.ProjectileReplacement;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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
            SnowmanUpgradeProvider provider = new SnowmanUpgradeProvider(snowman);
            event.addCapability(SnowmanUpgradeProvider.ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        handleSnowmanInteract(event, event.getTarget());
    }

    private static void handleSnowmanInteract(PlayerInteractEvent event, Entity target) {
        if (!SuperSnowmenConfig.enableUpgrades || !(target instanceof SnowGolem snowman)) {
            return;
        }
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            player.swing(event.getHand());
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (player instanceof ServerPlayer serverPlayer) {
            if (event.getItemStack().is(Items.SNOWBALL)) {
                healWithSnowball(serverPlayer, snowman, event.getItemStack());
                return;
            }
            handleSnowmanInteraction(serverPlayer, snowman, player.isShiftKeyDown());
        }
    }

    public static void handleSnowmanInteraction(ServerPlayer player, SnowGolem snowman, boolean withdraw) {
        if (!SuperSnowmenConfig.enableUpgrades || !snowman.isAlive() || player.distanceToSqr(snowman) > 64.0D) {
            SuperSnowmen.LOGGER.error("Snow golem interaction rejected: enabled={}, alive={}, distanceSquared={}", SuperSnowmenConfig.enableUpgrades, snowman.isAlive(), player.distanceToSqr(snowman));
            return;
        }

        var upgrades = SnowmanUpgradeAccess.get(snowman);
        if (upgrades.isEmpty()) {
            SuperSnowmen.LOGGER.error("Snow golem {} has no upgrade capability; cannot open upgrade menu", snowman.getId());
            return;
        }

        upgrades.ifPresent(inventory -> {
            if (withdraw) {
                withdrawAll(player, inventory);
                SnowmanUpgradeEffects.apply(snowman, inventory);
                return;
            }

            NetworkHooks.openScreen(
                    player,
                    new SimpleMenuProvider(
                            (containerId, playerInventory, p) -> new SnowmanUpgradeMenu(containerId, playerInventory, snowman.getId()),
                            Component.translatable("container.super_snowmen.snowman_upgrade")
                    ),
                    buf -> buf.writeInt(snowman.getId())
            );
        });
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof SnowGolem snowman) {
            installAttackGoal(snowman);
            SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> SnowmanUpgradeEffects.apply(snowman, inventory));
        }
        ProjectileReplacement.replaceSnowball(event);
    }

    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof SnowGolem snowman)) {
            return;
        }
        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            SnowmanUpgradeEffects.ArmorTier tier = SnowmanUpgradeEffects.armorTier(inventory);
            boolean dragonBreathImmune = inventory.hasProjectileUpgrade(SnowmanUpgradeType.DRAGON_BREATH)
                    && isDragonBreathDamage(event);
            boolean explosionImmune = inventory.hasProjectileUpgrade(SnowmanUpgradeType.TNT)
                    && (event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.PLAYER_EXPLOSION));
            if ((tier.warmImmune && event.getSource().is(DamageTypes.ON_FIRE))
                    || (tier.wetImmune && event.getSource().is(DamageTypes.DROWN))
                    || (tier.fireproof && (event.getSource().is(DamageTypes.IN_FIRE)
                    || event.getSource().is(DamageTypes.ON_FIRE)
                    || event.getSource().is(DamageTypes.LAVA)
                    || event.getSource().is(DamageTypes.HOT_FLOOR)))
                    || dragonBreathImmune
                    || explosionImmune) {
                event.setCanceled(true);
            }
        });
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof SnowGolem snowman) || snowman.level().isClientSide()) {
            return;
        }
        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot).copy();
                if (stack.isEmpty()) {
                    continue;
                }
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                ItemEntity drop = new ItemEntity(snowman.level(), snowman.getX(), snowman.getY(), snowman.getZ(), stack);
                drop.setDefaultPickUpDelay();
                event.getDrops().add(drop);
            }
        });
    }

    private static boolean isDragonBreathDamage(LivingAttackEvent event) {
        if (event.getSource().is(DamageTypes.DRAGON_BREATH)) {
            return true;
        }
        return event.getSource().getDirectEntity() instanceof AreaEffectCloud cloud
                && cloud.getParticle() == ParticleTypes.DRAGON_BREATH;
    }

    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Entity directSource = event.getExplosion().getDirectSourceEntity();
        if (directSource != null && directSource.getPersistentData().getBoolean(ProjectileReplacement.NO_BLOCK_DAMAGE_TAG)) {
            event.getAffectedBlocks().clear();
        }
    }

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

    private static void healWithSnowball(ServerPlayer player, SnowGolem snowman, ItemStack snowballs) {
        if (snowman.getHealth() >= snowman.getMaxHealth()) {
            return;
        }
        snowman.heal(10.0F);
        if (!player.getAbilities().instabuild) {
            snowballs.shrink(1);
        }
        player.level().playSound(null, snowman.blockPosition(), SoundEvents.SNOW_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.serverLevel().sendParticles(ParticleTypes.HEART, snowman.getX(), snowman.getY() + snowman.getBbHeight(), snowman.getZ(), 5, 0.25D, 0.25D, 0.25D, 0.0D);
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
