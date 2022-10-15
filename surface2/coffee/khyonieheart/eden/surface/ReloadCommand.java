package fish.yukiemeralis.eden.surface2;

import org.bukkit.command.CommandSender;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.command.annotations.EdenCommandHandler;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.annotation.HideFromCollector;
import fish.yukiemeralis.eden.module.annotation.PreventUnload;
import fish.yukiemeralis.eden.module.java.ModuleManager;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;

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

        mm.disableModule("Surface");
        mm.getModuleByName("Surface").setDisabled();
        mm.removeModuleFromMemory("Surface", CallerToken.EDEN);

        mm.loadSingleModule(mm.getReferences().get("Surface"));
        mm.enableModule(mm.getModuleByName("Surface"));
        mm.getModuleByName("Surface").setEnabled();
    }
}