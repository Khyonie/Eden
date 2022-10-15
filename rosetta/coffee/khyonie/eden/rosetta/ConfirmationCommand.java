package coffee.khyonie.eden.rosetta;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.command.EdenCommand;
import coffee.khyonieheart.eden.command.annotations.EdenCommandHandler;
import coffee.khyonieheart.eden.command.annotations.HideFromEdenHelpall;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.annotation.PreventUnload;
import coffee.khyonieheart.eden.module.java.ModuleDisableFailureData;
import coffee.khyonieheart.eden.module.java.enums.CallerToken;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.option.Option;
import coffee.khyonieheart.eden.utils.option.OptionState;

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

        if (Rosetta.EDEN_DISABLE_REQUESTS.size() == 0)
        {
            PrintUtils.sendMessage(sender, "§cNo EDEN module disable/unload requests available.");
            return;
        }

        switch (Rosetta.EDEN_DISABLE_REQUESTS.get(0).getType())
        {
            case 0:
                PrintUtils.sendMessage(sender, "§6Attempting to disable an EDEN-level module by elevating to EDEN...");
            
                Option data = Eden.getModuleManager().disableModule(Rosetta.EDEN_DISABLE_REQUESTS.get(0).getModule().getName(), CallerToken.EDEN);
        
                if (data.getState().equals(OptionState.SOME))
                {
                    ModuleDisableFailureData failure = data.unwrap(ModuleDisableFailureData.class);
                    PrintUtils.sendMessage(sender, "§cFailed to disable EDEN-level module " + Rosetta.EDEN_DISABLE_REQUESTS.get(0).getModule().getName() + "! (Reason: " + failure.getReason().name() + ")");
                    
                    if (failure.getDownstreamModules().size() != 0)
                    {
                        StringBuilder builder = new StringBuilder();

                        for (EdenModule m : failure.getDownstreamModules())
                            builder.append(m.getName() + ", ");

                        builder.delete(builder.length() - 2, builder.length() - 1);

                        PrintUtils.sendMessage(sender, "§cDownstream " + PrintUtils.plural(failure.getDownstreamModules().size(), "module", "modules") + ": " + builder.toString());
                    }
                }

                Rosetta.EDEN_WARN_DISABLE_REQUESTS.add(Rosetta.EDEN_DISABLE_REQUESTS.get(0).getModule().getName() + ":" + 0);
                break;
            case 1:
                PrintUtils.sendMessage(sender, "§6Attempting to unload an EDEN-level module by elevating to EDEN...");
                Eden.getModuleManager().removeModuleFromMemory(Rosetta.EDEN_DISABLE_REQUESTS.get(0).getModule().getName(), CallerToken.EDEN); //.disableModule(CoreModule.EDEN_DISABLE_REQUESTS.get(0).getModule().getName(), CallerToken.EDEN);
        
                Rosetta.EDEN_WARN_DISABLE_REQUESTS.add(Rosetta.EDEN_DISABLE_REQUESTS.get(0).getModule().getName() + ":" + 1);
                break;
            default:
                return;
        }

        Rosetta.EDEN_DISABLE_REQUESTS.remove(0);
    }
}
