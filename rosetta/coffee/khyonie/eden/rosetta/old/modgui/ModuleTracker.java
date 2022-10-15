package coffee.khyonie.eden.rosetta.old.modgui;

import coffee.khyonieheart.eden.module.annotation.Unimplemented;

/**
 * @deprecated Deprecated in favor of new ModuleFamily implementation.
 */
@Deprecated
@Unimplemented("Class has been retired.")
public class ModuleTracker
{
    // private static Map<String, GuiTab> moduleGuiTabs = new TreeMap<>();

    // public static void update()
    // {
    //     GuiTab noFamily = new GuiTab("Unknown", "§r§e§l", Material.BOOK);
    //     GuiTab unloaded = new GuiTab("Unloaded", "§r§e§l", Material.PHANTOM_MEMBRANE);

    //     for (String family : ModuleFamily.getAllFamilies())
    //     {
    //         GuiTab tab = new GuiTab(family, "§r§e§l", ModuleFamily.getIcon(family));

    //         for (EdenModule m : Eden.getModuleManager().getAllModules())
    //         {
    //             if (m.getLegacyFamily().equals(family))
    //                 tab.addComponent(new ModuleAdapter(m));
    //         }

    //         moduleGuiTabs.put(family, tab);
    //     }

    //     for (EdenModule m : Eden.getModuleManager().getAllModules())
    //     {
    //         if (m.getLegacyFamily().equals("Unknown"))
    //         {
    //             noFamily.addComponent(new ModuleAdapter(m));
    //             continue;
    //         }

    //         if (!ModuleFamily.getAllFamilies().contains(m.getLegacyFamily()))
    //         {
    //             noFamily.addComponent(new ModuleAdapter(m));
    //         }
    //     }

    //     for (String reference : Eden.getModuleManager().getReferences().keySet())
    //     {
    //         if (Eden.getModuleManager().getModuleByName(reference) != null)
    //             continue;

    //         unloaded.addComponent(new ModuleAdapter(reference));
    //     }

    //     moduleGuiTabs.put("Unknown", noFamily);
    //     moduleGuiTabs.put("Unloaded", unloaded);
    // }

    // public static Map<String, GuiTab> getModuleTabData()
    // {
    //     return moduleGuiTabs;
    // }

    // @EventHandler
    // public void onGather(ModuleLoadEvent event)
    // {
    //     EdenModule mod = event.getModule();

    //     if (!moduleGuiTabs.containsKey(mod.getLegacyFamily()))
    //     {
    //         if (!ModuleFamily.getAllFamilies().contains(mod.getLegacyFamily())) // Family has not been registered
    //         {
    //             // Register under "not part of a family"
    //             moduleGuiTabs.get("Unknown").addComponent(new ModuleAdapter(mod));
    //             return;
    //         }

    //         moduleGuiTabs.put(mod.getLegacyFamily(), new GuiTab(mod.getLegacyFamily(), ModuleFamily.getIcon(mod.getLegacyFamily()), new ModuleAdapter(mod)));
    //     }

    //     moduleGuiTabs.get(mod.getLegacyFamily()).addComponent(new ModuleAdapter(mod));
    // }

    // @EventHandler
    // public void onUnload(ModuleUnloadEvent event)
    // {
    //     GuiTab tab = moduleGuiTabs.get(event.getModule().getLegacyFamily());

    //     if (!ModuleFamily.getAllFamilies().contains(event.getModule().getLegacyFamily())) // Mod is registered as unknown
    //     {
    //         moduleGuiTabs.get("Unknown").removeComponent("§r§b§l" + event.getName());
    //         return;
    //     }

    //     tab.removeComponent("§r§b§l" + event.getName());
    // }
}
