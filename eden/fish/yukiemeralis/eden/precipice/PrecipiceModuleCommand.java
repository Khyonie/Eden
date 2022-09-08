package fish.yukiemeralis.eden.precipice;

import java.util.List;

import org.bukkit.command.CommandSender;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.command.annotations.EdenCommandHandler;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.annotation.HideFromCollector;
import fish.yukiemeralis.eden.utils.PrintUtils;

/**
 * Simple module manager interface command.
 */
@HideFromCollector
public class PrecipiceModuleCommand extends EdenCommand
{
    /**
     * Module command constructor
     * @param parent_module
     */
    public PrecipiceModuleCommand(EdenModule parent_module) 
    {
        super("pre", List.of("prelude", "precipice"), parent_module);

        this.addBranch("^load", "^unload", "^enable", "^disable", "^reload").addBranch("<ALL_MODULES>");
    }

    /**
     * Load command
     * @param sender
     * @param commandLabel
     * @param args
     */
    @EdenCommandHandler(usage = "pre load <module>", description = "Loads a module.", argsCount = 1)
    public void edencommand_load(CommandSender sender, String commandLabel, String[] args)
    {
        
    }

    /**
     * Unload command
     * @param sender
     * @param commandLabel
     * @param args
     */
    @EdenCommandHandler(usage = "pre unload <module>", description = "Unloads a disabled module.", argsCount = 1)
    public void edencommand_unload(CommandSender sender, String commandLabel, String[] args)
    {

    }

    /**
     * Enable command
     * @param sender
     * @param commandLabel
     * @param args
     */
    @EdenCommandHandler(usage = "pre enable <module>", description = "Enables a disabled module.", argsCount = 1)
    public void edencommand_enable(CommandSender sender, String commandLabel, String[] args)
    {

    }

    /**
     * Disable command
     * @param sender
     * @param commandLabel
     * @param args
     */
    @EdenCommandHandler(usage = "pre disable <module>", description = "Disables an enabled module.", argsCount = 1)
    public void edencommand_disable(CommandSender sender, String commandLabel, String[] args)
    {

    }

    /**
     * Reload command
     * @param sender
     * @param commandLabel
     * @param args
     */
    @EdenCommandHandler(usage = "pre reload <module>", description = "Reloads a module, retaining its enabled/disabled state.", argsCount = 1)
    public void edencommand_reload(CommandSender sender, String commandLabel, String[] args)
    {
        if (Eden.getModuleManager().forceReload(args[1], false, false, false))
        {
            PrintUtils.sendMessage(sender, "Reload success.");
            return;
        }

        PrintUtils.sendMessage(sender, "§cReload failed. See console for details.");
    }
}
