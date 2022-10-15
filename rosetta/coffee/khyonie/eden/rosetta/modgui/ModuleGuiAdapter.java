package coffee.khyonie.eden.rosetta.modgui;


import org.bukkit.event.inventory.InventoryClickEvent;

import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.EdenModule.LoadBefore;
import coffee.khyonieheart.eden.module.annotation.PreventUnload;
import coffee.khyonieheart.eden.module.java.enums.CallerToken;
import coffee.khyonieheart.eden.surface.SimpleComponentBuilder.SimpleComponentAction;
import coffee.khyonieheart.eden.surface.component.GuiComponent;
import coffee.khyonieheart.eden.surface.component.GuiItemStack;
import coffee.khyonieheart.eden.utils.ChatUtils;
import coffee.khyonieheart.eden.utils.ItemUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;

/**
 * Adapter class for Eden modules to be Surface-compatible.
 */
public class ModuleGuiAdapter implements GuiComponent
{
    private final EdenModule module;
    private final SimpleComponentAction action;
    private final boolean isSubGui;

    public ModuleGuiAdapter(EdenModule module, SimpleComponentAction action, boolean isSubGui)
    {
        this.module = module;
        this.action = action;
        this.isSubGui = isSubGui;
    }

    public ModuleGuiAdapter(EdenModule module, SimpleComponentAction action)
    {
        this(module, action, false);
    }

    public EdenModule getModule()
    {
        return this.module;
    }

    @Override
    public GuiItemStack generate()
    {
        try {
            String dpDescription = "- Module can be safely disabled by any";
            String dpDescription2 = " §7§o means.";
            
            if (module.getClass().isAnnotationPresent(PreventUnload.class))
            {
                switch (module.getClass().getAnnotation(PreventUnload.class).value())
                {
                    case CONSOLE:
                        dpDescription  = "- Module can be safely disabled by";
                        dpDescription2 = " §7§o the console.";
                        break;
                    case EDEN:
                        dpDescription  = "- Module can only be disabled by internal";
                        dpDescription2 = " §7§o Eden processes.";
                        break;
                    default:
                    case PLAYER:
                        break;
                }
            }

            String reliantColor = "";
            for (EdenModule m : module.getReliantModules())
                if (m.getClass().isAnnotationPresent(PreventUnload.class))
                    if (!m.getClass().getAnnotation(PreventUnload.class).value().equals(CallerToken.PLAYER))
                    {
                        dpDescription = "- §6§oModule cannot be disabled safely from here, as";
                        dpDescription2 = " §6§o " + m.getClass().getAnnotation(PreventUnload.class).value().name() + "-level module \"" + m.getName() + "\" would prevent it.";
                        reliantColor = "§e§o";
                        break;
                    }

            return new GuiItemStack(
                ItemUtils.build(
                    module.getModIcon(), 
                    "§r" + ChatUtils.of("DD73CE") + "§l" + module.getName(), 
                    "§7§o\"" + module.getDescription() + "\"",
                    "§7§oVersion: " + module.getVersion(),
                    "§7§oMaintainer: " + module.getMaintainer(),
                    "",
                    "§7§o" + module.getCommands().size() + " " + PrintUtils.plural(module.getCommands().size(), "command, ", "commands, ") + module.getListeners().size() + " " + PrintUtils.plural(module.getListeners().size(), "listener", "listeners"),
                    "§7§o" + (module.getReliantModules().size() == 0 ? "No other modules depend on this module" : module.getReliantModules().size() + " " + PrintUtils.plural(module.getReliantModules().size(), "module ", "modules ") + "depend on this module"),
                    "§7§o" + (!module.getClass().isAnnotationPresent(LoadBefore.class) ? "This module does not have any dependencies" : "This module depends on " + module.getClass().getAnnotation(LoadBefore.class).loadBefore().length + " other " + PrintUtils.plural(module.getClass().getAnnotation(LoadBefore.class).loadBefore().length, "module", "modules")),
                    "",
                    "§7§oDisable policy: " + reliantColor + (!module.getClass().isAnnotationPresent(PreventUnload.class) ? "N/A" : module.getClass().getAnnotation(PreventUnload.class).value().name()),
                    "§7§o" + dpDescription,
                    "§7§o" + dpDescription2,
                    "",
                    "§7§oStatus: " + (module.getIsEnabled() ? "§aEnabled" : "§cDisabled"),
                    "",
                    ChatUtils.of("808080") + (!isSubGui ? "§oClick for more details." : "§oYou are already viewing this module.")
                )
            ) {
                @Override
                public void onInteract(InventoryClickEvent event)
                {
                    getInstance().onInteract(event); // No stackoverflows, thank you very much 
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }

    private ModuleGuiAdapter getInstance()
    {
        return this;
    }

    @Override
    public void onInteract(InventoryClickEvent event)
    {
        this.action.onInteract(event);
    }
}
