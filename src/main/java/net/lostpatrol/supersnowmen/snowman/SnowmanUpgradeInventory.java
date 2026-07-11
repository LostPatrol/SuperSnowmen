package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    public static final int SLOT_COUNT = SPECIAL_START + SPECIAL_COUNT;

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
    protected void onContentsChanged(int slot) {
        if (owner != null) {
            SnowmanUpgradeEffects.apply(owner, this);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
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
        if (isPluginSlot(slot)) {
            return SnowmanUpgradeType.byItem(item) != null;
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
}
