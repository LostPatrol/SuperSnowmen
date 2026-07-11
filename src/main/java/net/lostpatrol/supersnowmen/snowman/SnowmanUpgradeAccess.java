package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.world.entity.Entity;
import java.util.Optional;

public final class SnowmanUpgradeAccess {
    private SnowmanUpgradeAccess() {
    }

    public static Optional<SnowmanUpgradeInventory> get(Entity entity) {
        return entity.getCapability(SnowmanUpgradeCapabilities.UPGRADES).resolve();
    }
}
