package net.lostpatrol.supersnowmen.projectile;

import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class SnowmanTippedArrow extends Arrow {
    public SnowmanTippedArrow(Level level, LivingEntity owner, ItemStack arrow, @Nullable ItemStack weapon) {
        super(level, owner, arrow, weapon);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        Entity effectSource = getEffectSource();
        PotionContents contents = getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        contents.potion().ifPresent(potion -> {
            for (MobEffectInstance effect : potion.value().getEffects()) {
                applyEffect(target, new MobEffectInstance(
                        effect.getEffect(),
                        Math.max(effect.mapDuration(duration -> duration / 8), 1),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible()
                ), effectSource);
            }
        });
        for (MobEffectInstance effect : contents.customEffects()) {
            applyEffect(target, new MobEffectInstance(effect), effectSource);
        }
    }

    private void applyEffect(LivingEntity target, MobEffectInstance instance, Entity effectSource) {
        MobEffect effect = instance.getEffect().value();
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
