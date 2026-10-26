package ebf.tim.registry;

import ebf.tim.TrainsInMotion;
import ebf.tim.gui.TransportMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TiMMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TrainsInMotion.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<TransportMenu>> TRANSPORT =
            MENUS.register("transport", () -> IMenuTypeExtension.create(TransportMenu::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<ebf.tim.gui.TableMenu>> TABLE =
            MENUS.register("table", () -> IMenuTypeExtension.create(ebf.tim.gui.TableMenu::fromNetwork));
}
