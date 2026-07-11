package net.lostpatrol.supersnowmen.menu;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SuperSnowmenMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SuperSnowmen.MOD_ID);

    public static final RegistryObject<MenuType<SnowmanUpgradeMenu>> SNOWMAN_UPGRADE = MENUS.register(
            "snowman_upgrade",
            () -> IForgeMenuType.create(SnowmanUpgradeMenu::new)
    );

    private SuperSnowmenMenus() {
    }
}
