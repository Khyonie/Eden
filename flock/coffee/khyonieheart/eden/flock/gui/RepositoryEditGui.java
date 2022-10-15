package fish.yukiemeralis.flock.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.meta.ItemMeta;

import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.surface2.special.PagedSurfaceGui;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.flock.repository.ModuleRepository;
import fish.yukiemeralis.flock.repository.ModuleRepositoryEntry;

@SuppressWarnings("unused")
public class RepositoryEditGui extends PagedSurfaceGui
{
    private final ModuleRepository repo;
    private static GuiItemStack QUIT_BUTTON = SimpleComponentBuilder.build(Material.BARRIER, "§r§c§lClose", (event) -> event.getWhoClicked().closeInventory(), "§7§oExits this menu.");

    public RepositoryEditGui(ModuleRepository repo, HumanEntity target) 
    {
        super(36, repo.getName() + " | §4EDIT MODE", target, 0, appendNewToList(repo, repo.getEntryList()), List.of(QUIT_BUTTON, generateExitEditModeButton(repo), generateRegenTimestampButton(repo)), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
        this.repo = repo;
    }

    private static List<? extends GuiComponent> appendNewToList(ModuleRepository repo, List<? extends GuiComponent> input)
    {
        List<GuiComponent> data = new ArrayList<>(input);
        data.add(generateNewEntryButton(repo));
        return Collections.unmodifiableList(data);
    }

    private static GuiItemStack generateNewEntryButton(ModuleRepository repo)
    {
        return SimpleComponentBuilder.build(Material.LIME_CONCRETE, "§r§a§lNew entry", (event) -> {
            ModuleRepositoryEntry entry = new ModuleRepositoryEntry();

            int count = 0;
            while (repo.contains("NewEntry" + count))
                count++;

            entry.setName("NewEntry" + count);

            entry.attachHost(repo);
            
            repo.updateTimestamp(System.currentTimeMillis());
            
            event.getWhoClicked().closeInventory();
            new EditRepositoryEntryGui(entry, event.getWhoClicked()).display(event.getWhoClicked());
        },
            "§7§oCreates a new module repository entry.",
            "",
            "§7§oThis action regenerates this repository's",
            "§7§otimestamp."
        );
    }

    private static GuiItemStack generateRegenTimestampButton(ModuleRepository repo)
    {
        return SimpleComponentBuilder.build(Material.CLOCK, "§r§e§lRegenerate timestamp", (event) -> {
            repo.updateTimestamp(System.currentTimeMillis());
            SurfaceGui.getOpenGui(event.getWhoClicked()).unwrap(SurfaceGui.class).updateSingleComponent(event.getWhoClicked(), event.getSlot(), generateRegenTimestampButton(repo));
        }, 
            "§7§oRegenerates this repository's timestamp,", 
            "§7§oallowing clients to synchronize with new",
            "§7§ochanges. Changes are local until remote",
            "§7§orepository file is updated.",
            "",
            "§6/!\\ This button is for repository authors! /!\\",
            "§6Current repo timestamp: " + (long) repo.getTimestamp(),
            "",
            "§7If you are a server owner, this button",
            "§7may cause unwanted effects.",
            "§7Force-resynchronizing this repository",
            "§7will likely fix any issues."
        );
    }

    private static GuiItemStack generateExitEditModeButton(ModuleRepository repo)
    {
        return SimpleComponentBuilder.build(Material.RED_CONCRETE, "§r§c§lExit edit mode", (event) -> {
            repo.save();
            new RepositoryGui(repo, event.getWhoClicked()).display(event.getWhoClicked());
        }, "§7§oRe-opens this repository in normal", "§7§omode, and writes all changes to disk.");
    }
}
