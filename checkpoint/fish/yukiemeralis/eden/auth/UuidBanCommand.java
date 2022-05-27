package fish.yukiemeralis.eden.auth;

import org.bukkit.command.CommandSender;

import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.module.EdenModule;

public class UuidBanCommand extends EdenCommand
{
    public UuidBanCommand(EdenModule parent_module) 
    {
        super("uuidban", parent_module);

        this.addBranch("^add", "^remove");
        this.getBranch("^add").addBranch("<ALL_PLAYERS>", "<REASON>");
        this.getBranch("^remove").addBranch("<UUID_BANNED_PLAYERS>");
    }

    @EdenCommandHandler(usage = "uuidban add <PLAYER> <REASON>", description = "Issue a UUID ban to a player.", argsCount = 3)
    public void edencommand_add(CommandSender sender, String commandLabel, String[] args)
    {

    }

    @EdenCommandHandler(usage = "uuidban remove <PLAYER>", description = "Pardon a UUID-banned player.", argsCount = 2)
    public void edenocommmand_remove(CommandSender sender, String commandLabel, String[] args)
    {

    }
}
