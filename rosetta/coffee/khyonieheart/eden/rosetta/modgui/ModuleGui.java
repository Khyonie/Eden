package coffee.khyonieheart.eden.rosetta.modgui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.ModuleFamilyRegistry;
import coffee.khyonieheart.eden.module.ModuleFamilyRegistry.ModuleFamilyEntry;
import coffee.khyonieheart.eden.surface.SimpleComponentBuilder;
import coffee.khyonieheart.eden.surface.component.GuiComponent;
import coffee.khyonieheart.eden.surface.component.GuiTab;
import coffee.khyonieheart.eden.surface.enums.DefaultClickAction;
import coffee.khyonieheart.eden.surface.special.TabbedSurfaceGui;
import coffee.khyonieheart.eden.utils.ItemUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.exception.TimeSpaceDistortionException;
import coffee.khyonieheart.eden.utils.result.Result;

public class ModuleGui
{
    public void display(HumanEntity target)
    {
        new TabbedSurfaceGui(36, "Rosetta Module Manager", 0, convertModuleFamilies(target), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF).display(target);
    }

    private List<GuiTab> convertModuleFamilies(HumanEntity target)
    {
        List<GuiTab> data = new ArrayList<>();

        for (ModuleFamilyEntry e : ModuleFamilyRegistry.getAllFamilies())
        {
            List<GuiComponent> components = new ArrayList<>();
            
            for (EdenModule m : e.getData())
            {
                components.add(new ModuleGuiAdapter(m, (event) -> {
                    new ModuleSubGui(m, target).display(event.getWhoClicked());
                }));
            }

            data.add(new GuiTab(ItemUtils.build(e.getMaterial(), "§r§9§l" + e.getName(), "§7§o" + e.getData().size() + " " + PrintUtils.plural(e.getData().size(), "module", "modules")), components));
        }

        List<GuiComponent> unloadedComponents = new ArrayList<>();
        for (String ref : Eden.getModuleManager().getReferences().keySet())
        {
            if (Eden.getModuleManager().isModulePresent(ref))
                continue;

            unloadedComponents.add(SimpleComponentBuilder.build(Material.SALMON_BUCKET, "§r§f§l" + ref, (e) -> {
                    // TODO Java 17 preview feature
                    Result result = Eden.getModuleManager().loadSingleModule(Eden.getModuleManager().getReferences().get(ref));
                    EdenModule mod;
                    switch (result.getState())
                    {
                        case ERR:
                            PrintUtils.sendMessage(e.getWhoClicked(), "§cFailed to load \"" + ref + "\". Reason: " + result.unwrapErr(String.class));
                            return;
                        case OK:
                            mod = result.unwrapOk(EdenModule.class);
                            break;
                        default: throw new TimeSpaceDistortionException();
                    }

                    new ModuleSubGui(mod, e.getWhoClicked()).display(e.getWhoClicked());
                },
                "§7§oThis module is unloaded.",
                "§7§oClick to load module."
            ));
        }
        
        if (unloadedComponents.size() > 0)
        {
            GuiTab unloaded = new GuiTab(Material.WHITE_CONCRETE, "§r§9§lUnloaded modules", unloadedComponents);
            data.add(unloaded);
        }

        return data;
    }
}
