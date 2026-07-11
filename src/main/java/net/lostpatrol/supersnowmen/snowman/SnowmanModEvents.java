package net.lostpatrol.supersnowmen.snowman;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class SnowmanModEvents {
    private SnowmanModEvents() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(SnowmanUpgradeInventory.class);
    }
}
