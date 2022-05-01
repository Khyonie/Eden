package fish.yukiemeralis.eden.core;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.command.annotations.HideFromEdenHelpall;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.java.ModuleDisableFailureData;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;
import fish.yukiemeralis.eden.utils.Option;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.Option.OptionState;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

@HideFromEdenHelpall
@PreventUnload(CallerToken.EDEN)
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
            
                Option<ModuleDisableFailureData> data = Eden.getModuleManager().disableModule(CoreModule.EDEN_DISABLE_REQUESTS.get(0).getModule().getName(), CallerToken.EDEN);
        
                if (data.getState().equals(OptionState.SOME))
                {
                    PrintUtils.sendMessage(sender, "§cFailed to disable EDEN-level module " + CoreModule.EDEN_DISABLE_REQUESTS.get(0).getModule().getName() + "! (Reason: " + data.unwrap().getReason().name() + ")");
                    
                    if (data.unwrap().getDownstreamModules().size() != 0)
                    {
                        StringBuilder builder = new StringBuilder();

                        for (EdenModule m : data.unwrap().getDownstreamModules())
                            builder.append(m.getName() + ", ");

                        builder.delete(builder.length() - 2, builder.length() - 1);

                        PrintUtils.sendMessage(sender, "§cDownstream " + PrintUtils.plural(data.unwrap().getDownstreamModules().size(), "module", "modules") + ": " + builder.toString());
                    }
                }

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
