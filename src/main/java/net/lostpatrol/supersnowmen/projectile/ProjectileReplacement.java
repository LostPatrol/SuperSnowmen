package net.lostpatrol.supersnowmen.projectile;

import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeAccess;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeInventory;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProjectileReplacement {
    public static final String NO_BLOCK_DAMAGE_TAG = "SuperSnowmenNoBlockDamage";
    public static final String WEATHERPROOF_CHANNELING_TAG = "SuperSnowmenWeatherproofChanneling";
    private static final String WITHER_COUNTER_TAG = "SuperSnowmenWitherCounter";
    private static final DyeColor[] FIREWORK_COLORS = {
            DyeColor.WHITE, DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.GRAY,
            DyeColor.LIGHT_GRAY, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE,
            DyeColor.GREEN, DyeColor.RED
    };

    private ProjectileReplacement() {
    }

    public static void replaceSnowball(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Snowball snowball)) {
            return;
        }
        Entity owner = snowball.getOwner();
        if (!(owner instanceof SnowGolem snowman)) {
            return;
        }
        if (!SuperSnowmenConfig.enableUpgrades) {
            playSnowballSound(snowman);
            return;
        }

        SnowmanUpgradeInventory inventory = SnowmanUpgradeAccess.get(snowman).orElse(null);
        if (inventory == null) {
            playSnowballSound(snowman);
            return;
        }
        SelectedUpgrade selected = select(inventory, snowman);
        if (selected == null) {
            playSnowballSound(snowman);
            return;
        }
        Entity replacement = createReplacement(selected, snowball, snowman);
        boolean directEffect = selected.type == SnowmanUpgradeType.SCULK_SHRIEKER
                || selected.type == SnowmanUpgradeType.TOTEM;
        if (replacement == null && !directEffect) {
            playSnowballSound(snowman);
            return;
        }
        event.setCanceled(true);
        snowball.discard();
        if (replacement != null) {
            event.getLevel().addFreshEntity(replacement);
        }
        playProjectileSound(snowman, selected);
        consumeIfNeeded(inventory, selected);
    }

    public static void performRangedAttack(SnowGolem snowman, LivingEntity target) {
        Snowball snowball = new Snowball(snowman.level(), snowman);
        double targetY = target.getEyeY() - 1.1D;
        double x = target.getX() - snowman.getX();
        double y = targetY - snowball.getY();
        double z = target.getZ() - snowman.getZ();
        double arc = Math.sqrt(x * x + z * z) * 0.2D;
        snowball.shoot(x, y + arc, z, 1.6F, 12.0F);
        snowman.level().addFreshEntity(snowball);
    }

    private static SelectedUpgrade select(SnowmanUpgradeInventory inventory, SnowGolem snowman) {
        List<SelectedUpgrade> upgrades = new ArrayList<>();
        ItemStack linkedTrident = inventory.findPreferredTrident();
        ItemStack bow = inventory.findBow();
        for (int slot = SnowmanUpgradeInventory.PLUGIN_START; slot < SnowmanUpgradeInventory.PLUGIN_START + SnowmanUpgradeInventory.PLUGIN_COUNT; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            SnowmanUpgradeType type = SnowmanUpgradeType.byItem(stack.getItem());
            if (type != null && !stack.isEmpty()) {
                if (type == SnowmanUpgradeType.LIGHTNING_ROD && !linkedTrident.isEmpty()) {
                    upgrades.add(new SelectedUpgrade(slot, SnowmanUpgradeType.TRIDENT, linkedTrident.copy(), false, ItemStack.EMPTY));
                } else if (type == SnowmanUpgradeType.BOW) {
                    upgrades.add(new SelectedUpgrade(slot, type, stack.copy(), false, stack.copy()));
                } else {
                    ItemStack arrowBow = isArrowType(type) && !bow.isEmpty() ? bow.copy() : ItemStack.EMPTY;
                    upgrades.add(new SelectedUpgrade(slot, type, stack.copy(), true, arrowBow));
                }
            }
        }
        if (upgrades.isEmpty()) {
            return null;
        }

        int roll = snowman.getRandom().nextInt(SnowmanUpgradeInventory.PLUGIN_COUNT);
        return roll < upgrades.size() ? upgrades.get(roll) : null;
    }

    private static Entity createReplacement(SelectedUpgrade selected, Snowball snowball, SnowGolem owner) {
        Vec3 velocity = snowball.getDeltaMovement();
        Vec3 direction = velocity.lengthSqr() > 0.0001D ? velocity.normalize() : owner.getLookAngle();
        Vec3 directDirection = directionToTarget(snowball, owner.getTarget(), direction);
        return switch (selected.type) {
            case ARROW -> shootSelectedArrow(new Arrow(owner.level(), owner), snowball, velocity, selected.bow);
            case SPECTRAL_ARROW -> shootSelectedArrow(new SpectralArrow(owner.level(), owner), snowball, velocity, selected.bow);
            case FIRE_CHARGE -> createSmallFireball(snowball, owner, directDirection);
            case FIREWORK_ROCKET -> createFireworkRocket(selected.stack, snowball, owner, directDirection, velocity.length());
            case DRAGON_BREATH -> createDragonFireball(snowball, owner, directDirection);
            case TNT -> createTnt(snowball, owner, velocity);
            case WITHER_SKULL -> createWitherSkull(snowball, owner, directDirection);
            case SCULK_SHRIEKER -> {
                sonicBoom(owner);
                yield null;
            }
            case TOTEM -> {
                createFangLine(owner, directDirection);
                yield null;
            }
            case TIPPED_ARROW -> createTippedArrow(selected.stack, selected.bow, snowball, owner, velocity);
            case POTION, SPLASH_POTION -> createPotion(selected.stack, snowball, owner, velocity);
            case EGG -> copyMotion(new ThrownEgg(owner.level(), owner), snowball, velocity);
            case TRIDENT -> shootArrow(createTrident(selected.stack, owner), snowball, velocity);
            case SHULKER_SHELL -> createShulkerBullet(owner);
            case LIGHTNING_ROD -> null;
            case BOW -> shootBowArrow(new Arrow(owner.level(), owner), snowball, velocity, selected.bow);
        };
    }

    private static boolean isArrowType(SnowmanUpgradeType type) {
        return type == SnowmanUpgradeType.ARROW
                || type == SnowmanUpgradeType.SPECTRAL_ARROW
                || type == SnowmanUpgradeType.TIPPED_ARROW;
    }

    private static AbstractArrow shootSelectedArrow(AbstractArrow arrow, Snowball snowball, Vec3 velocity, ItemStack bow) {
        return bow.isEmpty() ? shootArrow(arrow, snowball, velocity) : shootBowArrow(arrow, snowball, velocity, bow);
    }

    private static AbstractArrow shootArrow(AbstractArrow arrow, Snowball snowball, Vec3 velocity) {
        arrow.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        if (velocity.lengthSqr() > 0.0001D) {
            arrow.shoot(velocity.x, velocity.y, velocity.z, 1.6F, 0.0F);
        }
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        return arrow;
    }

    private static AbstractArrow shootBowArrow(AbstractArrow arrow, Snowball snowball, Vec3 velocity, ItemStack bow) {
        arrow.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        if (velocity.lengthSqr() > 0.0001D) {
            arrow.shoot(velocity.x, velocity.y, velocity.z, 3.0F, 1.0F);
        }
        arrow.setCritArrow(true);
        int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
        if (power > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + power * 0.5D + 0.5D);
        }
        int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
        if (punch > 0) {
            arrow.setKnockback(punch);
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
            arrow.setSecondsOnFire(100);
        }
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        return arrow;
    }

    private static Entity createTnt(Snowball snowball, SnowGolem owner, Vec3 velocity) {
        PrimedTnt tnt = new PrimedTnt(owner.level(), snowball.getX(), snowball.getY(), snowball.getZ(), owner);
        tnt.setDeltaMovement(velocity);
        tnt.setFuse(40);
        tnt.getPersistentData().putBoolean(NO_BLOCK_DAMAGE_TAG, true);
        return tnt;
    }

    private static Entity createSmallFireball(Snowball snowball, SnowGolem owner, Vec3 direction) {
        LivingEntity target = owner.getTarget();
        Vec3 aim = direction;
        if (target != null) {
            double distanceSqr = owner.distanceToSqr(target);
            double spread = Math.sqrt(Math.sqrt(distanceSqr)) * 0.5D;
            aim = new Vec3(
                    owner.getRandom().triangle(target.getX() - owner.getX(), 2.297D * spread),
                    target.getY(0.5D) - owner.getY(0.5D),
                    owner.getRandom().triangle(target.getZ() - owner.getZ(), 2.297D * spread)
            );
        }
        SmallFireball fireball = new SmallFireball(owner.level(), owner, aim.x, aim.y, aim.z);
        fireball.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        return fireball;
    }

    private static Entity createDragonFireball(Snowball snowball, SnowGolem owner, Vec3 direction) {
        DragonFireball fireball = new DragonFireball(owner.level(), owner, direction.x, direction.y, direction.z);
        fireball.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        return fireball;
    }

    private static Entity createFireworkRocket(ItemStack source, Snowball snowball, SnowGolem owner, Vec3 direction, double speed) {
        ItemStack rocketItem = source.copy();
        rocketItem.setCount(1);
        CompoundTag fireworks = rocketItem.getOrCreateTagElement("Fireworks");
        ListTag explosions = fireworks.getList("Explosions", 10);
        while (explosions.size() < 3) {
            explosions.add(new CompoundTag());
        }
        for (int i = 0; i < explosions.size(); i++) {
            randomizeFireworkExplosion(explosions.getCompound(i), owner.getRandom());
        }
        fireworks.putByte("Flight", (byte)1);
        fireworks.put("Explosions", explosions);

        FireworkRocketEntity rocket = new FireworkRocketEntity(
                owner.level(), rocketItem, owner, snowball.getX(), snowball.getY(), snowball.getZ(), true
        );
        rocket.setDeltaMovement(direction.normalize().scale(Math.max(1.0D, speed)));
        return rocket;
    }

    private static void randomizeFireworkExplosion(CompoundTag explosion, RandomSource random) {
        FireworkRocketItem.Shape shape = switch (random.nextInt(5)) {
            case 1 -> FireworkRocketItem.Shape.LARGE_BALL;
            case 2 -> FireworkRocketItem.Shape.STAR;
            case 3 -> FireworkRocketItem.Shape.CREEPER;
            case 4 -> FireworkRocketItem.Shape.BURST;
            default -> FireworkRocketItem.Shape.SMALL_BALL;
        };
        shape.save(explosion);
        explosion.putIntArray("Colors", randomPrimaryFireworkColors(random));
        explosion.putIntArray("FadeColors", randomFadeFireworkColors(random));

        int effectRoll = random.nextInt(100);
        explosion.putBoolean("Trail", effectRoll >= 70);
        explosion.putBoolean("Flicker", (effectRoll >= 65 && effectRoll < 70) || effectRoll >= 95);
    }

    private static int[] randomPrimaryFireworkColors(RandomSource random) {
        int count = random.nextInt(3);
        if (count == 0) {
            return new int[]{DyeColor.WHITE.getFireworkColor()};
        }
        boolean[] selected = new boolean[FIREWORK_COLORS.length];
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            int index = randomWeightedPrimaryColor(random, selected);
            selected[index] = true;
            colors[i] = FIREWORK_COLORS[index].getFireworkColor();
        }
        return colors;
    }

    private static int randomWeightedPrimaryColor(RandomSource random, boolean[] selected) {
        int totalWeight = 0;
        for (int i = 0; i < FIREWORK_COLORS.length; i++) {
            if (!selected[i]) {
                totalWeight += FIREWORK_COLORS[i] == DyeColor.WHITE ? 3 : 1;
            }
        }
        int roll = random.nextInt(totalWeight);
        for (int i = 0; i < FIREWORK_COLORS.length; i++) {
            if (selected[i]) {
                continue;
            }
            roll -= FIREWORK_COLORS[i] == DyeColor.WHITE ? 3 : 1;
            if (roll < 0) {
                return i;
            }
        }
        return 0;
    }

    private static int[] randomFadeFireworkColors(RandomSource random) {
        if (!random.nextBoolean()) {
            return new int[0];
        }
        DyeColor color = FIREWORK_COLORS[random.nextInt(FIREWORK_COLORS.length)];
        return new int[]{color.getFireworkColor()};
    }

    private static ThrownTrident createTrident(ItemStack source, SnowGolem owner) {
        ItemStack trident = source.copy();
        trident.setCount(1);
        Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(trident));
        enchantments.remove(Enchantments.LOYALTY);
        enchantments.remove(Enchantments.RIPTIDE);
        EnchantmentHelper.setEnchantments(enchantments, trident);
        ThrownTrident thrown = new ThrownTrident(owner.level(), owner, trident);
        boolean lightningRodEquipped = SnowmanUpgradeAccess.get(owner)
                .map(inventory -> inventory.hasProjectileUpgrade(SnowmanUpgradeType.LIGHTNING_ROD))
                .orElse(false);
        if (lightningRodEquipped
                && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.CHANNELING, trident) > 0) {
            thrown.getPersistentData().putBoolean(WEATHERPROOF_CHANNELING_TAG, true);
        }
        return thrown;
    }

    private static Entity createWitherSkull(Snowball snowball, SnowGolem owner, Vec3 fallbackDirection) {
        WitherSkull skull = new WitherSkull(owner.level(), owner, fallbackDirection.x, fallbackDirection.y, fallbackDirection.z);
        skull.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        int counter = owner.getPersistentData().getInt(WITHER_COUNTER_TAG) + 1;
        owner.getPersistentData().putInt(WITHER_COUNTER_TAG, counter);
        skull.setDangerous(counter % 4 == 0);
        skull.getPersistentData().putBoolean(NO_BLOCK_DAMAGE_TAG, true);
        return skull;
    }

    private static Entity createTippedArrow(ItemStack source, ItemStack bow, Snowball snowball, SnowGolem owner, Vec3 velocity) {
        Arrow arrow = new Arrow(owner.level(), owner);
        arrow.setEffectsFromItem(source);
        return shootSelectedArrow(arrow, snowball, velocity, bow);
    }

    private static Entity createPotion(ItemStack source, Snowball snowball, SnowGolem owner, Vec3 velocity) {
        ItemStack splash = new ItemStack(Items.SPLASH_POTION);
        PotionUtils.setPotion(splash, PotionUtils.getPotion(source));
        PotionUtils.setCustomEffects(splash, PotionUtils.getCustomEffects(source));
        ThrownPotion potion = new ThrownPotion(owner.level(), owner);
        potion.setItem(splash);
        return copyMotion(potion, snowball, velocity);
    }

    private static Entity createShulkerBullet(SnowGolem owner) {
        LivingEntity target = owner.getTarget();
        if (target != null) {
            return new ShulkerBullet(owner.level(), owner, target, Direction.Axis.Y);
        }
        return null;
    }

    private static void createFangLine(SnowGolem owner, Vec3 direction) {
        LivingEntity target = owner.getTarget();
        double minY = target == null ? owner.getY() : Math.min(target.getY(), owner.getY());
        double maxY = target == null ? owner.getY() + 1.0D : Math.max(target.getY(), owner.getY()) + 1.0D;
        float angle = (float)Mth.atan2(direction.z, direction.x);
        for (int index = 0; index < 16; index++) {
            double distance = 1.25D * (index + 1);
            createFang(
                    owner,
                    owner.getX() + Mth.cos(angle) * distance,
                    owner.getZ() + Mth.sin(angle) * distance,
                    minY,
                    maxY,
                    angle,
                    index
            );
        }
    }

    private static void createFang(SnowGolem owner, double x, double z, double minY, double maxY, float angle, int delay) {
        BlockPos pos = BlockPos.containing(x, maxY, z);
        boolean foundGround = false;
        double collisionHeight = 0.0D;
        do {
            BlockPos below = pos.below();
            BlockState ground = owner.level().getBlockState(below);
            if (ground.isFaceSturdy(owner.level(), below, Direction.UP)) {
                if (!owner.level().isEmptyBlock(pos)) {
                    VoxelShape collision = owner.level().getBlockState(pos).getCollisionShape(owner.level(), pos);
                    if (!collision.isEmpty()) {
                        collisionHeight = collision.max(Direction.Axis.Y);
                    }
                }
                foundGround = true;
                break;
            }
            pos = pos.below();
        } while (pos.getY() >= Mth.floor(minY) - 1);

        if (foundGround) {
            owner.level().addFreshEntity(new EvokerFangs(
                    owner.level(), x, pos.getY() + collisionHeight, z, angle, delay, owner
            ));
        }
    }

    private static Entity copyMotion(Entity entity, Snowball snowball, Vec3 velocity) {
        entity.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        entity.setDeltaMovement(velocity);
        if (entity instanceof Projectile projectile) {
            projectile.setOwner(snowball.getOwner());
        }
        return entity;
    }

    private static void sonicBoom(SnowGolem owner) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity target = owner.getTarget();
        if (target == null) {
            return;
        }
        Vec3 start = owner.position().add(0.0D, 1.6D, 0.0D);
        Vec3 toTarget = target.getEyePosition().subtract(start);
        Vec3 normalized = toTarget.normalize();
        for (int i = 1; i < Mth.floor(toTarget.length()) + 7; i++) {
            Vec3 point = start.add(normalized.scale(i));
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        owner.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        target.hurt(serverLevel.damageSources().sonicBoom(owner), 10.0F);
        double vertical = 0.5D * (1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        double horizontal = 2.5D * (1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        target.push(normalized.x * horizontal, normalized.y * vertical, normalized.z * horizontal);

        Vec3 end = target.getEyePosition();
        AABB searchArea = new AABB(start, end).inflate(2.0D);
        for (Monster monster : serverLevel.getEntitiesOfClass(Monster.class, searchArea,
                candidate -> candidate != target && candidate.isAlive())) {
            double hitRadius = 1.25D + monster.getBbWidth() * 0.5D;
            Vec3 center = monster.position().add(0.0D, monster.getBbHeight() * 0.5D, 0.0D);
            if (distanceToSegmentSqr(center, start, end) <= hitRadius * hitRadius) {
                monster.hurt(serverLevel.damageSources().sonicBoom(owner), 10.0F);
            }
        }
    }

    private static double distanceToSegmentSqr(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSqr = segment.lengthSqr();
        if (lengthSqr < 1.0E-7D) {
            return point.distanceToSqr(start);
        }
        double progress = Mth.clamp(point.subtract(start).dot(segment) / lengthSqr, 0.0D, 1.0D);
        return point.distanceToSqr(start.add(segment.scale(progress)));
    }

    private static Vec3 directionToTarget(Snowball snowball, LivingEntity target, Vec3 fallback) {
        if (target == null) {
            return fallback.normalize();
        }
        return new Vec3(
                target.getX() - snowball.getX(),
                target.getY() + target.getEyeHeight() * 0.5D - snowball.getY(),
                target.getZ() - snowball.getZ()
        ).normalize();
    }

    private static void playSnowballSound(SnowGolem owner) {
        owner.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (owner.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private static void playProjectileSound(SnowGolem owner, SelectedUpgrade selected) {
        SnowmanUpgradeType type = selected.type;
        if (type == SnowmanUpgradeType.BOW || isArrowType(type) && !selected.bow.isEmpty()) {
            owner.level().playSound(null, owner.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0F, 1.0F / (owner.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
            return;
        }
        if (type == SnowmanUpgradeType.SHULKER_SHELL) {
            owner.level().playSound(
                    null,
                    owner.blockPosition(),
                    SoundEvents.SHULKER_SHOOT,
                    SoundSource.HOSTILE,
                    2.0F,
                    (owner.getRandom().nextFloat() - owner.getRandom().nextFloat()) * 0.2F + 1.0F
            );
            return;
        }
        SoundEvent sound = switch (type) {
            case ARROW, SPECTRAL_ARROW, TIPPED_ARROW -> SoundEvents.SKELETON_SHOOT;
            case FIRE_CHARGE -> SoundEvents.BLAZE_SHOOT;
            case DRAGON_BREATH -> SoundEvents.ENDER_DRAGON_SHOOT;
            case TNT -> SoundEvents.TNT_PRIMED;
            case WITHER_SKULL -> SoundEvents.WITHER_SHOOT;
            case SCULK_SHRIEKER -> null;
            case TOTEM -> SoundEvents.EVOKER_CAST_SPELL;
            case POTION, SPLASH_POTION -> SoundEvents.WITCH_THROW;
            case EGG -> SoundEvents.EGG_THROW;
            case TRIDENT -> SoundEvents.TRIDENT_THROW;
            case SHULKER_SHELL -> null;
            case FIREWORK_ROCKET -> null;
            case LIGHTNING_ROD -> null;
            case BOW -> null;
        };
        if (sound != null) {
            owner.level().playSound(null, owner.blockPosition(), sound, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    private static void consumeIfNeeded(SnowmanUpgradeInventory inventory, SelectedUpgrade selected) {
        if (!selected.consumeItem) {
            return;
        }
        boolean potionLike = selected.type == SnowmanUpgradeType.TIPPED_ARROW
                || selected.type == SnowmanUpgradeType.POTION
                || selected.type == SnowmanUpgradeType.SPLASH_POTION;
        if (SuperSnowmenConfig.consumeProjectileItems || (potionLike && SuperSnowmenConfig.consumePotionProjectiles)) {
            inventory.extractItem(selected.slot, 1, false);
        }
    }

    private record SelectedUpgrade(int slot, SnowmanUpgradeType type, ItemStack stack, boolean consumeItem,
                                   ItemStack bow) {
    }

}
