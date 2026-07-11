package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Optional;

public final class SnowmanUpgradeAccess {
    private SnowmanUpgradeAccess() {
    }

    public static Optional<SnowmanUpgradeInventory> get(Entity entity) {
        return entity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .resolve()
                .filter(SnowmanUpgradeInventory.class::isInstance)
                .map(SnowmanUpgradeInventory.class::cast);
    }
}
