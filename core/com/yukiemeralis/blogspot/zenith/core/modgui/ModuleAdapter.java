package com.yukiemeralis.blogspot.zenith.core.modgui;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiComponent;
import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.java.ModuleManager;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ModuleAdapter implements GuiComponent
{
    private GuiItemStack icon = null; 
    private ZenithModule module;
    private String moduleReference = null;

    private static final ModuleManager mm = Zenith.getModuleManager();

    public ModuleAdapter(ZenithModule module)
    {
        this.module = module;
    }

    public ModuleAdapter(String moduleReference)
    {
        this.moduleReference = moduleReference;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event) 
    {
        icon.onIconInteract(event);
    }

    @Override
    public GuiItemStack toIcon() 
    {
        if (icon == null)
        {
            if (module != null)
            {
                icon = new GuiItemStack(module.toIcon())
                {
                    @Override
                    public void onIconInteract(InventoryClickEvent event) 
                    {
                        ZenithModule mod = mm.getModuleByName(ItemUtils.readFromNamespacedKey(this, "modName"));

                        if (mod == null)
                            return;

                        openSubGui(mod, (Player) event.getWhoClicked());
                    }
                };

                ItemUtils.saveToNamespacedKey(icon, "modName", module.getName());

                return icon;
            }
            
            icon = new GuiItemStack(Material.TROPICAL_FISH_BUCKET)
            {
                

                @Override
                public void onIconInteract(InventoryClickEvent event) 
                {
                    // Load module into memory and open subgui  
                    mm.loadSingleModule(mm.getReferences().get(ItemUtils.readFromNamespacedKey(this, "modName")));

                    ZenithModule mod = mm.getDisabledModuleByName(ItemUtils.readFromNamespacedKey(this, "modName"));

                    if (mod == null)
                    {
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to load module \"" + ItemUtils.readFromNamespacedKey(this, "modName") + "\".");
                        return;
                    }

                    openSubGui(mod, (Player) event.getWhoClicked());
                }
            };

            ItemUtils.applyName(icon, "§r§7§l" + moduleReference);
            ItemUtils.applyLore(icon, "§r§fThis module is unloaded, thus", "§r§fmodule information cannot", "§r§fbe displayed. Click here to", "§r§fload it.");

            ItemUtils.saveToNamespacedKey(icon, "modName", moduleReference);
        }

        return icon;
    }

    private void openSubGui(ZenithModule mod, Player player)
    {
        ModuleSubGui gui = new ModuleSubGui(mod);
        gui.init();
        gui.display(player);
    }
}
