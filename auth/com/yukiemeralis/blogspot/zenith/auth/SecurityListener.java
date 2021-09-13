package com.yukiemeralis.blogspot.zenith.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.permissions.PlayerData;
import com.yukiemeralis.blogspot.zenith.utils.ChatUtils;
import com.yukiemeralis.blogspot.zenith.utils.JsonUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

public class SecurityListener implements Listener
{
    @EventHandler
    public void onConnect(PlayerJoinEvent event)
    {
        PlayerData account = Zenith.getPermissionsManager().getPlayerData(event.getPlayer());
        Zenith.getPermissionsManager().addUserData(event.getPlayer(), account);
    }

    private static final List<Player> blockedPasswords = new ArrayList<>(); 

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (!SecurityCore.getModuleInstance().getConfig().get("blockPasswordsInChat").equals("true"))
            return;

        PlayerData account = Zenith.getPermissionsManager().getPlayerData(event.getPlayer());

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
        JsonUtils.toJsonFile("./plugins/Zenith/playerdata/" + event.getPlayer().getUniqueId() + ".json", Zenith.getPermissionsManager().getPlayerData(event.getPlayer()));
        Zenith.getPermissionsManager().removeUserData(event.getPlayer());
        Zenith.getPermissionsManager().removeElevatedUser(event.getPlayer());

        ChatUtils.forceNotify(event.getPlayer(), true);
        ChatUtils.deleteResult(event.getPlayer());
    }
}
