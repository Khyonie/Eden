package coffee.khyonieheart.eden.surface;

import org.bukkit.command.CommandSender;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.command.EdenCommand;
import coffee.khyonieheart.eden.command.annotations.EdenCommandHandler;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.annotation.HideFromCollector;
import coffee.khyonieheart.eden.module.annotation.PreventUnload;
import coffee.khyonieheart.eden.module.java.ModuleManager;
import coffee.khyonieheart.eden.module.java.enums.CallerToken;

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