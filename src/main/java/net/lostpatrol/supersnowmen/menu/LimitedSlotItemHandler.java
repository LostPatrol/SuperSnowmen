package net.lostpatrol.supersnowmen.menu;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LimitedSlotItemHandler extends SlotItemHandler {
    public LimitedSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize() {
        return getItemHandler().getSlotLimit(getSlotIndex());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(super.getMaxStackSize(stack), getItemHandler().getSlotLimit(getSlotIndex()));
    }
}
