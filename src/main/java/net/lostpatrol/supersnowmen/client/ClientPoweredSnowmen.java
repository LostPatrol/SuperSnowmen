package net.lostpatrol.supersnowmen.client;

import net.minecraft.world.entity.animal.SnowGolem;

import java.util.HashSet;
import java.util.Set;

public final class ClientPoweredSnowmen {
    private static final Set<Integer> POWERED_ENTITY_IDS = new HashSet<>();

    private ClientPoweredSnowmen() {
    }

    public static void update(int entityId, boolean powered) {
        if (powered) {
            POWERED_ENTITY_IDS.add(entityId);
        } else {
            POWERED_ENTITY_IDS.remove(entityId);
        }
    }

    public static boolean isPowered(SnowGolem snowGolem) {
        return POWERED_ENTITY_IDS.contains(snowGolem.getId());
    }
}
