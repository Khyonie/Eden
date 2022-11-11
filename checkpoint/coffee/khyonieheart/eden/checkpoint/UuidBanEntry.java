package coffee.khyonieheart.eden.checkpoint;

import com.google.gson.annotations.Expose;

import org.bukkit.entity.Player;

/**
 * An instance of a UUID ban to be loaded on checkpoint startup.
 * @author Khyonie
 * @since 1.5.0
 */
public class UuidBanEntry 
{
    @Expose
    private String uuid, username, banMessage;

    /**
     * An instance of a UUID ban entry, with a UUID, username, and a ban message
     * @param uuid
     * @param username
     * @param banMessage
     */
    public UuidBanEntry(String uuid, String username, String banMessage)
    {
        this.username = username;
        this.banMessage = banMessage;
        this.uuid = uuid;
    }

    /**
     * An instance of a UUID ban entry, with a player
     * @param player
     * @param banMessage
     */
    public UuidBanEntry(Player player, String banMessage)
    {
        this(player.getUniqueId().toString(), player.getName(), banMessage);
    }

    /**
     * Obtains the UUID this ban is issued to
     * @return This ban's target UUID
     */
    public String getUuid()
    {
        return this.uuid;
    }

    /**
     * Obtains the username this ban is issued to
     * @return This ban target's username
     */
    public String getUsername()
    {
        return this.username;
    }

    /**
     * Obtains the ban message to be sent whenever a banned player attempts to join
     * @return This entry's ban message
     */
    public String getBanMessage()
    {
        return this.banMessage;
    }
}
