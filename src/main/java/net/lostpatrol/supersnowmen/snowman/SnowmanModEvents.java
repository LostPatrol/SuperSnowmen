package net.lostpatrol.supersnowmen.snowman;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.entity.EntityType;

public final class SnowmanModEvents {
    private SnowmanModEvents() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(Capabilities.ItemHandler.ENTITY, EntityType.SNOW_GOLEM,
                (snowman, context) -> snowman.getData(SnowmanUpgradeCapabilities.UPGRADES));
    }
}
