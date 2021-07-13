package com.yukiemeralis.blogspot.zenith.core.modgui;

import java.util.Map;
import java.util.TreeMap;

import com.yukiemeralis.blogspot.modules.zenithgui.special.GuiTab;
import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.module.ModuleFamily;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.event.ModuleLoadEvent;
import com.yukiemeralis.blogspot.zenith.module.event.ModuleUnloadEvent;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ModuleTracker implements Listener
{
    private static Map<String, GuiTab> moduleGuiTabs = new TreeMap<>();

    public static void update()
    {
        GuiTab noFamily = new GuiTab("Unknown", "§r§e§l", Material.BOOK);
        GuiTab unloaded = new GuiTab("Unloaded", "§r§e§l", Material.PHANTOM_MEMBRANE);

        for (String family : ModuleFamily.getAllFamilies())
        {
            GuiTab tab = new GuiTab(family, "§r§e§l", ModuleFamily.getIcon(family));

            for (ZenithModule m : Zenith.getModuleManager().getAllModules())
            {
                if (m.getModFamily().equals(family))
                    tab.addComponent(new ModuleAdapter(m));
            }

            moduleGuiTabs.put(family, tab);
        }

        for (ZenithModule m : Zenith.getModuleManager().getAllModules())
        {
            if (m.getModFamily().equals("Unknown"))
            {
                noFamily.addComponent(new ModuleAdapter(m));
                continue;
            }

            if (!ModuleFamily.getAllFamilies().contains(m.getModFamily()))
            {
                noFamily.addComponent(new ModuleAdapter(m));
            }
        }

        for (String reference : Zenith.getModuleManager().getReferences().keySet())
        {
            if (Zenith.getModuleManager().getModuleByName(reference) != null)
                continue;

            unloaded.addComponent(new ModuleAdapter(reference));
        }

        moduleGuiTabs.put("Unknown", noFamily);
        moduleGuiTabs.put("Unloaded", unloaded);
    }

    public static Map<String, GuiTab> getModuleTabData()
    {
        return moduleGuiTabs;
    }

    @EventHandler
    public void onGather(ModuleLoadEvent event)
    {
        ZenithModule mod = event.getModule();

        if (!moduleGuiTabs.containsKey(mod.getModFamily()))
        {
            if (!ModuleFamily.getAllFamilies().contains(mod.getModFamily())) // Family has not been registered
            {
                // Register under "not part of a family"
                moduleGuiTabs.get("Unknown").addComponent(new ModuleAdapter(mod));
                return;
            }

            moduleGuiTabs.put(mod.getModFamily(), new GuiTab(mod.getModFamily(), ModuleFamily.getIcon(mod.getModFamily()), new ModuleAdapter(mod)));
        }

        moduleGuiTabs.get(mod.getModFamily()).addComponent(new ModuleAdapter(mod));
    }

    @EventHandler
    public void onUnload(ModuleUnloadEvent event)
    {
        GuiTab tab = moduleGuiTabs.get(event.getModule().getModFamily());

        if (!ModuleFamily.getAllFamilies().contains(event.getModule().getModFamily())) // Mod is registered as unknown
        {
            moduleGuiTabs.get("Unknown").removeComponent("§r§b§l" + event.getName());
            return;
        }

        tab.removeComponent("§r§b§l" + event.getName());
    }
}
