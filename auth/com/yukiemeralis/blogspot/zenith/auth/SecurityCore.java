package com.yukiemeralis.blogspot.zenith.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ModInfo;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ZenConfig;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.DefaultConfig;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.zenith.utils.ChatUtils;
import com.yukiemeralis.blogspot.zenith.utils.FileUtils;
import com.yukiemeralis.blogspot.zenith.utils.JsonUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

import org.bukkit.Material;

@ModInfo (
    modName = "ZenithAuth",
    description = "Provides permissions for users and commands.",
    modFamily = "Zenith base modules",
    version = "1.4.0",
    modIcon = Material.IRON_BARS,
    maintainer = "Yuki_emeralis",
    supportedApiVersions = {"v1_16_R3", "v1_17_R1"}
)
@PreventUnload(CallerToken.ZENITH)
@ZenConfig
@DefaultConfig(
    keys =   {"notifyElevate", "blockPasswordsInChat"},
    values = {"true",          "true"}
)
public class SecurityCore extends ZenithModule
{
    private static List<String> security_log = new ArrayList<>();

    private static ZenithModule module;

    public SecurityCore()
    {
        addListener(
            new ChatUtils()
        );
    }

    @Override
    public void onEnable() 
    {
        FileUtils.ensureFolder("./plugins/Zenith/playerdata/");
        FileUtils.ensureFolder("./plugins/Zenith/permissiongroups/");

        module = this;
        Zenith.setPermissionsManager(new ZenithPermissionManager());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() 
    {
        if (Zenith.getPermissionsManager() instanceof ZenithPermissionManager)
        {
            ((ZenithPermissionManager) Zenith.getPermissionsManager()).getAllGroups().forEach((label, group) -> {
                JsonUtils.toJsonFile("./plugins/Zenith/permissiongroups/" + label + ".json", group);
            });
        }
    }

    /**
     * Gets a list of all account requests.
     * @return A list of all account requests.
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    public static Map<String, com.yukiemeralis.blogspot.zenith.auth.old.SecurePlayerAccount> getAccountRequests() 
    { 
        //return Permissions.getAccountRequests();
        return null;
    }

    static LocalDate date;
    static LocalTime time;
    /**
     * Prints a message to the console, and saves it to the log.
     * @param message
     * @param printToConsole
     */
    public static void log(String message, boolean printToConsole)
    {
        date = LocalDate.now();
        time = LocalTime.now();
        String fullMessage = "[" + date.toString() + "-" + time.toString() + "] " + message;

        security_log.add(fullMessage);

        if (printToConsole)
            PrintUtils.sendMessage("§d" + fullMessage);
    }

    public static ZenithModule getModuleInstance()
    {
        return module;
    }

    static PrintWriter writer;
    /**
     * Exports the current log to a text file.
     */
    public static void export_log()
    {
        date = LocalDate.now();

        File logFile = new File("./plugins/Zenith/security_log-" + date.toString() + "-" + FileUtils.getFileNameCount("./plugins/Zenith/", "security_log-" + date.toString()) + ".txt");

        try {
            writer = new PrintWriter(logFile);

            for (String str : PrintUtils.getLog())
                writer.println(str);
        } catch (FileNotFoundException e) {
            return;
        }

        writer.flush();
        writer.close();
    }
}