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
import net.minecraft.world.entity.projectile.LargeFireball;
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
    public static final String SNOWMAN_LIGHTNING_TAG = "SuperSnowmenSnowmanLightning";
    private static final String WITHER_COUNTER_TAG = "SuperSnowmenWitherCounter";
    private static final double FULL_DRAW_ARROW_SPEED = 3.0D;
    private static final double ARROW_AIR_INERTIA = 0.99D;
    private static final double ARROW_GRAVITY = 0.05D;
    private static final double TNT_AIR_INERTIA = 0.98D;
    private static final double TNT_GRAVITY = 0.04D;
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
                || selected.type == SnowmanUpgradeType.TOTEM
                || selected.type == SnowmanUpgradeType.POTION;
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
        ItemStack crossbow = inventory.findCrossbow();
        ItemStack linkedFirework = inventory.findFirstProjectileUpgrade(SnowmanUpgradeType.FIREWORK_ROCKET);
        for (int slot = SnowmanUpgradeInventory.PLUGIN_START; slot < SnowmanUpgradeInventory.PLUGIN_START + SnowmanUpgradeInventory.PLUGIN_COUNT; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            SnowmanUpgradeType type = SnowmanUpgradeType.byItem(stack.getItem());
            if (type != null && !stack.isEmpty()) {
                if (type == SnowmanUpgradeType.LIGHTNING_ROD && !linkedTrident.isEmpty()) {
                    upgrades.add(new SelectedUpgrade(slot, SnowmanUpgradeType.TRIDENT, linkedTrident.copy(), false,
                            ItemStack.EMPTY, ItemStack.EMPTY));
                } else if (type == SnowmanUpgradeType.BOW) {
                    upgrades.add(new SelectedUpgrade(slot, type, stack.copy(), false, stack.copy(), ItemStack.EMPTY));
                } else if (type == SnowmanUpgradeType.CROSSBOW) {
                    if (!linkedFirework.isEmpty()) {
                        upgrades.add(new SelectedUpgrade(slot, SnowmanUpgradeType.FIREWORK_ROCKET,
                                linkedFirework.copy(), false, ItemStack.EMPTY, stack.copy()));
                    }
                } else {
                    ItemStack arrowBow = isArrowType(type) && !bow.isEmpty() ? bow.copy() : ItemStack.EMPTY;
                    ItemStack fireworkCrossbow = type == SnowmanUpgradeType.FIREWORK_ROCKET && !crossbow.isEmpty()
                            ? crossbow.copy() : ItemStack.EMPTY;
                    upgrades.add(new SelectedUpgrade(slot, type, stack.copy(), true, arrowBow, fireworkCrossbow));
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
            case ARROW -> shootSelectedArrow(new Arrow(owner.level(), owner), snowball, owner, velocity, selected.bow);
            case SPECTRAL_ARROW -> shootSelectedArrow(new SpectralArrow(owner.level(), owner), snowball, owner, velocity, selected.bow);
            case FIRE_CHARGE -> createSmallFireball(snowball, owner, directDirection);
            case GHAST_TEAR -> createGhastFireball(snowball, owner, directDirection);
            case FIREWORK_ROCKET -> createFireworkRocket(selected.stack, selected.crossbow, snowball, owner,
                    directDirection, velocity.length());
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
            case POTION -> {
                drinkPotion(selected.stack, owner);
                yield null;
            }
            case SPLASH_POTION, LINGERING_POTION -> createThrownPotion(selected.stack, snowball, owner, velocity);
            case EGG -> copyMotion(new ThrownEgg(owner.level(), owner), snowball, velocity);
            case TRIDENT -> shootArrow(createTrident(selected.stack, owner), snowball, velocity);
            case SHULKER_SHELL -> createShulkerBullet(owner);
            case LIGHTNING_ROD -> null;
            case BOW -> shootBowArrow(new Arrow(owner.level(), owner), snowball, owner, velocity, selected.bow);
            case CROSSBOW -> null;
        };
    }

    private static boolean isArrowType(SnowmanUpgradeType type) {
        return type == SnowmanUpgradeType.ARROW
                || type == SnowmanUpgradeType.SPECTRAL_ARROW
                || type == SnowmanUpgradeType.TIPPED_ARROW;
    }

    private static AbstractArrow shootSelectedArrow(AbstractArrow arrow, Snowball snowball, SnowGolem owner,
                                                     Vec3 velocity, ItemStack bow) {
        return bow.isEmpty() ? shootArrow(arrow, snowball, velocity) : shootBowArrow(arrow, snowball, owner, velocity, bow);
    }

    private static AbstractArrow shootArrow(AbstractArrow arrow, Snowball snowball, Vec3 velocity) {
        arrow.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        if (velocity.lengthSqr() > 0.0001D) {
            arrow.shoot(velocity.x, velocity.y, velocity.z, 1.6F, 0.0F);
        }
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        return arrow;
    }

    private static AbstractArrow shootBowArrow(AbstractArrow arrow, Snowball snowball, SnowGolem owner,
                                                Vec3 fallbackVelocity, ItemStack bow) {
        arrow.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        LivingEntity target = owner.getTarget();
        if (target != null) {
            Vec3 targetCenter = target.getBoundingBox().getCenter();
            double horizontalSpread = target.getBbWidth() * 0.18D;
            double verticalSpread = target.getBbHeight() * 0.18D;
            Vec3 targetPoint = targetCenter.add(
                    owner.getRandom().triangle(0.0D, horizontalSpread),
                    owner.getRandom().triangle(0.0D, verticalSpread),
                    owner.getRandom().triangle(0.0D, horizontalSpread)
            );
            Vec3 aim = calculateBowTrajectory(snowball.position(), targetPoint);
            arrow.shoot(aim.x, aim.y, aim.z, (float)FULL_DRAW_ARROW_SPEED, 0.0F);
        } else if (fallbackVelocity.lengthSqr() > 0.0001D) {
            arrow.shoot(fallbackVelocity.x, fallbackVelocity.y, fallbackVelocity.z,
                    (float)FULL_DRAW_ARROW_SPEED, 0.0F);
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

    private static Vec3 calculateBowTrajectory(Vec3 origin, Vec3 target) {
        Vec3 delta = target.subtract(origin);
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontalDistance < 1.0E-4D) {
            return delta.normalize();
        }

        double lowAngle = -Math.PI / 3.0D;
        double highAngle = Math.PI / 3.0D;
        double lowHeight = arrowHeightAtDistance(lowAngle, horizontalDistance);
        double highHeight = arrowHeightAtDistance(highAngle, horizontalDistance);
        if (!Double.isFinite(lowHeight) || !Double.isFinite(highHeight)
                || delta.y < lowHeight || delta.y > highHeight) {
            return delta.normalize();
        }

        for (int iteration = 0; iteration < 32; iteration++) {
            double angle = (lowAngle + highAngle) * 0.5D;
            if (arrowHeightAtDistance(angle, horizontalDistance) < delta.y) {
                lowAngle = angle;
            } else {
                highAngle = angle;
            }
        }

        double angle = (lowAngle + highAngle) * 0.5D;
        double horizontalScale = Math.cos(angle) / horizontalDistance;
        return new Vec3(delta.x * horizontalScale, Math.sin(angle), delta.z * horizontalScale);
    }

    private static double arrowHeightAtDistance(double angle, double targetDistance) {
        double horizontalVelocity = Math.cos(angle) * FULL_DRAW_ARROW_SPEED;
        double verticalVelocity = Math.sin(angle) * FULL_DRAW_ARROW_SPEED;
        double horizontalPosition = 0.0D;
        double verticalPosition = 0.0D;
        for (int tick = 0; tick < 200; tick++) {
            double nextHorizontal = horizontalPosition + horizontalVelocity;
            double nextVertical = verticalPosition + verticalVelocity;
            if (nextHorizontal >= targetDistance) {
                double partialTick = (targetDistance - horizontalPosition) / horizontalVelocity;
                return Mth.lerp(partialTick, verticalPosition, nextVertical);
            }
            horizontalPosition = nextHorizontal;
            verticalPosition = nextVertical;
            horizontalVelocity *= ARROW_AIR_INERTIA;
            verticalVelocity = verticalVelocity * ARROW_AIR_INERTIA - ARROW_GRAVITY;
        }
        return Double.NaN;
    }

    private static Entity createTnt(Snowball snowball, SnowGolem owner, Vec3 velocity) {
        PrimedTnt tnt = new PrimedTnt(owner.level(), snowball.getX(), snowball.getY(), snowball.getZ(), owner);
        LivingEntity target = owner.getTarget();
        if (target != null) {
            Vec3 origin = snowball.position();
            Vec3 horizontalOffset = new Vec3(target.getX() - origin.x, 0.0D, target.getZ() - origin.z);
            double horizontalDistance = horizontalOffset.length();
            double stopShort = target.getBbWidth() * 0.5D + 0.4D;
            double landingDistance = Math.max(0.0D, horizontalDistance - stopShort);
            Vec3 landingPoint = horizontalDistance > 1.0E-4D
                    ? origin.add(horizontalOffset.scale(landingDistance / horizontalDistance)).with(Direction.Axis.Y, target.getY())
                    : new Vec3(origin.x, target.getY(), origin.z);
            tnt.setDeltaMovement(calculateTntLaunchVelocity(origin, landingPoint));
        } else {
            tnt.setDeltaMovement(velocity);
        }
        tnt.setFuse(40);
        tnt.getPersistentData().putBoolean(NO_BLOCK_DAMAGE_TAG, true);
        return tnt;
    }

    private static Vec3 calculateTntLaunchVelocity(Vec3 origin, Vec3 landingPoint) {
        Vec3 displacement = landingPoint.subtract(origin);
        double horizontalDistance = Math.sqrt(displacement.x * displacement.x + displacement.z * displacement.z);
        int flightTicks = Mth.clamp(Mth.ceil(horizontalDistance / 0.65D), 6, 28);

        double horizontalFactor = 0.0D;
        double inertia = 1.0D;
        for (int tick = 0; tick < flightTicks; tick++) {
            horizontalFactor += inertia;
            inertia *= TNT_AIR_INERTIA;
        }

        double zeroVelocityHeight = simulateTntVerticalDisplacement(0.0D, flightTicks);
        double unitVelocityHeight = simulateTntVerticalDisplacement(1.0D, flightTicks);
        double verticalFactor = unitVelocityHeight - zeroVelocityHeight;
        double verticalVelocity = (displacement.y - zeroVelocityHeight) / verticalFactor;
        return new Vec3(
                displacement.x / horizontalFactor,
                verticalVelocity,
                displacement.z / horizontalFactor
        );
    }

    private static double simulateTntVerticalDisplacement(double initialVelocity, int ticks) {
        double position = 0.0D;
        double velocity = initialVelocity;
        for (int tick = 0; tick < ticks; tick++) {
            velocity -= TNT_GRAVITY;
            position += velocity;
            velocity *= TNT_AIR_INERTIA;
        }
        return position;
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

    private static Entity createGhastFireball(Snowball snowball, SnowGolem owner, Vec3 direction) {
        LargeFireball fireball = new LargeFireball(owner.level(), owner, direction.x, direction.y, direction.z, 1);
        fireball.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        fireball.getPersistentData().putBoolean(NO_BLOCK_DAMAGE_TAG, true);
        return fireball;
    }

    private static Entity createDragonFireball(Snowball snowball, SnowGolem owner, Vec3 direction) {
        DragonFireball fireball = new DragonFireball(owner.level(), owner, direction.x, direction.y, direction.z);
        fireball.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        return fireball;
    }

    private static Entity createFireworkRocket(ItemStack source, ItemStack crossbow, Snowball snowball,
                                               SnowGolem owner, Vec3 direction, double speed) {
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

        double rocketSpeed = Math.max(1.0D, speed);
        FireworkRocketEntity rocket = createFireworkEntity(rocketItem, snowball, owner, direction, rocketSpeed);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow) > 0) {
            owner.level().addFreshEntity(createFireworkEntity(
                    rocketItem, snowball, owner, direction.yRot((float)Math.toRadians(-10.0D)), rocketSpeed));
            owner.level().addFreshEntity(createFireworkEntity(
                    rocketItem, snowball, owner, direction.yRot((float)Math.toRadians(10.0D)), rocketSpeed));
        }
        return rocket;
    }

    private static FireworkRocketEntity createFireworkEntity(ItemStack rocketItem, Snowball snowball,
                                                              SnowGolem owner, Vec3 direction, double speed) {
        FireworkRocketEntity rocket = new FireworkRocketEntity(
                owner.level(), rocketItem.copy(), owner, snowball.getX(), snowball.getY(), snowball.getZ(), true
        );
        rocket.setDeltaMovement(direction.normalize().scale(speed));
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
        Arrow arrow = new SnowmanTippedArrow(owner.level(), owner, source);
        return shootSelectedArrow(arrow, snowball, owner, velocity, bow);
    }

    private static void drinkPotion(ItemStack source, SnowGolem owner) {
        ItemStack potion = source.copy();
        potion.setCount(1);
        potion.finishUsingItem(owner.level(), owner);
    }

    private static Entity createThrownPotion(ItemStack source, Snowball snowball, SnowGolem owner, Vec3 velocity) {
        ItemStack thrownStack = source.copy();
        thrownStack.setCount(1);
        ThrownPotion potion = new ThrownPotion(owner.level(), owner);
        potion.setItem(thrownStack);
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
            case GHAST_TEAR -> SoundEvents.GHAST_SHOOT;
            case DRAGON_BREATH -> SoundEvents.ENDER_DRAGON_SHOOT;
            case TNT -> SoundEvents.TNT_PRIMED;
            case WITHER_SKULL -> SoundEvents.WITHER_SHOOT;
            case SCULK_SHRIEKER -> null;
            case TOTEM -> SoundEvents.EVOKER_CAST_SPELL;
            case POTION -> SoundEvents.GENERIC_DRINK;
            case SPLASH_POTION, LINGERING_POTION -> SoundEvents.WITCH_THROW;
            case EGG -> SoundEvents.EGG_THROW;
            case TRIDENT -> SoundEvents.TRIDENT_THROW;
            case SHULKER_SHELL -> null;
            case FIREWORK_ROCKET -> null;
            case LIGHTNING_ROD -> null;
            case BOW -> null;
            case CROSSBOW -> null;
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
                || selected.type == SnowmanUpgradeType.SPLASH_POTION
                || selected.type == SnowmanUpgradeType.LINGERING_POTION;
        if (SuperSnowmenConfig.consumeProjectileItems || (potionLike && SuperSnowmenConfig.consumePotionProjectiles)) {
            inventory.extractItem(selected.slot, 1, false);
        }
    }

    private record SelectedUpgrade(int slot, SnowmanUpgradeType type, ItemStack stack, boolean consumeItem,
                                   ItemStack bow, ItemStack crossbow) {
    }

}
