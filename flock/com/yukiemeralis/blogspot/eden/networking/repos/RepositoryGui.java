package com.yukiemeralis.blogspot.eden.networking.repos;

import java.util.Arrays;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.gui.base.DynamicGui;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.networking.NetworkingModule;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RepositoryGui extends PagedDynamicGui 
{
    public RepositoryGui(Player player, EdenRepository repo) 
    {
        this(player, repo, false);
    }

    public RepositoryGui(Player player, EdenRepository repo, boolean addSyncButton) 
    {
        super(
            5, 
            repo.getName() + ": " + repo.getEntries().size() + (addSyncButton ? " §c(TEMPORARY)" : ""), 
            player, 0, 
            repo.getEntries(), 
            Arrays.asList(
                new GuiItemStack[] {
                    getCloseButton(), 
                    getBackButton(), 
                    getDlAllButton(), 
                    getInfoIcon(repo), 
                    addSyncButton ? getSyncButton(repo) : getBlankIconGuiItemStack()
                }
            ), 
            InventoryAction.PICKUP_ALL, 
            InventoryAction.PICKUP_HALF
        );
    }

    private static GuiItemStack getDlAllButton()
    {
        return new GuiItemStack(ItemUtils.build(Material.ENDER_CHEST, "§r§a§lDownload all", "§r§7§oDownloads all modules inside", "§r§7§othis repository.", "", "§r§bClick to perform this action.")) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                
            }
        };       
    }

    private static GuiItemStack getInfoIcon(EdenRepository repo)
    {
        int installed = 0,
            incompatible = 0,
            updatable = 0,
            foreign = 0
            ;

        for (EdenRepositoryEntry e : repo.getEntries())
        {
            switch (NetworkingModule.getModuleUpgradeStatus(e.getName(), e))
            {
                case INCOMPATIBLE_SERVER:
                    incompatible++;
                    break;
                case NOT_INSTALLED:
                    foreign++;
                    break;
                case SAME_VERSION:
                    installed++;
                    break;
                case UPGRADABLE:
                    updatable++;
                    break;
                case UPGRADABLE_INCOMPATIBLE_SERVER:
                    incompatible++;
                    break;
            }
        }

        String updateColor = updatable == 0 ? "a" : "e";
        String incompatColor = incompatible == 0 ? "a" : "c";

        return new GuiItemStack(ItemUtils.build(
            Material.CHEST, 
            "§r§9§lInfo", 
            "§r§7§oInstalled: §a" + installed,
            "§r§7§oIncompatible: §" + incompatColor + incompatible,
            "§r§7§oUpgradable: §" + updateColor + updatable,
            "§r§7§oForeign: §a" + foreign
        )) {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                return;
            }
            
        };
    }

    private static GuiItemStack getBackButton()
    {
        return new GuiItemStack(ItemUtils.build(Material.RED_CONCRETE, "§r§e§lBack", "§r§7§oReturn to the global", "§r§7§orepository list.")) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                event.getWhoClicked().closeInventory();
                new GlobalRepositoryGui((Player) event.getWhoClicked()).display(event.getWhoClicked());    
            }
        };
    }

    private static GuiItemStack getBlankIconGuiItemStack()
    {
        return new GuiItemStack(DynamicGui.getBlankIcon())
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) {}
        };
    }

    private static GuiItemStack getSyncButton(EdenRepository repo)
    {
        return new GuiItemStack(ItemUtils.build(Material.FURNACE, "§r§9§lSync/Install", "§r§7§oSync this repo locally.", "", "§r§bClick to perform this action.")) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                NetworkingModule.getKnownRepositories().put(repo.getName(), repo);
                JsonUtils.toJsonFile("./plugins/Eden/repos/" + repo.getName() + ".json", repo);

                PrintUtils.sendMessage(event.getWhoClicked(), "§aInstalled repository!");
                new RepositoryGui((Player) event.getWhoClicked(), repo, false).display(event.getWhoClicked());;
            }
        };
    }

    public static GuiItemStack getCloseButton()
    {
        return new GuiItemStack(ItemUtils.build(Material.BARRIER, "§r§c§lClose", "§r§7§oClose this menu."))
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                event.getWhoClicked().closeInventory();
            }  
        };
    }
}
