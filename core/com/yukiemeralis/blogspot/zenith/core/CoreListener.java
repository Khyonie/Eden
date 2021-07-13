package com.yukiemeralis.blogspot.zenith.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.Branch;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

@PreventUnload(CallerToken.ZENITH)
public class CoreListener implements Listener
{
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if (Boolean.valueOf(Zenith.getModuleManager().getEnabledModuleByName("Zenith").getConfig().get("prettyLoginMessage")))
            event.setJoinMessage("§8[§a§l→§r§8] §7" + event.getPlayer().getName());

        if (!Boolean.valueOf(Zenith.getModuleManager().getEnabledModuleByName("Zenith").getConfig().get("loginGreeting")))
            return;

        PrintUtils.sendMessage(event.getPlayer(), "Welcome " + event.getPlayer().getDisplayName() + "! This server is running ZenithCore " + VersionCtrl.getVersion() + ".");

        if (Boolean.valueOf(Zenith.getModuleManager().getEnabledModuleByName("Zenith").getConfig().get("warnIfNotRelease")))
            switch (Zenith.getInstance().getClass().getAnnotation(Branch.class).value())
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
        if (Boolean.valueOf(Zenith.getModuleManager().getEnabledModuleByName("Zenith").getConfig().get("prettyLoginMessage")))
            event.setQuitMessage("§8[§c§l←§r§8] §7" + event.getPlayer().getName());
    }

    @EventHandler
    public void onConfigChange(ZenithConfigChangeEvent e)
    {
        PrintUtils.log("§8Config for " + e.getModule().getName() + " has changed! §aKey§7: §b" + e.getKey() + "§7, §avalue§7: §b" + e.getOldValue() + " §e-> §b" + e.getValue(), InfoType.INFO);
    }
}
