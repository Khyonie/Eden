package com.yukiemeralis.blogspot.eden.core;

import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.command.EdenCommand;
import com.yukiemeralis.blogspot.eden.command.annotations.HideFromEdenHelpall;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

@HideFromEdenHelpall
public class ConfirmationCommand extends EdenCommand 
{
    public ConfirmationCommand(EdenModule parent_module) 
    {
        super("iknowwhatimdoingiswear", parent_module);
    }
    
    @EdenCommandHandler(usage = "iknowwhatimdoingiswear", description = "Used for disabling modules marked as EDEN.", argsCount = 0)
    public void edencommand_nosubcmd(CommandSender sender, String commandLabel, String[] args)
    {
        if (!(sender instanceof ConsoleCommandSender))
        {
            PrintUtils.sendMessage(sender, "§cThis command must be run in the console.");
            return;
        }

        if (CoreModule.EDEN_DISABLE_REQUESTS.size() == 0)
        {
            PrintUtils.sendMessage(sender, "§cNo EDEN module disable/unload requests available.");
            return;
        }

        switch (CoreModule.EDEN_DISABLE_REQUESTS.get(0).getType())
        {
            case 0:
                PrintUtils.sendMessage(sender, "§6Attempting to disable an EDEN-level module by elevating to EDEN...");
                Eden.getModuleManager().disableModule(CoreModule.EDEN_DISABLE_REQUESTS.get(0).getModule().getName(), CallerToken.EDEN);
        
                CoreModule.EDEN_WARN_DISABLE_REQUESTS.add(CoreModule.EDEN_DISABLE_REQUESTS.get(0).getModule().getName() + ":" + 0);
                break;
            case 1:
                PrintUtils.sendMessage(sender, "§6Attempting to unload an EDEN-level module by elevating to EDEN...");
                Eden.getModuleManager().removeModuleFromMemory(CoreModule.EDEN_DISABLE_REQUESTS.get(0).getModule().getName(), CallerToken.EDEN); //.disableModule(CoreModule.EDEN_DISABLE_REQUESTS.get(0).getModule().getName(), CallerToken.EDEN);
        
                CoreModule.EDEN_WARN_DISABLE_REQUESTS.add(CoreModule.EDEN_DISABLE_REQUESTS.get(0).getModule().getName() + ":" + 1);
                break;
            default:
                return;
        }

        CoreModule.EDEN_DISABLE_REQUESTS.remove(0);
    }
}
