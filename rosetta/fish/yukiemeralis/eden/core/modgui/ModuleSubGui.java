package fish.yukiemeralis.eden.core.modgui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.annotation.PreventUnload;
import fish.yukiemeralis.eden.module.java.ModuleDisableFailureData;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.surface2.GuiUtils;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.utils.DataUtils;
import fish.yukiemeralis.eden.utils.ItemUtils;
import fish.yukiemeralis.eden.utils.Option;
import fish.yukiemeralis.eden.utils.Option.OptionState;
import fish.yukiemeralis.eden.utils.PrintUtils;

public class ModuleSubGui extends SurfaceGui
{
    private final EdenModule module;

    private static GuiItemStack CLOSE_BUTTON = SimpleComponentBuilder.build(Material.BARRIER, "§r§c§lClose", 
        (e) -> e.getWhoClicked().closeInventory(),
        "§7§oClose this menu."
    );

    private static GuiItemStack BACK_BUTTON = SimpleComponentBuilder.build(Material.RED_CONCRETE, "§r§c§lBack", 
        (e) -> new ModuleGui().display(e.getWhoClicked()), 
        "§7§oReturn to the main module manager", 
        "§7§oscreen."
    );

    private static ItemStack UNLOAD_INACTIVE_BUTTON = ItemUtils.build(
        Material.GRAY_CONCRETE, 
        "§r§8§lUnload module",
        "§7§oModules must be disabled first before",
        "§7§othey may be unloaded."
    );

    private static GuiItemStack ENABLE_MODULE;
    private static GuiItemStack UNLOAD_MODULE;

    public ModuleSubGui(EdenModule module, HumanEntity target) 
    {
        super(27, "Rosetta -> " + module.getName() + " v" + module.getVersion(), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
        this.module = module;
        paintBlack();

        ENABLE_MODULE = SimpleComponentBuilder.build(Material.LIME_CONCRETE, "§r§a§lEnable module", (e) -> {
                Eden.getModuleManager().enableModule(module);
                init(target, view(target)); 
            },
            "§7§oAttempt to safely enable this module."
        );

        UNLOAD_MODULE = SimpleComponentBuilder.build(Material.RED_CONCRETE, "§r§4§lUnload module", (e) -> {
                Eden.getModuleManager().removeModuleFromMemory(module.getName(), CallerToken.PLAYER);
                new ModuleGui().display(e.getWhoClicked());
            },
            "§c§oAttempt to remove this module from",
            "§c§othe server's physical memory.",
            "",
            "§7§oUnloaded modules cannot be enabled",
            "§7§oand must first be loaded back into",
            "§7§omemory.",
            "",
            "§7§oA reference will remain for easy",
            "§7§oreloading of this module."
        );
    }

    @Override
    public void init(HumanEntity e, InventoryView view)
    {
        updateSingleComponent(e, 0, CLOSE_BUTTON);
        updateSingleComponent(e, 1, BACK_BUTTON);
        updateSingleComponent(e, 2, new ModuleGuiAdapter(module, (event) -> {}, false));

        // Data items
        updateSingleItem(
            e, 
            3, 
            ItemUtils.build(
                Material.REPEATING_COMMAND_BLOCK, 
                "§r§2§lRegistered commands", 
                (module.getCommands().size() == 0 ?
                    new String[] {"§7§oNo commands are registered", "§7§oto this module."}
                :
                    DataUtils.mapList(module.getCommands(), (cmd) -> { return "§7§o/" + ((EdenCommand) cmd).getName(); })
                        .toArray(new String[module.getCommands().size()])
                )
            ), 
            false
        );
        updateSingleItem(e, 4, ItemUtils.build(Material.OBSERVER, "§r§2§lRegistered events", "§7§o" + module.getListeners().size() + PrintUtils.plural(module.getListeners().size(), " event", " events")), false);
        updateSingleItem(e, 5, (module.getReliantModules().size() != 0 ? generateTreeItem() : GuiUtils.BLACK_PANE), false);

        // Main buttons
        GuiItemStack displayedButton = ENABLE_MODULE;
        if (!module.getIsEnabled())
        {
            updateSingleDataComponent(e, GuiUtils.generateComponentRectangle(0, 1, 4, 3, displayedButton));
            updateSingleDataComponent(e, GuiUtils.generateComponentRectangle(5, 1, 9, 3, UNLOAD_MODULE));
            return;
        }

        displayedButton = generateDisableActiveItem(e);

        Option<DisableData> option = isDisableDisallowed();
        if (module.getClass().isAnnotationPresent(PreventUnload.class) || option.getState().equals(OptionState.SOME))    
            displayedButton = generateDisableInactiveItem(option);

        updateSingleDataComponent(e, GuiUtils.generateComponentRectangle(0, 1, 4, 3, displayedButton));
        updateSingleDataItem(e, GuiUtils.generateItemRectangle(5, 1, 9, 3, UNLOAD_INACTIVE_BUTTON), false);
    }

    public EdenModule getModule()
    {
        return this.module;
    }

    private GuiItemStack generateDisableInactiveItem(Option<DisableData> data)
    {
        List<String> description = new ArrayList<>();
        description.add("§7§oThis module cannot be disabled for the");
        description.add("§7§ofollowing reason:");
        description.add("");

        boolean additionalFlag = false;
        if (module.getClass().isAnnotationPresent(PreventUnload.class))
            if (!module.getClass().getAnnotation(PreventUnload.class).value().equals(CallerToken.PLAYER))
            {
                additionalFlag = true;
                description.add("§7§oThis module's disable policy of " + module.getClass().getAnnotation(PreventUnload.class).value().name() + " disallows");
                description.add("§7§ouser disable requests.");
            }
        
        description.add("");

        if (data.getState().equals(OptionState.SOME))
        {
            description.add((!additionalFlag ? "§7§oD" : "§7§oAdditionally, d") + "ependant module " + data.unwrap().getModule().getName() + "'s disable");
            description.add("§7§opolicy of " + data.unwrap().getToken() + " disallows user disable requests.");
            description.add("");        
        }

        
        description.add("§7§oDisable cannot be requested from here.");

        return SimpleComponentBuilder.build(Material.GRAY_CONCRETE, "§r§8§lDisable module", (e) -> {}, description.toArray(new String[description.size()]));
    }

    private GuiItemStack generateDisableActiveItem(HumanEntity target)
    {
        List<String> description = new ArrayList<>();
        description.add("§7§oAttempt to safely disable this module.");

        if (module.getReliantModules().size() != 0)
        {
            description.add("");
            description.add("§7§oThe following " + PrintUtils.plural(module.getReliantModules().size(), "module", "modules") + " will also be disabled:");
            
            for (EdenModule m : recurseDependencies(this.module, new ArrayList<>()))
                description.add("§7§o - " + m.getName() + " v" + m.getVersion());
        }


        return SimpleComponentBuilder.build(Material.YELLOW_CONCRETE, "§r§6§lDisable module", (e) -> {
                Option<ModuleDisableFailureData> option = Eden.getModuleManager().disableModule(module.getName());

                if (option.getState().equals(OptionState.SOME))
                {
                    PrintUtils.sendMessage(e.getWhoClicked(), "§cFailed to disable " + module.getName() + " v" + module.getVersion() + "! Failure: " + option.unwrap().getReason().name() + ", attempting rollback...");
                    if (option.unwrap().performRollback()) {
                        PrintUtils.sendMessage(e.getWhoClicked(), "§cAttempted rollback failed. Restarting the server or executing /eden restore may fix the problem.");
                        return;
                    }
                    PrintUtils.sendMessage(e.getWhoClicked(), "Rollback complete.");
                }
                module.setDisabled();
                init(target, view(target));
            },
            description.toArray(new String[description.size()])
        );
    }

    private ItemStack generateTreeItem()
    {
        List<String> description = new ArrayList<>();
        description.add("§7§oThe following " + PrintUtils.plural(module.getReliantModules().size(), "module", "modules") + " depend on");
        description.add("§7§othis module:");

        for (EdenModule m : recurseDependencies(this.module, new ArrayList<>()))
            description.add("§7§o - " + m.getName() + " v" + m.getVersion() + " (" + (module.getReliantModules().contains(m) ? "direct)" : "indirect)"));
    
        return ItemUtils.build(Material.OAK_SAPLING, "§r§9§lDependants", description.toArray(new String[description.size()]));
    }

    private Option<DisableData> isDisableDisallowed()
    {
        Option<DisableData> option = new Option<>(DisableData.class);

        for (EdenModule m : recurseDependencies(module, new ArrayList<>()))
            if (m.getClass().isAnnotationPresent(PreventUnload.class))
            {
                if (!m.getClass().getAnnotation(PreventUnload.class).value().equals(CallerToken.PLAYER))
                {
                    return option.some(new DisableData(m, m.getClass().getAnnotation(PreventUnload.class).value().name()));
                }
            }

        return option;
    }

    private List<EdenModule> recurseDependencies(EdenModule m, List<EdenModule> data)
    {
        if (data.contains(m))
            return data;
        
        if (!m.equals(this.module))
           data.add(m);

        for (EdenModule dep : m.getReliantModules())
            recurseDependencies(dep, data);

        return data;
    }

    private static class DisableData
    {
        private final EdenModule m;
        private final String token;

        public DisableData(EdenModule m, String token)
        {
            this.m = m;
            this.token = token;
        }

        public EdenModule getModule()
        {
            return this.m;
        }

        public String getToken()
        {
            return this.token;
        }
    }
}
