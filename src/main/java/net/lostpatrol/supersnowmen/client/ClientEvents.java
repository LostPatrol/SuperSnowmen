package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.lostpatrol.supersnowmen.menu.SuperSnowmenMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SuperSnowmen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(SuperSnowmenMenus.SNOWMAN_UPGRADE.get(), SnowmanUpgradeScreen::new));
    }
}
