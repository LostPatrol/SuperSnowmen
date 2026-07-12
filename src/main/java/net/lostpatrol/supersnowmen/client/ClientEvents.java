package net.lostpatrol.supersnowmen.client;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.lostpatrol.supersnowmen.menu.SuperSnowmenMenus;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.SnowGolem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = SuperSnowmen.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(SuperSnowmenMenus.SNOWMAN_UPGRADE.get(), SnowmanUpgradeScreen::new);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderer<? extends SnowGolem> renderer = event.getRenderer(EntityType.SNOW_GOLEM);
        if (renderer instanceof SnowGolemRenderer snowGolemRenderer) {
            snowGolemRenderer.addLayer(new SnowmanWitherArmorLayer(snowGolemRenderer, event.getEntityModels()));
        }
    }
}
