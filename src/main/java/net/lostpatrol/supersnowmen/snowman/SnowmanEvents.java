package net.lostpatrol.supersnowmen.snowman;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.lostpatrol.supersnowmen.menu.SnowmanUpgradeMenu;
import net.lostpatrol.supersnowmen.network.SuperSnowmenNetwork;
import net.lostpatrol.supersnowmen.projectile.ProjectileReplacement;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class SnowmanEvents {
    private static final Map<SnowGolem, Boolean> POWERED_STATES = new WeakHashMap<>();
    private static final Map<AreaEffectCloud, Boolean> SNOWMAN_POTION_CLOUDS = new WeakHashMap<>();
    private static final Map<Entity, DamageSegmentBatch> DAMAGE_SEGMENTS = new WeakHashMap<>();

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
        if (!event.getLevel().isClientSide()
                && event.getEntity() instanceof AreaEffectCloud cloud
                && cloud.getOwner() instanceof SnowGolem) {
            SNOWMAN_POTION_CLOUDS.put(cloud, true);
        }
        tagSnowmanChannelingLightning(event);
        ProjectileReplacement.replaceSnowball(event);
    }

    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player) {
            if (isSnowGolemFriendlyDamage(event.getSource())) {
                event.setCanceled(true);
            }
            return;
        }
        if (!(event.getEntity() instanceof SnowGolem snowman)) {
            return;
        }
        if (isSnowGolemFriendlyDamage(event.getSource())) {
            event.setCanceled(true);
            return;
        }
        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            SnowmanUpgradeEffects.ArmorTier tier = SnowmanUpgradeEffects.armorTier(inventory);
            boolean dragonBreathImmune = inventory.hasProjectileUpgrade(SnowmanUpgradeType.DRAGON_BREATH)
                    && isDragonBreathDamage(event);
            boolean explosionImmune = inventory.hasProjectileUpgrade(SnowmanUpgradeType.TNT)
                    && (event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.PLAYER_EXPLOSION));
            boolean witherEquipped = inventory.hasProjectileUpgrade(SnowmanUpgradeType.WITHER_SKULL);
            boolean poweredArrowShield = witherEquipped
                    && snowman.getHealth() <= snowman.getMaxHealth() / 2.0F
                    && event.getSource().getDirectEntity() instanceof AbstractArrow;
            if ((tier.climateImmune && event.getSource().is(DamageTypes.ON_FIRE) && !snowman.isOnFire())
                    || (tier.wetImmune && event.getSource().is(DamageTypes.DROWN))
                    || dragonBreathImmune
                    || explosionImmune
                    || (witherEquipped && event.getSource().is(DamageTypes.WITHER))
                    || poweredArrowShield) {
                event.setCanceled(true);
            }
        });
    }

    public static void onSnowmanDamageCooldown(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        if (!SuperSnowmenConfig.bypassDamageCooldown
                || target instanceof Player
                || (target instanceof SnowGolem && isSnowGolemFriendlyDamage(source))
                || target.level().isClientSide
                || target.isDeadOrDying()
                || target.isInvulnerableTo(source)
                || (source.is(DamageTypeTags.IS_FIRE) && target.hasEffect(MobEffects.FIRE_RESISTANCE))
                || !shouldBypassDamageCooldown(source, target)) {
            return;
        }
        target.invulnerableTime = 0;
    }

    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof SnowGolem snowman)) {
            return;
        }
        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            boolean rejectWither = event.getEffectInstance().getEffect() == MobEffects.WITHER
                    && inventory.hasProjectileUpgrade(SnowmanUpgradeType.WITHER_SKULL);
            boolean rejectLevitation = event.getEffectInstance().getEffect() == MobEffects.LEVITATION
                    && inventory.hasProjectileUpgrade(SnowmanUpgradeType.SHULKER_SHELL);
            if (rejectWither || rejectLevitation) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            }
        });
    }

    // Forge posts Added before put/update; restore previous effect after addEffect returns.
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        LivingEntity target = event.getEntity();
        boolean protectPlayer = target instanceof Player;
        boolean protectSnowman = target instanceof SnowGolem
                && event.getEffectInstance().getEffect().getCategory() == MobEffectCategory.HARMFUL;
        var server = target.getServer();
        if ((!protectPlayer && !protectSnowman)
                || target.level().isClientSide()
                || !isSnowGolemAttacker(event.getEffectSource())
                || server == null) {
            return;
        }
        MobEffect type = event.getEffectInstance().getEffect();
        MobEffectInstance previous = event.getOldEffectInstance();
        MobEffectInstance restore = previous == null ? null : new MobEffectInstance(previous);
        server.execute(() -> {
            if (!target.isAlive()) {
                return;
            }
            if (restore == null) {
                target.removeEffect(type);
            } else {
                target.removeEffect(type);
                target.addEffect(restore);
            }
        });
    }

    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player || event.getEntity() instanceof SnowGolem
                || !(event.getSource().getEntity() instanceof SnowGolem snowman)) {
            return;
        }
        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            int diamonds = inventory.getStackInSlot(SnowmanUpgradeInventory.BASE_DIAMOND_SLOT).getCount();
            if (diamonds > 0 && claimDiamondBonus(event.getSource(), event.getEntity())) {
                event.setAmount(event.getAmount() + diamonds);
            }
        });
    }

    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        if ((event.getEntity() instanceof Player || event.getEntity() instanceof SnowGolem)
                && event.getLightning().getPersistentData().getBoolean(ProjectileReplacement.SNOWMAN_LIGHTNING_TAG)) {
            event.setCanceled(true);
        }
    }

    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof SnowGolem snowman)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            int diamonds = inventory.getStackInSlot(SnowmanUpgradeInventory.BASE_DIAMOND_SLOT).getCount();
            if (diamonds > 0) {
                event.setAmount(CombatRules.getDamageAfterMagicAbsorb(event.getAmount(), diamonds));
            }
        });
    }

    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (handleSnowmanPotionImpact(event)) {
            return;
        }
        if (!(event.getProjectile() instanceof ThrownTrident trident)
                || !trident.getPersistentData().getBoolean(ProjectileReplacement.WEATHERPROOF_CHANNELING_TAG)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHit)
                || !(trident.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)
                || serverLevel.isThundering()) {
            return;
        }
        var hitPos = entityHit.getEntity().blockPosition();
        if (!serverLevel.canSeeSky(hitPos)) {
            return;
        }
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning != null) {
            lightning.moveTo(Vec3.atBottomCenterOf(hitPos));
            lightning.getPersistentData().putBoolean(ProjectileReplacement.SNOWMAN_LIGHTNING_TAG, true);
            serverLevel.addFreshEntity(lightning);
            trident.getPersistentData().remove(ProjectileReplacement.WEATHERPROOF_CHANNELING_TAG);
        }
    }

    // Non-DEFAULT impact skips onHit; re-splash only non-players then discard.
    private static boolean handleSnowmanPotionImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion)
                || potion.level().isClientSide()
                || !(potion.getOwner() instanceof SnowGolem)
                || potion.getItem().is(Items.LINGERING_POTION)
                || event.getRayTraceResult().getType() == HitResult.Type.MISS) {
            return false;
        }
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);

        ItemStack stack = potion.getItem();
        Potion potionType = PotionUtils.getPotion(stack);
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
        boolean water = potionType == Potions.WATER && effects.isEmpty();
        if (water) {
            applyWaterSplash(potion);
        } else if (!effects.isEmpty()) {
            Entity directHit = event.getRayTraceResult().getType() == HitResult.Type.ENTITY
                    ? ((EntityHitResult) event.getRayTraceResult()).getEntity()
                    : null;
            applySplashExcludingPlayers(potion, effects, directHit);
        }
        int particles = potionType.hasInstantEffects() ? 2007 : 2002;
        potion.level().levelEvent(particles, potion.blockPosition(), PotionUtils.getColor(stack));
        potion.discard();
        return true;
    }

    private static void applyWaterSplash(ThrownPotion potion) {
        AABB area = potion.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        for (LivingEntity living : potion.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> !(entity instanceof Player)
                        && (entity.isSensitiveToWater() || entity.isOnFire()))) {
            if (potion.distanceToSqr(living) >= 16.0D) {
                continue;
            }
            if (living.isSensitiveToWater()) {
                living.hurt(potion.damageSources().indirectMagic(potion, potion.getOwner()), 1.0F);
            }
            if (living.isOnFire() && living.isAlive()) {
                living.extinguishFire();
            }
        }
    }

    private static void applySplashExcludingPlayers(ThrownPotion potion, List<MobEffectInstance> effects,
                                                    @Nullable Entity directHit) {
        AABB area = potion.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        Entity effectSource = potion.getEffectSource();
        for (LivingEntity living : potion.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (living instanceof Player || !living.isAffectedByPotions()) {
                continue;
            }
            double distanceSqr = potion.distanceToSqr(living);
            if (distanceSqr >= 16.0D) {
                continue;
            }
            double intensity = living == directHit ? 1.0D : 1.0D - Math.sqrt(distanceSqr) / 4.0D;
            for (MobEffectInstance effect : effects) {
                MobEffect type = effect.getEffect();
                if (type.isInstantenous()) {
                    type.applyInstantenousEffect(potion, potion.getOwner(), living, effect.getAmplifier(), intensity);
                } else {
                    int duration = effect.mapDuration(base -> (int) (intensity * (double) base + 0.5D));
                    MobEffectInstance scaled = new MobEffectInstance(
                            type, duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible());
                    if (!scaled.endsWithin(20)) {
                        living.addEffect(scaled, effectSource);
                    }
                }
            }
        }
    }

    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof SnowGolem snowman)
                || !(snowman.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory ->
                SnowmanUpgradeEffects.maintainEffects(snowman, inventory));
        boolean powered = snowman.getHealth() <= snowman.getMaxHealth() / 2.0F
                && SnowmanUpgradeAccess.get(snowman)
                .map(inventory -> inventory.hasProjectileUpgrade(SnowmanUpgradeType.WITHER_SKULL))
                .orElse(false);
        boolean previous = POWERED_STATES.getOrDefault(snowman, false);
        if (powered != previous) {
            POWERED_STATES.put(snowman, powered);
            SuperSnowmenNetwork.sendPoweredState(snowman, powered);
        }
        if (!powered) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            if (snowman.getRandom().nextInt(4) == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.ENTITY_EFFECT,
                        snowman.getX() + snowman.getRandom().nextGaussian() * 0.3D,
                        snowman.getY() + snowman.getRandom().nextDouble() * snowman.getBbHeight(),
                        snowman.getZ() + snowman.getRandom().nextGaussian() * 0.3D,
                        0, 0.7D, 0.7D, 0.5D, 1.0D
                );
            }
        }
    }

    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START
                || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }
        SNOWMAN_POTION_CLOUDS.keySet().removeIf(Entity::isRemoved);
        for (AreaEffectCloud cloud : SNOWMAN_POTION_CLOUDS.keySet()) {
            if (cloud.level() != serverLevel) {
                continue;
            }
            for (ServerPlayer player : serverLevel.players()) {
                cloud.victims.put(player, Integer.MAX_VALUE);
            }
        }
    }

    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof SnowGolem snowman) {
            boolean powered = snowman.getHealth() <= snowman.getMaxHealth() / 2.0F
                    && SnowmanUpgradeAccess.get(snowman)
                    .map(inventory -> inventory.hasProjectileUpgrade(SnowmanUpgradeType.WITHER_SKULL))
                    .orElse(false);
            SuperSnowmenNetwork.sendPoweredState(player, snowman, powered);
        }
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
        if (isSnowGolemAttacker(event.getExplosion().getIndirectSourceEntity())
                || isSnowGolemAttacker(directSource)) {
            event.getAffectedEntities().removeIf(entity -> entity instanceof Player || entity instanceof SnowGolem);
        }
    }

    private static boolean isSnowGolemFriendlyDamage(DamageSource source) {
        if (isSnowGolemDamage(source)) {
            return true;
        }
        return source.getDirectEntity() instanceof LightningBolt lightning
                && lightning.getPersistentData().getBoolean(ProjectileReplacement.SNOWMAN_LIGHTNING_TAG);
    }

    private static boolean isSnowGolemDamage(DamageSource source) {
        return isSnowGolemAttacker(source.getEntity()) || isSnowGolemAttacker(source.getDirectEntity());
    }

    private static boolean shouldBypassDamageCooldown(DamageSource source, LivingEntity target) {
        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof AreaEffectCloud || directEntity instanceof LightningBolt || !isSnowGolemDamage(source)) {
            return false;
        }
        if (directEntity == null || directEntity instanceof SnowGolem) {
            return true;
        }
        long gameTime = target.level().getGameTime();
        DamageSegmentBatch batch = DAMAGE_SEGMENTS.computeIfAbsent(directEntity, ignored -> new DamageSegmentBatch());
        if (batch.gameTime != gameTime) {
            batch.gameTime = gameTime;
            batch.targets.clear();
        }
        return batch.targets.add(target.getUUID());
    }

    private static boolean claimDiamondBonus(DamageSource source, LivingEntity target) {
        Entity directEntity = source.getDirectEntity();
        if (directEntity == null || directEntity instanceof SnowGolem) {
            return true;
        }
        return DAMAGE_SEGMENTS.computeIfAbsent(directEntity, ignored -> new DamageSegmentBatch())
                .diamondTargets.add(target.getUUID());
    }

    private static boolean isSnowGolemAttacker(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof SnowGolem) {
            return true;
        }
        if (entity instanceof TraceableEntity traceable) {
            return traceable.getOwner() instanceof SnowGolem;
        }
        return false;
    }

    private static final class DamageSegmentBatch {
        private long gameTime = Long.MIN_VALUE;
        private final Set<UUID> targets = new HashSet<>();
        private final Set<UUID> diamondTargets = new HashSet<>();
    }

    // Tag lightning that appears next to a snow-golem-owned trident.
    private static void tagSnowmanChannelingLightning(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LightningBolt lightning)) {
            return;
        }
        if (lightning.getPersistentData().getBoolean(ProjectileReplacement.SNOWMAN_LIGHTNING_TAG)) {
            return;
        }
        Level level = event.getLevel();
        AABB search = lightning.getBoundingBox().inflate(3.0D);
        for (ThrownTrident trident : level.getEntitiesOfClass(ThrownTrident.class, search)) {
            if (trident.getOwner() instanceof SnowGolem) {
                lightning.getPersistentData().putBoolean(ProjectileReplacement.SNOWMAN_LIGHTNING_TAG, true);
                return;
            }
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
                                })))
                .then(Commands.literal("bypassDamageCooldown")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean value = BoolArgumentType.getBool(ctx, "value");
                                    SuperSnowmenConfig.setBypassDamageCooldown(value);
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.super_snowmen.bypass_damage_cooldown", value), true);
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
