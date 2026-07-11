package net.lostpatrol.supersnowmen.snowman;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.lostpatrol.supersnowmen.menu.SnowmanUpgradeMenu;
import net.lostpatrol.supersnowmen.network.NetworkHandler;
import net.lostpatrol.supersnowmen.network.packet.PacketOpenSnowmanUpgrade;
import net.lostpatrol.supersnowmen.projectile.ProjectileReplacement;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
            NetworkHandler.sendOpenSnowmanUpgradeToServer(new PacketOpenSnowmanUpgrade(snowman.getId(), player.isShiftKeyDown()));
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (player instanceof ServerPlayer serverPlayer) {
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
