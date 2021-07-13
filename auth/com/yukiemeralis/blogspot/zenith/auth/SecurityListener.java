package com.yukiemeralis.blogspot.zenith.auth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.yukiemeralis.blogspot.zenith.utils.ChatUtils;
import com.yukiemeralis.blogspot.zenith.utils.JsonUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

public class SecurityListener implements Listener
{
    @EventHandler
    public void onConnect(PlayerJoinEvent event)
    {
        PlayerData account = SecurityCore.getPlayerData(event.getPlayer());

        SecurityCore.active_players.put(event.getPlayer(), account);
        
        // Handle auto-login
        if (account.isAutoLogin())
        {
            if (Permissions.getAccounts().containsKey(account.getAutologinUsername()))
            {
                // Check expected IP against connected IP
                if (account.getExpectedIP().equals(event.getPlayer().getAddress().getAddress().getHostAddress()))
                {
                    // Check password against account key
                    SecurePlayerAccount sec_account = Permissions.getAccounts().get(account.getAutologinUsername());

                    if (account.getAutologinKey() != PlayerData.generateKey(sec_account.getPassword()))
                    {
                        PrintUtils.sendMessage(event.getPlayer(), "§cAttempted to auto-login to secure account \"" + account.getAutologinUsername() + "\" but the stored key is incorrect! Please re-enable auto-login.");
                        account.disableAutoLogin();
                    } else {
                        PrintUtils.sendMessage(event.getPlayer(), "§aAutomatically logged into account \"" + sec_account.getUsername() + "\".");
                        Permissions.login(event.getPlayer(), account.getAutologinUsername());
                    }
                } else {
                    PrintUtils.sendMessage(event.getPlayer(), "§cConnection from new location, you have not been automatically logged in.");
                }
            } else {
                PrintUtils.sendMessage(event.getPlayer(), "§cAttempted to auto-login to secure account \"" + account.getAutologinUsername() + "\" but the account is missing!");
                account.disableAutoLogin();
            }
        }

        if (Permissions.isAuthorized(event.getPlayer(), 3) && SecurityCore.getAccountRequests().size() > 0)
        {
            PrintUtils.sendMessage(event.getPlayer(), "§eThere are new account requests! Please view, and approve or reject them.");
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event)
    {
        Permissions.logout(event.getPlayer());

        JsonUtils.toJsonFile("./plugins/Zenith/playerdata/" + event.getPlayer().getUniqueId() + ".json", SecurityCore.getPlayerData(event.getPlayer()));
        SecurityCore.active_players.remove(event.getPlayer());

        ChatUtils.forceNotify(event.getPlayer(), true);
        ChatUtils.deleteResult(event.getPlayer());
    }
}
