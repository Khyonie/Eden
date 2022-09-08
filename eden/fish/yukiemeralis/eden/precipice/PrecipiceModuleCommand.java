package fish.yukiemeralis.eden.precipice;

import java.util.List;

import org.bukkit.command.CommandSender;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.command.annotations.EdenCommandHandler;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.annotation.HideFromCollector;
import fish.yukiemeralis.eden.module.java.ModuleDisableFailureData;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.option.Option;

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
        if (Eden.getModuleManager().getModuleByName(args[1]) == null)
        {
            PrintUtils.sendMessage(sender, "§cNo module by that name was found.");
            return;
        }
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
        if (Eden.getModuleManager().getDisabledModuleByName(args[1]) == null)
        {
            PrintUtils.sendMessage(sender, "§cNo disabled module by that name was found.");
            return;
        }
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
        EdenModule mod = Eden.getModuleManager().getDisabledModuleByName(args[1]);
        if (mod == null)
        {
            PrintUtils.sendMessage(sender, "§cNo module by that name was found.");
            return;
        }

        try {
            Eden.getModuleManager().enableModule(mod);
            mod.setEnabled();
        } catch (Exception e) {
            PrintUtils.sendMessage(sender, "§cFailed to enabled " + args[1] + ". Reason: " + e.getMessage());
            return;
        }

        PrintUtils.sendMessage(sender, "Enable complete.");
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
        if (Eden.getModuleManager().getEnabledModuleByName(args[1]) == null)
        {
            PrintUtils.sendMessage(sender, "§cNo enabled module by that name was found.");
            return;
        }

        Option opt = Eden.getModuleManager().disableModule(args[1], CallerToken.fromCommandSender(sender));
        switch (opt.getState())
        {
            case SOME:
                PrintUtils.sendMessage(sender, "§cFailed to disable " + args[1] + ". Reason: " + opt.unwrap(ModuleDisableFailureData.class));
                if (!opt.unwrap(ModuleDisableFailureData.class).performRollback())
                {
                    PrintUtils.sendMessage(sender, "§cRollback failed.");
                    return;
                }

                PrintUtils.sendMessage(sender, "§cRollback complete.");
                return;
            default: break;
        }

        PrintUtils.sendMessage(sender, "Disable complete.");
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
        if (Eden.getModuleManager().getModuleByName(args[1]) == null)
        {
            PrintUtils.sendMessage(sender, "§cNo module by that name was found.");
            return;
        }

        if (Eden.getModuleManager().forceReload(args[1], false, false, false))
        {
            PrintUtils.sendMessage(sender, "Reload success.");
            return;
        }

        PrintUtils.sendMessage(sender, "§cReload failed. See console for details.");
    }
}
