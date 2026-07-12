package net.lostpatrol.supersnowmen.snowman;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class SnowmanUpgradeCapabilities {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SuperSnowmen.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SnowmanUpgradeInventory>> UPGRADES =
            ATTACHMENTS.register("upgrades", () -> AttachmentType.serializable(holder ->
                    new SnowmanUpgradeInventory((net.minecraft.world.entity.animal.SnowGolem) holder)).build());

    private SnowmanUpgradeCapabilities() {
    }
}
