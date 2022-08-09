package fish.yukiemeralis.eden.auth;

import org.bukkit.command.CommandSender;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.command.annotations.EdenCommandHandler;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;

public class UuidBanCommand extends EdenCommand
{
    public UuidBanCommand(EdenModule parent_module) 
    {
        super("uuidban", parent_module);

        this.addBranch("^add", "^remove");
        this.getBranch("^add").addBranch("<ALL_PLAYERS>").addBranch("<REASON>");
        this.getBranch("^remove").addBranch("<UUID_BANNED_PLAYERS>");
    }

    @EdenCommandHandler(usage = "uuidban add <PLAYER> <REASON>", description = "Issue a UUID ban to a player.", argsCount = 3)
    public void edencommand_add(CommandSender sender, String commandLabel, String[] args)
    {
        String reason = ChatUtils.concatStringArgs(args, " ", 2);
        boolean result = SecurityCore.banUuid(Eden.getInstance().getServer().getPlayerExact(args[1]), reason, reason);

        if (result)
        {
            PrintUtils.sendMessage(sender, "UUID-banned §c" + args[1] + "§7 with reason \"§e" + reason + "§7\".");
            return;
        }

        PrintUtils.sendMessage(sender, "Player \"§c" + args[1] + "§7\" is already UUID-banned. Reason: \"§e" + SecurityCore.isBanned(Eden.getInstance().getServer().getPlayerExact(args[1])).unwrap(UuidBanEntry.class).getBanMessage() + "§7\".");
    }

    @EdenCommandHandler(usage = "uuidban remove <PLAYER>", description = "Pardon a UUID-banned player.", argsCount = 2)
    public void edencommand_remove(CommandSender sender, String commandLabel, String[] args)
    {
        boolean result = SecurityCore.pardonUuid(args[1]);

        if (result)
        {
            PrintUtils.sendMessage(sender, "§aPardoned §e" + args[1] + "§a.");
            return;
        }

        PrintUtils.sendMessage(sender, "§cPlayer \"§e" + args[1] + "§c\" is not UUID-banned.");
    }
}
