package net.lostpatrol.supersnowmen.projectile;

import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeAccess;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeInventory;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeType;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.item.PrimedTnt;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

public final class ProjectileReplacement {
    public static final String NO_BLOCK_DAMAGE_TAG = "SuperSnowmenNoBlockDamage";
    public static final String NO_BOTTLE_DRAGON_BREATH_TAG = "SuperSnowmenNoBottleDragonBreath";
    private static final String WITHER_COUNTER_TAG = "SuperSnowmenWitherCounter";

    private ProjectileReplacement() {
    }

    public static void replaceSnowball(EntityJoinLevelEvent event) {
        if (!SuperSnowmenConfig.enableUpgrades || event.getLevel().isClientSide() || !(event.getEntity() instanceof Snowball snowball)) {
            return;
        }
        Entity owner = snowball.getOwner();
        if (!(owner instanceof SnowGolem snowman)) {
            return;
        }

        SnowmanUpgradeAccess.get(snowman).ifPresent(inventory -> {
            SelectedUpgrade selected = select(inventory, snowman);
            if (selected == null) {
                return;
            }
            Entity replacement = createReplacement(selected, snowball, snowman);
            if (replacement == null && selected.type != SnowmanUpgradeType.SCULK_SHRIEKER) {
                return;
            }
            event.setCanceled(true);
            snowball.discard();
            if (replacement != null) {
                event.getLevel().addFreshEntity(replacement);
            }
            consumeIfNeeded(inventory, selected);
        });
    }

    private static SelectedUpgrade select(SnowmanUpgradeInventory inventory, SnowGolem snowman) {
        List<SelectedUpgrade> upgrades = new ArrayList<>();
        for (int slot = SnowmanUpgradeInventory.PLUGIN_START; slot < SnowmanUpgradeInventory.PLUGIN_START + SnowmanUpgradeInventory.PLUGIN_COUNT; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            SnowmanUpgradeType type = SnowmanUpgradeType.byItem(stack.getItem());
            if (type != null && !stack.isEmpty()) {
                upgrades.add(new SelectedUpgrade(slot, type, stack.copy()));
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
        return switch (selected.type) {
            case ARROW -> shootArrow(new Arrow(owner.level(), owner), snowball, velocity);
            case SPECTRAL_ARROW -> shootArrow(new SpectralArrow(owner.level(), owner), snowball, velocity);
            case FIRE_CHARGE -> copyMotion(new SmallFireball(owner.level(), owner, direction.x, direction.y, direction.z), snowball, velocity);
            case FIREWORK_ROCKET -> copyMotion(new FireworkRocketEntity(owner.level(), selected.stack.copy(), owner, snowball.getX(), snowball.getY(), snowball.getZ(), true), snowball, velocity);
            case DRAGON_BREATH -> createDragonFireball(snowball, owner, direction, velocity);
            case TNT -> createTnt(snowball, owner, velocity);
            case WITHER_SKULL -> createWitherSkull(snowball, owner, direction, velocity);
            case SCULK_SHRIEKER -> {
                sonicBoom(snowball, owner, direction);
                yield null;
            }
            case TOTEM -> createFangs(snowball, owner, direction);
            case TIPPED_ARROW -> createTippedArrow(selected.stack, snowball, owner, velocity);
            case POTION, SPLASH_POTION -> createPotion(selected.stack, snowball, owner, velocity);
            case EGG -> copyMotion(new ThrownEgg(owner.level(), owner), snowball, velocity);
            case TRIDENT -> shootArrow(new ThrownTrident(owner.level(), owner, new ItemStack(Items.TRIDENT)), snowball, velocity);
            case SHULKER_SHELL -> createShulkerBullet(snowball, owner, velocity);
        };
    }

    private static AbstractArrow shootArrow(AbstractArrow arrow, Snowball snowball, Vec3 velocity) {
        arrow.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        if (velocity.lengthSqr() > 0.0001D) {
            arrow.shoot(velocity.x, velocity.y, velocity.z, 1.6F, 0.0F);
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

    private static Entity createDragonFireball(Snowball snowball, SnowGolem owner, Vec3 direction, Vec3 velocity) {
        DragonFireball fireball = new DragonFireball(owner.level(), owner, direction.x, direction.y, direction.z);
        fireball.getPersistentData().putBoolean(NO_BOTTLE_DRAGON_BREATH_TAG, true);
        return copyMotion(fireball, snowball, velocity);
    }

    private static Entity createWitherSkull(Snowball snowball, SnowGolem owner, Vec3 direction, Vec3 velocity) {
        WitherSkull skull = new WitherSkull(owner.level(), owner, direction.x, direction.y, direction.z);
        int counter = owner.getPersistentData().getInt(WITHER_COUNTER_TAG) + 1;
        owner.getPersistentData().putInt(WITHER_COUNTER_TAG, counter);
        skull.setDangerous(counter % 4 == 0);
        skull.getPersistentData().putBoolean(NO_BLOCK_DAMAGE_TAG, true);
        return copyMotion(skull, snowball, velocity);
    }

    private static Entity createFangs(Snowball snowball, SnowGolem owner, Vec3 direction) {
        Vec3 pos = snowball.position().add(direction.normalize().scale(1.5D));
        return new EvokerFangs(owner.level(), pos.x, pos.y, pos.z, owner.getYRot(), 0, owner);
    }

    private static Entity createTippedArrow(ItemStack source, Snowball snowball, SnowGolem owner, Vec3 velocity) {
        Arrow arrow = new Arrow(owner.level(), owner);
        arrow.setEffectsFromItem(source);
        return shootArrow(arrow, snowball, velocity);
    }

    private static Entity createPotion(ItemStack source, Snowball snowball, SnowGolem owner, Vec3 velocity) {
        ItemStack splash = new ItemStack(Items.SPLASH_POTION);
        PotionUtils.setPotion(splash, PotionUtils.getPotion(source));
        PotionUtils.setCustomEffects(splash, PotionUtils.getCustomEffects(source));
        ThrownPotion potion = new ThrownPotion(owner.level(), owner);
        potion.setItem(splash);
        return copyMotion(potion, snowball, velocity);
    }

    private static Entity createShulkerBullet(Snowball snowball, SnowGolem owner, Vec3 velocity) {
        LivingEntity target = owner.getTarget();
        if (target != null) {
            owner.level().playSound(
                    null,
                    owner.getX(),
                    owner.getY(),
                    owner.getZ(),
                    SoundEvents.SHULKER_SHOOT,
                    SoundSource.HOSTILE,
                    2.0F,
                    (owner.getRandom().nextFloat() - owner.getRandom().nextFloat()) * 0.2F + 1.0F
            );
            return new ShulkerBullet(owner.level(), owner, target, Direction.Axis.Y);
        }
        return null;
    }

    private static Entity copyMotion(Entity entity, Snowball snowball, Vec3 velocity) {
        entity.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
        entity.setDeltaMovement(velocity);
        if (entity instanceof Projectile projectile) {
            projectile.setOwner(snowball.getOwner());
        }
        return entity;
    }

    private static void sonicBoom(Snowball snowball, SnowGolem owner, Vec3 direction) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 start = snowball.position();
        Vec3 end = start.add(direction.normalize().scale(15.0D));
        AABB box = new AABB(start, end).inflate(1.5D);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box, entity -> entity != owner && entity.isAlive());
        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).subtract(start);
            if (toTarget.normalize().dot(direction.normalize()) > 0.92D) {
                target.hurt(owner.damageSources().sonicBoom(owner), 10.0F);
            }
        }
        serverLevel.playSound(null, owner.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.0F, 1.0F);
        for (int i = 0; i < 16; i++) {
            Vec3 point = start.lerp(end, i / 15.0D);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void consumeIfNeeded(SnowmanUpgradeInventory inventory, SelectedUpgrade selected) {
        boolean potionLike = selected.type == SnowmanUpgradeType.TIPPED_ARROW
                || selected.type == SnowmanUpgradeType.POTION
                || selected.type == SnowmanUpgradeType.SPLASH_POTION;
        if (SuperSnowmenConfig.consumeProjectileItems || (potionLike && SuperSnowmenConfig.consumePotionProjectiles)) {
            inventory.extractItem(selected.slot, 1, false);
        }
    }

    public static void tagDragonBreathCloud(AreaEffectCloud cloud) {
        if (!(cloud.getOwner() instanceof SnowGolem)) {
            return;
        }
        cloud.getPersistentData().putBoolean(NO_BOTTLE_DRAGON_BREATH_TAG, true);
    }

    private record SelectedUpgrade(int slot, SnowmanUpgradeType type, ItemStack stack) {
    }
}
