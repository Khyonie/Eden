package com.yukiemeralis.blogspot.eden.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.module.java.annotations.Branch;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

@PreventUnload(CallerToken.EDEN)
public class CoreListener implements Listener
{
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if (Boolean.valueOf(Eden.getModuleManager().getEnabledModuleByName("Eden").getConfig().get("prettyLoginMessage")))
            event.setJoinMessage("§8[§a§l→§r§8] §7" + event.getPlayer().getName());

        if (!Boolean.valueOf(Eden.getModuleManager().getEnabledModuleByName("Eden").getConfig().get("loginGreeting")))
            return;

        PrintUtils.sendMessage(event.getPlayer(), "Welcome " + event.getPlayer().getDisplayName() + "! This server is running Eden " + VersionCtrl.getVersion() + ".");

        if (Boolean.valueOf(Eden.getModuleManager().getEnabledModuleByName("Eden").getConfig().get("warnIfNotRelease")))
            switch (Eden.getInstance().getClass().getAnnotation(Branch.class).value())
            {
                case NIGHTLY: // Most unstable - Features may not be fleshed out, and showstopper grade bugs may be present.
                    PrintUtils.sendMessage(event.getPlayer(), "§cYou are using a nightly build, expect many bugs and unfinished features.");
                    break;
                case BETA: // Second most unstable - No immediate showstopper bugs present, features may not be finished.
                    PrintUtils.sendMessage(event.getPlayer(), "§cYou are using a beta build, please report all bugs to Yuki_emeralis.");
                    break;
                case RELEASE_CANDIDATE: // Second most stable - No immediate showstopper bugs present, all features present.
                    PrintUtils.sendMessage(event.getPlayer(), "§cYou are using a release candidate build, bugs are unlikely but still possible. Please report all issues to Yuki_emeralis.");
                    break;
                default:
                    break;
            }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event)
    {
        if (Boolean.valueOf(Eden.getModuleManager().getEnabledModuleByName("Eden").getConfig().get("prettyLoginMessage")))
            event.setQuitMessage("§8[§c§l←§r§8] §7" + event.getPlayer().getName());
    }

    @EventHandler
    public void onConfigChange(EdenConfigChangeEvent e)
    {
        PrintUtils.log("Config for (" + e.getModule().getName() + ") has changed! Key: {" + e.getKey() + "}, value: [" + e.getOldValue() + "] -> [" + e.getValue() + "]", InfoType.INFO);
    }
}
