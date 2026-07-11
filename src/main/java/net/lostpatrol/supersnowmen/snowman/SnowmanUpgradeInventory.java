package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnowmanUpgradeInventory extends ItemStackHandler {
    public static final int BASE_PUMPKIN_SLOT = 0;
    public static final int BASE_SNOW_SLOT = 1;
    public static final int PLUGIN_START = 2;
    public static final int PLUGIN_COUNT = 20;
    public static final int SPECIAL_START = PLUGIN_START + PLUGIN_COUNT;
    public static final int SPECIAL_COUNT = 3;
    public static final int BASE_DIAMOND_SLOT = SPECIAL_START + SPECIAL_COUNT;
    public static final int SLOT_COUNT = BASE_DIAMOND_SLOT + 1;

    @Nullable
    private final SnowGolem owner;

    public SnowmanUpgradeInventory() {
        this(null);
    }

    public SnowmanUpgradeInventory(@Nullable SnowGolem owner) {
        super(SLOT_COUNT);
        this.owner = owner;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag migrated = nbt.copy();
        migrated.putInt("Size", SLOT_COUNT);
        super.deserializeNBT(migrated);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (owner != null) {
            SnowmanUpgradeEffects.apply(owner, this);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot == BASE_DIAMOND_SLOT) {
            return 4;
        }
        return isPluginSlot(slot) ? 1 : 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        Item item = stack.getItem();
        if (slot == BASE_PUMPKIN_SLOT) {
            return item == Items.PUMPKIN || item == Items.CARVED_PUMPKIN;
        }
        if (slot == BASE_SNOW_SLOT) {
            return item == Items.SNOW_BLOCK;
        }
        if (slot == BASE_DIAMOND_SLOT) {
            return item == Items.DIAMOND;
        }
        if (isPluginSlot(slot)) {
            SnowmanUpgradeType type = SnowmanUpgradeType.byItem(item);
            if (type == SnowmanUpgradeType.LIGHTNING_ROD) {
                return getStackInSlot(slot).is(Items.LIGHTNING_ROD)
                        || !hasProjectileUpgrade(SnowmanUpgradeType.LIGHTNING_ROD);
            }
            return type != null;
        }
        if (isSpecialSlot(slot)) {
            return item == Items.ICE || item == Items.PACKED_ICE || item == Items.BLUE_ICE;
        }
        return false;
    }

    public static boolean isPluginSlot(int slot) {
        return slot >= PLUGIN_START && slot < PLUGIN_START + PLUGIN_COUNT;
    }

    public static boolean isSpecialSlot(int slot) {
        return slot >= SPECIAL_START && slot < SPECIAL_START + SPECIAL_COUNT;
    }

    public boolean hasProjectileUpgrade(SnowmanUpgradeType type) {
        for (int slot = PLUGIN_START; slot < PLUGIN_START + PLUGIN_COUNT; slot++) {
            if (getStackInSlot(slot).is(type.item())) {
                return true;
            }
        }
        return false;
    }

    public ItemStack findPreferredTrident() {
        ItemStack first = ItemStack.EMPTY;
        for (int slot = PLUGIN_START; slot < PLUGIN_START + PLUGIN_COUNT; slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (!stack.is(Items.TRIDENT)) {
                continue;
            }
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.CHANNELING, stack) > 0) {
                return stack;
            }
            if (first.isEmpty()) {
                first = stack;
            }
        }
        return first;
    }

    public boolean hasChannelingTrident() {
        ItemStack trident = findPreferredTrident();
        return !trident.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.CHANNELING, trident) > 0;
    }

    public boolean areAllPluginSlotsFilled() {
        for (int slot = PLUGIN_START; slot < PLUGIN_START + PLUGIN_COUNT; slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.isEmpty() || !isItemValid(slot, stack)) {
                return false;
            }
        }
        return true;
    }

    public boolean areAllAttributeSlotsActive() {
        ItemStack pumpkin = getStackInSlot(BASE_PUMPKIN_SLOT);
        ItemStack snow = getStackInSlot(BASE_SNOW_SLOT);
        ItemStack diamond = getStackInSlot(BASE_DIAMOND_SLOT);
        if (pumpkin.isEmpty() || !isItemValid(BASE_PUMPKIN_SLOT, pumpkin)
                || snow.isEmpty() || !isItemValid(BASE_SNOW_SLOT, snow)
                || diamond.isEmpty() || !isItemValid(BASE_DIAMOND_SLOT, diamond)) {
            return false;
        }
        for (int slot = SPECIAL_START; slot < SPECIAL_START + SPECIAL_COUNT; slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.getCount() < 64 || !isItemValid(slot, stack)) {
                return false;
            }
        }
        return true;
    }
}
