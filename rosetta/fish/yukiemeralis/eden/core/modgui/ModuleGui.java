package fish.yukiemeralis.eden.core.modgui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.ModuleFamilyRegistry;
import fish.yukiemeralis.eden.module.ModuleFamilyRegistry.ModuleFamilyEntry;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiTab;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.surface2.special.TabbedSurfaceGui;
import fish.yukiemeralis.eden.utils.ItemUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.exception.TimeSpaceDistortionException;
import fish.yukiemeralis.eden.utils.result.Err;
import fish.yukiemeralis.eden.utils.result.Ok;
import fish.yukiemeralis.eden.utils.result.Result;


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
                components.add(new ModuleGuiAdapter(m, (event) -> {
                    new ModuleSubGui(m, target).display(event.getWhoClicked());
                }));

            data.add(new GuiTab(ItemUtils.build(e.getMaterial(), "§r§9§l" + e.getName(), "§7§o" + e.getData().size() + " " + PrintUtils.plural(e.getData().size(), "module", "modules")), components));
        }

        List<GuiComponent> unloadedComponents = new ArrayList<>();
        for (String ref : Eden.getModuleManager().getReferences().keySet())
        {
            if (Eden.getModuleManager().isModulePresent(ref))
                continue;

            unloadedComponents.add(SimpleComponentBuilder.build(Material.SALMON_BUCKET, "§r§f§l" + ref, (e) -> {
                    Result result = Eden.getModuleManager().loadSingleModule(Eden.getModuleManager().getReferences().get(ref));
                    EdenModule mod;
                    switch (result)
                    {
                        case Err err:
                            PrintUtils.sendMessage(e.getWhoClicked(), "§cFailed to load \"" + ref + "\". Reason: " + err.unwrapErr(String.class));
                            return;
                        case Ok ok:
                            mod = ok.unwrapOk(EdenModule.class);
                            break;
                        case default: throw new TimeSpaceDistortionException();
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
