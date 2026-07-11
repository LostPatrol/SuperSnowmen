package net.lostpatrol.supersnowmen;

import com.mojang.logging.LogUtils;
import net.lostpatrol.supersnowmen.config.SuperSnowmenConfig;
import net.lostpatrol.supersnowmen.menu.SuperSnowmenMenus;
import net.lostpatrol.supersnowmen.snowman.SnowmanEvents;
import net.lostpatrol.supersnowmen.snowman.SnowmanModEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SuperSnowmen.MOD_ID)
public class SuperSnowmen {
    public static final String MOD_ID = "super_snowmen";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SuperSnowmen(FMLJavaModLoadingContext context) {
        var modBus = context.getModEventBus();
        SuperSnowmenMenus.MENUS.register(modBus);
        modBus.addListener(SnowmanModEvents::registerCapabilities);
        context.registerConfig(ModConfig.Type.SERVER, SuperSnowmenConfig.SPEC);

        var forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.register(SnowmanEvents.class);
        forgeBus.addListener(SnowmanEvents::onEntityInteract);
        forgeBus.addListener(SnowmanEvents::onEntityJoinLevel);
        forgeBus.addListener(SnowmanEvents::onLivingAttack);
        forgeBus.addListener(SnowmanEvents::onLivingDrops);
        forgeBus.addListener(SnowmanEvents::onLivingTick);
        forgeBus.addListener(SnowmanEvents::onMobEffectApplicable);
        forgeBus.addListener(SnowmanEvents::onExplosionDetonate);
        forgeBus.addListener(SnowmanEvents::registerCommands);
    }
}
