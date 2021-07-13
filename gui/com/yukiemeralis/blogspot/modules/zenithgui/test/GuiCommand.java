package com.yukiemeralis.blogspot.modules.zenithgui.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiComponent;
import com.yukiemeralis.blogspot.modules.zenithgui.special.GuiTab;
import com.yukiemeralis.blogspot.modules.zenithgui.special.TabbedDynamicGui;
import com.yukiemeralis.blogspot.zenith.command.ZenithCommand;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

public class GuiCommand extends ZenithCommand
{
    public GuiCommand(ZenithModule parent_module) 
    {
        super("zgui", parent_module);
    }

    private static final String[] colors = {"WHITE", "LIGHT_GRAY", "GRAY", "BLACK", "RED", "ORANGE", "YELLOW", "LIME", "LIGHT_BLUE", "CYAN", "BLUE", "PURPLE", "MAGENTA", "PINK"};
    private static final String[] colored_blocks = {"WOOL", "TERRACOTTA", "STAINED_GLASS", "CONCRETE", "CONCRETE_POWDER", "CARPET", "STAINED_GLASS_PANE", "BANNER"};

    private static final Map<String, GuiTab> data = new HashMap<>();
    static {

        List<GuiComponent> valid_components = new ArrayList<>();
        GuiTab tab;
        Material material;
        for (String type : colored_blocks) {
            for (String color : colors) {
                try {
                    material = Material.valueOf(color + "_" + type);
                    valid_components.add(new ComponentTest(material, "§r§e§l" + material.name().replaceAll("_", " ")));
                } catch (IllegalArgumentException e) {}
            }

            tab = new GuiTab("" + type.replaceAll("_", " "), Material.valueOf("GREEN_" + type), valid_components.toArray(new GuiComponent[] {}));
            data.put("" + type.replaceAll("_", " "), tab);
            valid_components.clear();
        }
    }

    
    @ZenCommandHandler(usage = "zgui", description = "Open up a test GUI.", argsCount = 0, minAuthorizedRank = 0)
    public void zcommand_nosubcmd(CommandSender sender, String commandLabel, String[] args)
    {
        TabbedDynamicGui gui = new TabbedDynamicGui(5, "Color tabs", null, data, "WOOL", InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
        gui.display((Player) sender);
    }
}
