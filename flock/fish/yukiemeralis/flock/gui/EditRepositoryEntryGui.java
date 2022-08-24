package fish.yukiemeralis.flock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.InventoryView;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.flock.repository.ModuleRepositoryEntry;

public class EditRepositoryEntryGui extends SurfaceGui
{
    private static GuiItemStack CLOSE_BUTTON = SimpleComponentBuilder.build(Material.BARRIER, "§c§lClose", (event) -> event.getWhoClicked().closeInventory(), "§7§oCloses this menu.");

    private GuiItemStack 
        saveButton,
        setNameButton, 
        setUrlButton, 
        setAuthorButton, 
        setDescriptionButton = generateTodoButton("Set description"), 
        setVersionButton = generateTodoButton("Set version"),
        addDependencyButton = generateTodoButton("Add dependency"),
        importFromModuleButton = generateTodoButton("Import from module");
    
    public EditRepositoryEntryGui(ModuleRepositoryEntry entry, HumanEntity target) 
    {
        super(9, entry.getName(), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);

        saveButton = SimpleComponentBuilder.build(Material.LIME_CONCRETE, "§a§lSave and go back", (event) -> {
            entry.getHostRepository().updateEntry(entry);

            Bukkit.getScheduler().runTask(Eden.getInstance(), () -> new RepositoryGui(entry.getHostRepository(), target).display(target));
        }, 
            "§7§oSaves and returns to the previous", 
            "§7§omenu."
        );
    
        setNameButton = SimpleComponentBuilder.build(Material.PAPER, "§9§lSet name", (event) -> {
            event.getWhoClicked().closeInventory();
            PrintUtils.sendMessage(target, "Enter new name for this repository entry:");

            ChatUtils.expectChat(target, () -> {
                String input = ChatUtils.receiveResult(target);
                ChatUtils.deleteResult(target);

                entry.setName(input);

                openSync(entry, target);
            });
        },
            "§7§oCurrent name: " + (entry.getName() != null ? entry.getName() : "not set"),
            "",
            "§7§oClick to change. This action will",
            "§7§oregenerate this entry's timestamp."
        );

        setUrlButton = SimpleComponentBuilder.build(Material.OBSERVER, "§9§lSet URL", (event) -> {
            event.getWhoClicked().closeInventory();
            PrintUtils.sendMessage(target, "Enter new URL for this repository entry:");

            ChatUtils.expectChat(target, () -> {
                String input = ChatUtils.receiveResult(target);
                ChatUtils.deleteResult(target);

                entry.setUrl(input);

                openSync(entry, target);
            });
        },
            "§7§oCurrent URL:",
            "§7§o" + (entry.getUrl() != null ? entry.getUrl() : "not set"),
            "",
            "§7§oClick to change. This action will",
            "§7§oregenerate this entry's timestamp."
        );

        setAuthorButton = SimpleComponentBuilder.build(Material.PLAYER_HEAD, "§9§lSet author", (event) -> {
            event.getWhoClicked().closeInventory();
            PrintUtils.sendMessage(target, "Enter new author for this repository entry:");

            ChatUtils.expectChat(target, () -> {
                String input = ChatUtils.receiveResult(target);
                ChatUtils.deleteResult(target);

                entry.setAuthor(input);

                openSync(entry, target);
            });
        },
            "§7§oCurrent author: " + (entry.getAuthor() != null ? entry.getAuthor() : "not set"),
            "",
            "§7§oClick to change. This action will",
            "§7§oregenerate this entry's timestamp."
        );
    }

    @Override
    public void init(HumanEntity e, InventoryView view) 
    {
        updateSingleComponent(e, 0, CLOSE_BUTTON);
        updateSingleComponent(e, 1, saveButton);
        updateSingleComponent(e, 2, setNameButton);
        updateSingleComponent(e, 3, setUrlButton);
        updateSingleComponent(e, 4, setDescriptionButton);
        updateSingleComponent(e, 5, setAuthorButton);
        updateSingleComponent(e, 6, setVersionButton);
        updateSingleComponent(e, 7, addDependencyButton);
        updateSingleComponent(e, 8, importFromModuleButton);
    }

    private static GuiItemStack generateTodoButton(String input)
    {
        return SimpleComponentBuilder.build(Material.GRAY_CONCRETE, "§8§lTODO Button: " + input, (event) -> {}, "§7§oThis button hasn't been created yet.");
    }

    private void openSync(ModuleRepositoryEntry entry, HumanEntity e)
    {
        Bukkit.getScheduler().runTask(Eden.getInstance(), () -> new EditRepositoryEntryGui(entry, e).display(e));
    }
}

