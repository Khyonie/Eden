package coffee.khyonieheart.eden.checkpoint;

import org.bukkit.command.CommandSender;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.command.EdenCommand;
import coffee.khyonieheart.eden.command.annotations.EdenCommandHandler;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.utils.ChatUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;

/**
 * UUID ban command for /uuidban
 * @author Yuki_emeralis
 * @since 1.5.0
 */
public class UuidBanCommand extends EdenCommand
{
    /**
     * Command constructor
     * @param parent_module
     */
    public UuidBanCommand(EdenModule parent_module) 
    {
        super("uuidban", parent_module);

        this.addBranch("^add", "^remove");
        this.getBranch("^add").addBranch("<ALL_PLAYERS>").addBranch("<REASON>");
        this.getBranch("^remove").addBranch("<UUID_BANNED_PLAYERS>");
    }

    /**
     * /uuidban add <PLAYER> <REASON>
     * @param sender
     * @param commandLabel
     * @param args
     */
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

    /**
     * /uuidban remove <PLAYER>
     * @param sender
     * @param commandLabel
     * @param args
     */
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
