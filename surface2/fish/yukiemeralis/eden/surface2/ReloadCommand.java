package fish.yukiemeralis.eden.surface2;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.java.ModuleManager;
import fish.yukiemeralis.eden.module.java.annotations.HideFromCollector;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;

import org.bukkit.command.CommandSender;

/**
 * Small reload command for debugging. Unsafe.
 */
@PreventUnload(CallerToken.EDEN)
@HideFromCollector
public class ReloadCommand extends EdenCommand
{
    public ReloadCommand(EdenModule parent_module) 
    {
        super("surreload", parent_module);
    }

    @EdenCommandHandler(usage = "surreload", description = "Reloads surface2.", argsCount = 0)
    public void edencommand_nosubcmd(CommandSender sender, String commandLabel, String[] args)
    {
        ModuleManager mm = Eden.getModuleManager();

        mm.disableModule("Surface2");
        mm.getModuleByName("Surface2").setDisabled();
        mm.removeModuleFromMemory("Surface2", CallerToken.EDEN);

        mm.loadSingleModule(mm.getReferences().get("Surface2"));
        mm.enableModule(mm.getModuleByName("Surface2"));
        mm.getModuleByName("Surface2").setEnabled();
    }
}