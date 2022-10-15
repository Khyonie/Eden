package coffee.khyonieheart.eden.module.java.enums;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Represents a type of user requesting a resource.
 * @author Yuki_emeralis
 */
public enum CallerToken 
{
	/** */
    EDEN(ConsoleCommandSender.class),
    /** */
    CONSOLE(ConsoleCommandSender.class),
    /** */
    PLAYER(Player.class)
    ;

    private final Class<? extends CommandSender> caller;

    private CallerToken(Class<? extends CommandSender> caller)
    {
        this.caller = caller;
    }

    /**
     * Converts a callertoken into a type of commandsender.
     * @return
     */
    public Class<? extends CommandSender> getCallerToken()
    {
        return this.caller;
    }

    /**
     * Generates a token based on the type of command sender given.
     * @param sender The command sender to handle
     * @return A token matching that of the sender
     */
    public static CallerToken fromCommandSender(CommandSender sender)
    {
        return sender instanceof ConsoleCommandSender ? CONSOLE : PLAYER;
    }

    /**
     * Checks if a token is of a specific "tier", starting from PLAYER as lowest, CONSOLE, and then EDEN.<p>
     * 
     * Used for rules specifying who and when a resource can be disabled or unloaded.
     * @param input
     * @param toCheckAgainst
     * @return True if the given input's tier is equal to or higher than the reference.
     */
    public static boolean isEqualToOrHigher(CallerToken input, CallerToken toCheckAgainst)
    {
        switch (toCheckAgainst)
        {
            case PLAYER:
                return true; // Players are the minimum requirement, and as CONSOLE/EDEN are higher than PLAYER, this will always be true
            case CONSOLE:
                return (input.equals(CONSOLE) || input.equals(EDEN)); // Assumes that if it's not CONSOLE or EDEN, it's PLAYER
            case EDEN:
                return (input.equals(EDEN));
            default:
                return false;
        }
    }
}
