package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.lostpatrol.supersnowmen.menu.SuperSnowmenMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SuperSnowmen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(SuperSnowmenMenus.SNOWMAN_UPGRADE.get(), SnowmanUpgradeScreen::new));
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderer<? extends SnowGolem> renderer = event.getRenderer(EntityType.SNOW_GOLEM);
        if (renderer instanceof SnowGolemRenderer snowGolemRenderer) {
            snowGolemRenderer.addLayer(new SnowmanWitherArmorLayer(snowGolemRenderer, event.getEntityModels()));
        }
    }
}
