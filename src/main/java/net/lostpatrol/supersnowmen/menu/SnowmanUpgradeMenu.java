package net.lostpatrol.supersnowmen.menu;

import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeAccess;
import net.lostpatrol.supersnowmen.snowman.SnowmanUpgradeInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SnowmanUpgradeMenu extends AbstractContainerMenu {
    private final int snowmanId;
    private final SnowmanUpgradeInventory upgrades;

    public SnowmanUpgradeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readInt());
    }

    public SnowmanUpgradeMenu(int containerId, Inventory playerInventory, int snowmanId) {
        super(SuperSnowmenMenus.SNOWMAN_UPGRADE.get(), containerId);
        this.snowmanId = snowmanId;
        Entity entity = playerInventory.player.level().getEntity(snowmanId);
        this.upgrades = entity instanceof SnowGolem
                ? SnowmanUpgradeAccess.get(entity).orElseGet(SnowmanUpgradeInventory::new)
                : new SnowmanUpgradeInventory();
        addUpgradeSlots();
        addPlayerSlots(playerInventory);
    }

    public SnowmanUpgradeInventory upgrades() {
        return upgrades;
    }

    @Override
    public boolean stillValid(Player player) {
        Entity entity = player.level().getEntity(snowmanId);
        return entity instanceof SnowGolem && entity.isAlive() && player.distanceToSqr(entity) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < SnowmanUpgradeInventory.SLOT_COUNT) {
                if (!moveItemStackTo(stack, SnowmanUpgradeInventory.SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveIntoUpgradeSlots(stack)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    private boolean moveIntoUpgradeSlots(ItemStack stack) {
        for (int slot = 0; slot < SnowmanUpgradeInventory.SLOT_COUNT; slot++) {
            if (upgrades.isItemValid(slot, stack) && moveItemStackTo(stack, slot, slot + 1, false)) {
                return true;
            }
        }
        return false;
    }

    private void addUpgradeSlots() {
        addSlot(new LimitedSlotItemHandler(upgrades, SnowmanUpgradeInventory.BASE_PUMPKIN_SLOT, 17, 27));
        addSlot(new LimitedSlotItemHandler(upgrades, SnowmanUpgradeInventory.BASE_SNOW_SLOT, 17, 54));

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                int slot = SnowmanUpgradeInventory.PLUGIN_START + row * 5 + col;
                addSlot(new LimitedSlotItemHandler(upgrades, slot, 62 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < SnowmanUpgradeInventory.SPECIAL_COUNT; row++) {
            int slot = SnowmanUpgradeInventory.SPECIAL_START + row;
            addSlot(new LimitedSlotItemHandler(upgrades, slot, 164, 27 + row * 20));
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 17 + col * 18, 106 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 17 + col * 18, 164));
        }
    }
}
