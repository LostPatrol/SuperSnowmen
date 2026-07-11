package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class SnowmanRangedAttackGoal extends Goal {
    private final Mob mob;
    private final RangedAttackMob rangedAttackMob;
    private final double speedModifier;
    private final int baseAttackIntervalMin;
    private final int baseAttackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;
    @Nullable
    private LivingEntity target;
    private int attackTime = -1;
    private int seeTime;

    public SnowmanRangedAttackGoal(RangedAttackMob rangedAttackMob, double speedModifier, int attackInterval, float attackRadius) {
        this(rangedAttackMob, speedModifier, attackInterval, attackInterval, attackRadius);
    }

    public SnowmanRangedAttackGoal(RangedAttackMob rangedAttackMob, double speedModifier, int attackIntervalMin, int attackIntervalMax, float attackRadius) {
        if (!(rangedAttackMob instanceof Mob mob)) {
            throw new IllegalArgumentException("SnowmanRangedAttackGoal requires a Mob that implements RangedAttackMob");
        }
        this.rangedAttackMob = rangedAttackMob;
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.baseAttackIntervalMin = attackIntervalMin;
        this.baseAttackIntervalMax = attackIntervalMax;
        this.attackRadius = attackRadius;
        this.attackRadiusSqr = attackRadius * attackRadius;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = mob.getTarget();
        if (livingEntity != null && livingEntity.isAlive()) {
            target = livingEntity;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || target != null && target.isAlive() && !mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        target = null;
        seeTime = 0;
        attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }

        double distanceSqr = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = mob.getSensing().hasLineOfSight(target);
        if (hasLineOfSight) {
            seeTime++;
        } else {
            seeTime = 0;
        }

        if (!(distanceSqr > attackRadiusSqr) && seeTime >= 5) {
            mob.getNavigation().stop();
        } else {
            mob.getNavigation().moveTo(target, speedModifier);
        }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--attackTime == 0) {
            if (!hasLineOfSight) {
                return;
            }

            float distanceFactor = (float)Math.sqrt(distanceSqr) / attackRadius;
            float clampedDistanceFactor = Mth.clamp(distanceFactor, 0.1F, 1.0F);
            rangedAttackMob.performRangedAttack(target, clampedDistanceFactor);
            attackTime = Mth.floor(distanceFactor * (attackIntervalMax() - attackIntervalMin()) + attackIntervalMin());
        } else if (attackTime < 0) {
            attackTime = Mth.floor(Mth.lerp(Math.sqrt(distanceSqr) / attackRadius, (double)attackIntervalMin(), (double)attackIntervalMax()));
        }
    }

    private int attackIntervalMin() {
        return scaledInterval(baseAttackIntervalMin);
    }

    private int attackIntervalMax() {
        return scaledInterval(baseAttackIntervalMax);
    }

    private int scaledInterval(int baseInterval) {
        int pumpkins = SnowmanUpgradeAccess.get(mob)
                .map(inventory -> inventory.getStackInSlot(SnowmanUpgradeInventory.BASE_PUMPKIN_SLOT).getCount())
                .orElse(0);
        double attacksPerInterval = 1.0D + pumpkins * 0.02D;
        return Math.max(1, Mth.floor(baseInterval / attacksPerInterval));
    }
}
