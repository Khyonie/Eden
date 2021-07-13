package com.yukiemeralis.blogspot.zenith.core.modgui;

import com.yukiemeralis.blogspot.modules.zenithgui.special.TabbedDynamicGui;

import org.bukkit.event.inventory.InventoryAction;

public class ModuleGui extends TabbedDynamicGui
{
    public ModuleGui() 
    {
        super(
            3, // How many rows to have in the inventory
            "Modules", // Inventory name, I.E what gets presented at the top of the inventory
            null, // A player to build the inventory's data off of. Not applicable in this case, so we pass in null
            ModuleTracker.getModuleTabData(), // Tab data to build the GUI with
            "Zenith base modules", // Initial tab to start on
            InventoryAction.PICKUP_ALL, // Type of inventory action that this inventory will allow (regular left click)
            InventoryAction.PICKUP_HALF // Type of inventory action that this inventory will allow (regular right click)
        );
    }
}
