package fish.yukiemeralis.flock.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.InventoryView;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.surface2.special.PagedSurfaceGui;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.flock.TextUtils;
import fish.yukiemeralis.flock.repository.ModuleRepositoryEntry;
import fish.yukiemeralis.flock.repository.ModuleRepositoryEntry.RepositoryEntryProperty;

public class EditRepositoryEntryGui extends SurfaceGui
{
    private static GuiItemStack CLOSE_BUTTON = SimpleComponentBuilder.build(Material.BARRIER, "§c§lClose", (event) -> event.getWhoClicked().closeInventory(), "§7§oCloses this menu.");

    private GuiItemStack 
        saveButton,
        setNameButton, 
        setUrlButton, 
        setAuthorButton, 
        setDescriptionButton, 
        setVersionButton,
        addDependencyButton,
        updateTimestampButton,
        importFromModuleButton;
    
    public EditRepositoryEntryGui(ModuleRepositoryEntry entry, HumanEntity target) 
    {
        super(18, entry.getName(), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);

        paintBlack();

        saveButton = SimpleComponentBuilder.build(Material.LIME_CONCRETE, "§a§lSave and go back", (event) -> {
            entry.getHostRepository().updateEntry(entry);

            Bukkit.getScheduler().runTask(Eden.getInstance(), () -> new RepositoryGui(entry.getHostRepository(), target).display(target));
        }, 
            "§7§oSaves and returns to the previous", 
            "§7§omenu."
        );
    
        setNameButton = SimpleComponentBuilder.build(Material.NAME_TAG, "§9§lSet name", (event) -> {
            askPropertyChange(target, entry, RepositoryEntryProperty.NAME, "name");
        },
            "§7§oCurrent name: " + (entry.getName() != null ? entry.getName() : "not set"),
            "",
            "§7§oClick to change. This action will",
            "§7§oregenerate this entry's timestamp."
        );

        setUrlButton = SimpleComponentBuilder.build(Material.OBSERVER, "§9§lSet URL", (event) -> {
            askPropertyChange(target, entry, RepositoryEntryProperty.URL, "url");
        },
            "§7§oCurrent URL:",
            "§7§o" + TextUtils.pruneStringLength(entry.getUrl() != null ? entry.getUrl() : "not set", "...", 40),
            "",
            "§7§oClick to change. This action will",
            "§7§oregenerate this entry's timestamp."
        );

        setAuthorButton = SimpleComponentBuilder.build(Material.PLAYER_HEAD, "§9§lSet author", (event) -> {
            askPropertyChange(target, entry, RepositoryEntryProperty.AUTHOR, "author");
        },
            "§7§oCurrent author: " + (entry.getAuthor() != null ? entry.getAuthor() : "not set"),
            "",
            "§7§oClick to change. This action will",
            "§7§oregenerate this entry's timestamp."
        );

        setDescriptionButton = SimpleComponentBuilder.build(Material.PAPER, "§9§lSet description", (event) -> {
            askPropertyChange(target, entry, RepositoryEntryProperty.DESCRIPTION, "description");
        },
            "§7§oCurrent description:",
            "§7§o" + TextUtils.pruneStringLength(entry.getDescription() != null ? entry.getDescription() : "Not set", "...", 40),
            "",
            "§7§oClick to change. This action will",
            "§7§oregenerate this entry's timestamp."
        );

        setVersionButton = SimpleComponentBuilder.build(Material.FILLED_MAP, "§9§lSet version", (event) -> {
            askPropertyChange(target, entry, RepositoryEntryProperty.VERSION, "version");
        },
            "§7§oCurrent version: " + (entry.getVersion() != null ? entry.getVersion() : "not set"),
            "",
            "§7§oClick to change. This action will",
            "§7§oregenerate this entry's timestamp."
        );

        updateTimestampButton = generateUpdateTimestampButton(target, this, entry);
        addDependencyButton = generateAddDependencyButton(target, entry, this);
        importFromModuleButton = generateImportFromModuleButton(target, entry);
    }

    @Override
    public void init(HumanEntity e, InventoryView view) 
    {
        updateSingleComponent(e, 0, CLOSE_BUTTON);
        updateSingleComponent(e, 1, saveButton);
        updateSingleComponent(e, 2, importFromModuleButton);
        updateSingleComponent(e, 3, updateTimestampButton);
        
        updateSingleComponent(e, 9, setNameButton);
        updateSingleComponent(e, 10, setUrlButton);
        updateSingleComponent(e, 11, setDescriptionButton);
        updateSingleComponent(e, 12, setAuthorButton);
        updateSingleComponent(e, 13, setVersionButton);
        updateSingleComponent(e, 14, addDependencyButton);
    }

    private static GuiItemStack generateUpdateTimestampButton(HumanEntity target, SurfaceGui gui, ModuleRepositoryEntry entry)
    {
        return SimpleComponentBuilder.build(Material.CLOCK, "§9§lUpdate timestamp", (event) -> {
            entry.updateTimestamp();
            gui.updateSingleComponent(target, event.getSlot(), generateUpdateTimestampButton(target, gui, entry));
        }, 
            "§7§oCurrent timestamp: " + entry.getTimestamp(),
            "",
            "§7§oClick to update timestamp."
        );
    }

    // private static GuiItemStack generateTodoButton(String input)
    // {
    //     return SimpleComponentBuilder.build(Material.GRAY_CONCRETE, "§8§lTODO Button: " + input, (event) -> {}, "§7§oThis button hasn't been created yet.");
    // }

    private static GuiItemStack generateAddDependencyButton(HumanEntity target, ModuleRepositoryEntry entry, SurfaceGui gui)
    {
        List<String> addDependencyLore = new ArrayList<>(Arrays.asList("§7§oDependencies:"));
        if (entry.getDependencies().size() == 0)
            addDependencyLore.add("§7§oNo dependencies");

        for (String dep : entry.getDependencies())
            addDependencyLore.add("§7§o- " + dep);
        
        addDependencyLore.add("");
        addDependencyLore.add("§7§oLeft-click to add dependency.");
        addDependencyLore.add("§7§oRight-click to remove bottom");
        addDependencyLore.add("§7§odependency.");

        return SimpleComponentBuilder.build(Material.OAK_SAPLING, "§9§oAdd dependency", (event) -> {
            switch (event.getAction())
            {
                case PICKUP_ALL: // Left click - Add dependency
                    event.getWhoClicked().closeInventory();
                    PrintUtils.sendMessage(event.getWhoClicked(), "Enter dependency in \"repository:module\" format:");

                    ChatUtils.expectChat(event.getWhoClicked(), () -> {
                        String input = ChatUtils.receiveResult(event.getWhoClicked());
                        ChatUtils.deleteResult(event.getWhoClicked());

                        if (!input.contains(":"))
                        {  
                            PrintUtils.sendMessage(event.getWhoClicked(), "§cInvalid dependency \"" + input + "\". Dependencies must be in \"repository:module\" format.");
                            return;
                        }

                        if (entry.getDependencies().contains(input))
                        {
                            PrintUtils.sendMessage(event.getWhoClicked(), "§cDependency \"" + input + "\" already exists for " + entry.getName() + ".");
                            return;
                        }

                        entry.addDependency(input);
                        entry.updateTimestamp();
                        
                        openSync(entry, event.getWhoClicked());
                    });
                    break;
                case PICKUP_HALF: // Right click - Remove dependency
                    String result = entry.removeBottomDependency();

                    if (result == null)
                    {   
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cCannot remove dependency, as this entry has no dependencies.");
                        break;
                    }

                    gui.updateSingleComponent(event.getWhoClicked(), event.getSlot(), generateAddDependencyButton(target, entry, gui));
                    break;
                default:
                    break;
            }

        },
            addDependencyLore.toArray(new String[addDependencyLore.size()])
        );
    }

    private GuiItemStack generateImportFromModuleButton(HumanEntity target, ModuleRepositoryEntry entry)
    {
        return SimpleComponentBuilder.build(Material.ENDER_CHEST, "§9§lImport from module", (event) -> {
            List<GuiComponent> data = new ArrayList<>();

            for (EdenModule module : Eden.getModuleManager().getAllModules())
                data.add(new ModuleImportButton(module, entry));

            new PagedSurfaceGui(54, "Import from module", event.getWhoClicked(), 0, data, List.of(CLOSE_BUTTON), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF).display(event.getWhoClicked());
        });
    }

    private static void openSync(ModuleRepositoryEntry entry, HumanEntity e)
    {
        Bukkit.getScheduler().runTask(Eden.getInstance(), () -> new EditRepositoryEntryGui(entry, e).display(e));
    }

    private static void askPropertyChange(HumanEntity target, ModuleRepositoryEntry entry, RepositoryEntryProperty property, String friendlyName)
    {
        target.closeInventory();
        PrintUtils.sendMessage(target, "Enter new " + friendlyName + " for this repository entry:");

        ChatUtils.expectChat(target, () -> {
            String input = ChatUtils.receiveResult(target);
            ChatUtils.deleteResult(target);

            entry.setProperty(property, input);
            entry.updateTimestamp();

            openSync(entry, target);
        });
    }

    private static class ModuleImportButton implements GuiComponent
    {
        private EdenModule module;
        private ModuleRepositoryEntry entry;

        public ModuleImportButton(EdenModule module, ModuleRepositoryEntry entry)
        {
            this.module = module;
            this.entry = entry;
        }

        @Override
        public GuiItemStack generate()
        {
            return SimpleComponentBuilder.build(module.getModIcon(), "§a§lImport from " + module.getName(), (event) -> {
                entry.setProperty(RepositoryEntryProperty.NAME, module.getName());
                entry.setProperty(RepositoryEntryProperty.DESCRIPTION, module.getDescription());
                entry.setProperty(RepositoryEntryProperty.AUTHOR, module.getMaintainer());
                entry.setProperty(RepositoryEntryProperty.VERSION, module.getVersion());

                new EditRepositoryEntryGui(entry, event.getWhoClicked()).display(event.getWhoClicked());
            },
                "§7§oImports name, description, author,",
                "§7§oand version from this loaded module.",
                "",
                "§7§oA URL and any dependencies will",
                "§7§ostill need to be specified manually."
            );
        }
    }
}

