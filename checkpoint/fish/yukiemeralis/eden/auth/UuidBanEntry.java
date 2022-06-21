package fish.yukiemeralis.eden.auth;

import com.google.gson.annotations.Expose;

import org.bukkit.entity.Player;

public class UuidBanEntry 
{
    @Expose
    private String uuid, username, banMessage;

    public UuidBanEntry(String uuid, String username, String banMessage)
    {
        this.username = username;
        this.banMessage = banMessage;
        this.uuid = uuid;
    }

    public UuidBanEntry(Player player, String banMessage)
    {
        this(player.getUniqueId().toString(), player.getName(), banMessage);
    }

    public String getUuid()
    {
        return this.uuid;
    }

    public String getUsername()
    {
        return this.username;
    }

    public String getBanMessage()
    {
        return this.banMessage;
    }
}
