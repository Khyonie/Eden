package com.yukiemeralis.blogspot.eden.core.old;

/*
import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.auth.Permissions;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.HideFromCollector;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
*/

import org.bukkit.event.Listener;

import com.yukiemeralis.blogspot.eden.module.java.annotations.Unimplemented;

@Unimplemented("Class has been retired in favor of using ZenithGui.")
public class ModuleGui implements Listener
{
    /*
    public ModuleGui() {}

    private final static String invName = "Zenith Modules";
    private Inventory inv;

    private static ItemStack blank_glass;
    private static ItemStack unloaded_module;

    static {
        blank_glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemUtils.applyName(blank_glass, " ");

        unloaded_module = new ItemStack(Material.TROPICAL_FISH_BUCKET);
        ItemUtils.applyLore(unloaded_module, "§r§fThis module is unloaded, thus", "§r§fmodule information cannot", "§r§fbe displayed. Click here to", "§r§fload it.");
    }

    public void init()
    {
        inv = Bukkit.createInventory(null, 54, invName);

        // Paint black
        for (int i = 0; i < 54; i++)
            inv.setItem(i, blank_glass.clone());

        int index = 0;

        for (String name : Zenith.getModuleManager().getReferences().keySet().toArray(new String[] {}))
        {
            if (Zenith.getModuleManager().getGatheredModuleByName(name) != null) // Module is loaded
            {
                inv.setItem(index, Zenith.getModuleManager().getGatheredModuleByName(name).toIcon());
                index++;
                continue;
            }

            // Module is not loaded, go off of reference
            ItemStack modIcon = unloaded_module.clone();
            ItemUtils.applyName(modIcon, "§r§7§l" + name);
            ItemUtils.saveToNamespacedKey(modIcon, "modName", name);

            inv.setItem(index, modIcon);
            index++;
        }
    }

    public void display(Player target)
    {
        target.closeInventory();
        target.openInventory(inv);
    }

    @EventHandler
    public void onInteract(InventoryClickEvent event)
    {
        Inventory inv = event.getClickedInventory();

        if (inv == null)
            return;

        if (!event.getView().getTitle().equals(invName))
            return;
        event.setCancelled(true);

        ItemStack target = inv.getItem(event.getRawSlot());

        if (target == null)
            return;
        
        if (target.getType().equals(Material.BLACK_STAINED_GLASS_PANE))
            return;

        String modName = ItemUtils.readFromNamespacedKey(target, "modName");
        ZenithModule module = Zenith.getModuleManager().getGatheredModuleByName(modName);

        if (module == null)
        {
            // Load module into memory from its file, and start it as disabled
            module = Zenith.getModuleManager().gatherModulesFromFile(new File(Zenith.getModuleManager().getReferences().get(modName)));
            Zenith.getModuleManager().getGatheredModules().put(module.getName(), module);
            Zenith.getModuleManager().getDisabledModules().add(module);
        }

        ModuleSubGui gui = new ModuleSubGui(module);
        gui.init();
        gui.display((Player) event.getWhoClicked());
    }

    @SuppressWarnings("unused")
    @HideFromCollector
    @Unimplemented("Class has been retired in favor of using ZenithGui.")
    public static class ModuleSubGui implements Listener
    {
        ZenithModule module;
        Inventory inv;

        // Buttons
        private final static ItemStack enable_button, disable_button, load_button, unload_button;
        static 
        {
            enable_button = ItemUtils.build(Material.LIME_CONCRETE, "§r§a§lEnable module", "§r§2Enable a disabled module.");
            disable_button = ItemUtils.build(Material.RED_CONCRETE, "§r§c§lDisable module", "§r§4Disable an enabled module.", "§r§4This unloads all commands and events", "§r§4tied to the module.");
            load_button = ItemUtils.build(Material.BLUE_CONCRETE, "§r§a§lLoad module", "§r§9Loads a module from a file", "§r§9reference into memory.");
            unload_button = ItemUtils.build(Material.YELLOW_CONCRETE, "§r§e§lUnload module", "§r§6Unload a disabled module from", "§r§6memory.", "§r§6If the module is enabled, it will", "§r§6be disabled first.");
        }

        public ModuleSubGui(ZenithModule module)
        {
            this.module = module;
        }

        ModuleSubGui() {}

        public void init()
        {
            inv = Bukkit.createInventory(null, 9, "Module options");

            // Paint black
            for (int i = 0; i < 9; i++)
                inv.setItem(i, blank_glass.clone());

            // Set module
            inv.setItem(4, module.toIcon());

            // Buttons
            ItemStack button;
            if (module.getIsEnabled())
            {
                button = disable_button.clone();
            } else {
                button = enable_button.clone();
            }

            ItemUtils.saveToNamespacedKey(button, "modName", module.getName());
            inv.setItem(3, button);

            ItemStack button_ = unload_button.clone();
            ItemUtils.saveToNamespacedKey(button_, "modName", module.getName());
            inv.setItem(5, button_);
        }

        @EventHandler
        public void onInteract(InventoryClickEvent event)
        {
            Inventory inv = event.getClickedInventory();

            if (inv == null)
                return;

            if (!event.getView().getTitle().equals("Module options"))
                return;

            event.setCancelled(true);

            if (!Permissions.isAuthorized((Player) event.getWhoClicked(), 3))
                return;

            String modName;
            ZenithModule module;
            switch (event.getRawSlot())
            {
                case 3: // Enable/disable
                    modName = ItemUtils.readFromNamespacedKey(event.getClickedInventory().getItem(event.getRawSlot()), "modName");
                    module = Zenith.getModuleManager().getGatheredModuleByName(modName);

                    if (module == null)
                        break;

                    if (module.getIsEnabled())
                    {
                        if (module.getClass().isAnnotationPresent(PreventUnload.class))
                        {
                            PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module cannot be disabled from here!");
                            break;
                        }

                        Zenith.getModuleManager().disableModule(module.getName(), CallerToken.PLAYER);
                        module.setDisabled();
                        break;
                    }

                    Zenith.getModuleManager().loadModule(module);
                    module.setEnabled();
                    break;
                case 5: // Unload module | Disable + unload module
                    modName = ItemUtils.readFromNamespacedKey(event.getClickedInventory().getItem(event.getRawSlot()), "modName");
                    module = Zenith.getModuleManager().getGatheredModuleByName(modName);

                    if (module.getClass().isAnnotationPresent(PreventUnload.class))
                    {
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module cannot be disabled from here!");
                        break;
                    }

                    if (module.getIsEnabled()) // Disable
                    {
                        Zenith.getModuleManager().disableModule(module.getName(), CallerToken.PLAYER);
                        module.setDisabled();
                    }

                    Zenith.getModuleManager().removeModuleFromMemory(module.getName(), CallerToken.PLAYER);

                    break;
                default:
                    break;
            }

            ModuleGui mgui = new ModuleGui();
            mgui.init();
            mgui.display((Player) event.getWhoClicked());
        }

        public void display(Player target)
        {
            target.openInventory(this.inv);
        }
    }
    */
}
