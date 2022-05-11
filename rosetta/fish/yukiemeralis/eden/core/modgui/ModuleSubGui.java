package fish.yukiemeralis.eden.core.modgui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;
import fish.yukiemeralis.eden.surface2.GuiUtils;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
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

    private static GuiItemStack ENABLE_MODULE = SimpleComponentBuilder.build(Material.LIME_CONCRETE, "§r§a§lEnable module", (e) -> {

        },
        "§7§oAttempt to safely enable this module."
    );

    public ModuleSubGui(EdenModule module, HumanEntity target) 
    {
        super(27, "Rosetta -> " + module.getName() + " v" + module.getVersion(), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
        this.module = module;
    }

    @Override
    public void init(HumanEntity e, InventoryView view)
    {
        paintBlack();
        updateSingleComponent(e, 0, CLOSE_BUTTON);
        updateSingleComponent(e, 1, BACK_BUTTON);
        updateSingleComponent(e, 2, new ModuleGuiAdapter(module, (event) -> {}, true));
        updateSingleItem(e, 3, (module.getReliantModules().size() != 0 ? generateTreeItem() : GuiUtils.BLACK_PANE), false);

        GuiItemStack displayedButton = ENABLE_MODULE;
        if (!module.getIsEnabled())
        {
            updateSingleDataItem(e, GuiUtils.generateRectange(0, 1, 4, 3, displayedButton), false);
            return;
        }
        
        displayedButton = generateDisableActiveItem();

        Option<DisableData> option = isDisableDisallowed();
        if (module.getClass().isAnnotationPresent(PreventUnload.class) || option.getState().equals(OptionState.SOME))    
            displayedButton = generateDisableInactiveItem(option);

        updateSingleDataItem(e, GuiUtils.generateRectange(0, 1, 4, 3, displayedButton), false);
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

    private GuiItemStack generateDisableActiveItem()
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
