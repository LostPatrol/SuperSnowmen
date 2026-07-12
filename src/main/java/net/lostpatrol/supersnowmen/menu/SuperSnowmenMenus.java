package net.lostpatrol.supersnowmen.menu;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public final class SuperSnowmenMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SuperSnowmen.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<SnowmanUpgradeMenu>> SNOWMAN_UPGRADE = MENUS.register(
            "snowman_upgrade",
            () -> IMenuTypeExtension.create(SnowmanUpgradeMenu::new)
    );

    private SuperSnowmenMenus() {
    }
}
