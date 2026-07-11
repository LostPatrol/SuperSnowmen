package net.lostpatrol.supersnowmen.snowman;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnowmanUpgradeProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SuperSnowmen.MOD_ID, "upgrades");

    private final SnowmanUpgradeInventory inventory;
    private final LazyOptional<SnowmanUpgradeInventory> optional;

    public SnowmanUpgradeProvider(SnowGolem snowman) {
        this.inventory = new SnowmanUpgradeInventory(snowman);
        this.optional = LazyOptional.of(() -> inventory);
    }

    public SnowmanUpgradeInventory inventory() {
        return inventory;
    }

    public void invalidate() {
        optional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == SnowmanUpgradeCapabilities.UPGRADES || cap == ForgeCapabilities.ITEM_HANDLER) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        inventory.deserializeNBT(nbt);
    }
}
