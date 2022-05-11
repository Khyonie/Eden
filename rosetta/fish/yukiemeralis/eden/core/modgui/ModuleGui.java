package fish.yukiemeralis.eden.core.modgui;

import java.util.ArrayList;
import java.util.List;

import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.ModuleFamilyRegistry;
import fish.yukiemeralis.eden.module.ModuleFamilyRegistry.ModuleFamilyEntry;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiTab;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.surface2.special.TabbedSurfaceGui;
import fish.yukiemeralis.eden.utils.ItemUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;

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

        return data;
    }
}
