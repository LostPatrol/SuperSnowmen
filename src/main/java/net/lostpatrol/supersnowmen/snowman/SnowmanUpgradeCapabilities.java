package net.lostpatrol.supersnowmen.snowman;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class SnowmanUpgradeCapabilities {
    public static final Capability<SnowmanUpgradeInventory> UPGRADES = CapabilityManager.get(new CapabilityToken<>() {
    });

    private SnowmanUpgradeCapabilities() {
    }
}
