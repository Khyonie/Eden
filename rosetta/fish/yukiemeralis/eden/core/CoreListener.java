package fish.yukiemeralis.eden.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.java.annotations.Branch;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;

@PreventUnload(CallerToken.EDEN)
public class CoreListener implements Listener
{
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if (CoreModule.getModuleInstance().getConfig().getBoolean("prettyLoginMessage"))
            event.setJoinMessage("§8[§a§l→§r§8] §7" + event.getPlayer().getName());

        if (!CoreModule.getModuleInstance().getConfig().getBoolean("loginGreeting"))
            return;

        PrintUtils.sendMessage(event.getPlayer(), "Welcome " + event.getPlayer().getDisplayName() + "! This server is running Eden " + VersionCtrl.getVersion() + ".");

        if (CoreModule.getModuleInstance().getConfig().getBoolean("warnIfNotRelease"))
            switch (Eden.getInstance().getClass().getAnnotation(Branch.class).value())
            {
                case FEATURE:
                    PrintUtils.sendMessage(event.getPlayer(), "§cYou are using a feature build, one or more features will be work-in-progress.");
                    break;
                case RELEASE_CANDIDATE:
                    PrintUtils.sendMessage(event.getPlayer(), "§cYou are using a release candidate build, report all problems to https://github.com/YukiEmeralis/Eden/issues.");
                    break;
                default:
                    break;
            }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event)
    {
        if (CoreModule.getModuleInstance().getConfig().getBoolean("prettyLoginMessage"))
            event.setQuitMessage("§8[§c§l←§r§8] §7" + event.getPlayer().getName());
    }

    @EventHandler
    public void onConfigChange(EdenConfigChangeEvent e)
    {
        PrintUtils.log("Config for (" + e.getModule().getName() + ") has changed! Key: {" + e.getKey() + "}, value: [" + e.getOldValue() + "] -> [" + e.getValue() + "]", InfoType.INFO);
    }
}
