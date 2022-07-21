package fish.yukiemeralis.eden.auth;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.permissions.PlayerData;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.Option;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.Option.OptionState;

/**
 * Handler for various smaller security tasks.
 */
public class SecurityListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onConnect(PlayerJoinEvent event)
    {
        Option<UuidBanEntry> isBanned = SecurityCore.isBanned(event.getPlayer());
        if (isBanned.getState().equals(OptionState.SOME))
        {
            event.getPlayer().kickPlayer(isBanned.unwrap().getBanMessage());
            event.setJoinMessage("§8[§4§l✕§r§8] §c" + event.getPlayer().getName() + "§7 attempted to connect, but is banned.");
            return;
        }

        PlayerData account = Eden.getPermissionsManager().getPlayerData(event.getPlayer());
        Eden.getPermissionsManager().addUserData(event.getPlayer(), account);
    }

    private static final List<Player> blockedPasswords = new ArrayList<>(); 

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (!SecurityCore.getModuleInstance().getConfig().getBoolean("blockPasswordsInChat"))
            return;

        PlayerData account = Eden.getPermissionsManager().getPlayerData(event.getPlayer());

        if (event.isCancelled())
            return;

        if (!account.hasPassword())
            return;

        if (!account.comparePassword(event.getMessage()))
        {
            if (blockedPasswords.contains(event.getPlayer()))
                blockedPasswords.remove(event.getPlayer());
            return;
        }

        if (blockedPasswords.contains(event.getPlayer()))
        {
            blockedPasswords.remove(event.getPlayer());
            return;
        }

        PrintUtils.sendMessage(event.getPlayer(), "Careful, your chat message matches your password! If you truly wish to send this message, simply send it again.");
        blockedPasswords.add(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event)
    {
        JsonUtils.toJsonFile("./plugins/Eden/playerdata/" + event.getPlayer().getUniqueId() + ".json", Eden.getPermissionsManager().getPlayerData(event.getPlayer()));
        Eden.getPermissionsManager().removeUserData(event.getPlayer());
        Eden.getPermissionsManager().removeElevatedUser(event.getPlayer());

        ChatUtils.forceNotify(event.getPlayer(), true);
        ChatUtils.deleteResult(event.getPlayer());
    }
}
