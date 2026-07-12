package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

public final class UpgradeEnchantments {
    private UpgradeEnchantments() {
    }

    public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
        return find(stack, key).map(holder -> stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(holder)).orElse(0);
    }

    public static Optional<Holder<Enchantment>> find(ItemStack stack, ResourceKey<Enchantment> key) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).keySet().stream()
                .filter(holder -> holder.is(key)).findFirst();
    }

    public static void remove(ItemStack stack, ResourceKey<Enchantment>... keys) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
                stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        mutable.removeIf(holder -> {
            for (ResourceKey<Enchantment> key : keys) {
                if (holder.is(key)) return true;
            }
            return false;
        });
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }
}
