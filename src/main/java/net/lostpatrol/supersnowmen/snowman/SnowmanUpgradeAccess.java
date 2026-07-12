package net.lostpatrol.supersnowmen.snowman;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.SnowGolem;
import java.util.Optional;

public final class SnowmanUpgradeAccess {
    private SnowmanUpgradeAccess() {
    }

    public static Optional<SnowmanUpgradeInventory> get(Entity entity) {
        return entity instanceof SnowGolem ? Optional.of(entity.getData(SnowmanUpgradeCapabilities.UPGRADES)) : Optional.empty();
    }
}
