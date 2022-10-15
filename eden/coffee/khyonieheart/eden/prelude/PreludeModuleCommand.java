package coffee.khyonieheart.eden.prelude;

import java.util.List;

import org.bukkit.command.CommandSender;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.command.EdenCommand;
import coffee.khyonieheart.eden.command.annotations.EdenCommandHandler;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.annotation.HideFromCollector;
import coffee.khyonieheart.eden.module.java.ModuleDisableFailureData;
import coffee.khyonieheart.eden.module.java.enums.CallerToken;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.option.Option;
import coffee.khyonieheart.eden.utils.result.Result;

/**
 * Simple module manager interface command.
 */
@HideFromCollector
public class PreludeModuleCommand extends EdenCommand
{
    /**
     * Module command constructor
     * @param parent_module
     */
    public PreludeModuleCommand(EdenModule parent_module) 
    {
        super("pre", List.of("prelude"), parent_module);

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
        if (Eden.getModuleManager().hasReferenceTo(args[1]))
        {
            if (Eden.getModuleManager().isModulePresent(args[1]))
            {
                PrintUtils.sendMessage(sender, "§cA named \"" + args[1] + "\" is already loaded.");
                return;
            }

            Result result = Eden.getModuleManager().loadSingleModule(Eden.getModuleManager().getReferences().get(args[1]));

            if (result.isErr())
            {
                PrintUtils.sendMessage(sender, "§cFailed to load module reference for \"" + args[1] + "\". Reason: " + result.unwrapErr(String.class));
                return;
            }

            PrintUtils.sendMessage(sender, "Reference load complete.");
            return;
        }

        String path = "./plugins/Eden/mods/" + args[1] + ".jar";
        Result result = Eden.getModuleManager().loadSingleModule(path);

        if (result.isErr())
        {
            PrintUtils.sendMessage(sender, "§cFailed to load module \"" + args[1] + "\". Reason: " + result.unwrapErr(String.class));
            return;
        }

        PrintUtils.sendMessage(sender, "Load complete.");
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
        EdenModule target = Eden.getModuleManager().getDisabledModuleByName(args[1]);

        if (target == null)
        {
            PrintUtils.sendMessage(sender, "§cNo disabled module by that name was found.");
            return;
        }

        Eden.getModuleManager().removeModuleFromMemory(target.getName(), CallerToken.fromCommandSender(sender));
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
