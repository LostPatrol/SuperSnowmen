package net.lostpatrol.supersnowmen.projectile;

import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

import java.util.List;

public final class SnowmanTippedArrow extends Arrow {
    private final Potion potion;
    private final List<MobEffectInstance> customEffects;

    public SnowmanTippedArrow(Level level, LivingEntity owner, ItemStack arrow) {
        super(level, owner);
        setEffectsFromItem(arrow);
        potion = PotionUtils.getPotion(arrow);
        customEffects = PotionUtils.getCustomEffects(arrow).stream().map(MobEffectInstance::new).toList();
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        Entity effectSource = getEffectSource();
        for (MobEffectInstance effect : potion.getEffects()) {
            applyEffect(target, new MobEffectInstance(
                    effect.getEffect(),
                    Math.max(effect.mapDuration(duration -> duration / 8), 1),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible()
            ), effectSource);
        }
        for (MobEffectInstance effect : customEffects) {
            applyEffect(target, new MobEffectInstance(effect), effectSource);
        }
    }

    private void applyEffect(LivingEntity target, MobEffectInstance instance, Entity effectSource) {
        MobEffect effect = instance.getEffect();
        if (!effect.isInstantenous()) {
            target.addEffect(instance, effectSource);
            return;
        }
        int previousInvulnerableTime = target.invulnerableTime;
        if (SuperSnowmenConfig.bypassDamageCooldown) {
            target.invulnerableTime = 0;
        }
        effect.applyInstantenousEffect(this, getOwner(), target, instance.getAmplifier(), 1.0D);
        if (SuperSnowmenConfig.bypassDamageCooldown && target.invulnerableTime == 0) {
            target.invulnerableTime = previousInvulnerableTime;
        }
    }
}
