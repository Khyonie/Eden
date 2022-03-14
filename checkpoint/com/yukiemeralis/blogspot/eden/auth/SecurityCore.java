package com.yukiemeralis.blogspot.eden.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;
import com.yukiemeralis.blogspot.eden.module.EdenModule.EdenConfig;
import com.yukiemeralis.blogspot.eden.module.java.annotations.DefaultConfig;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.eden.utils.ChatUtils;
import com.yukiemeralis.blogspot.eden.utils.FileUtils;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;

@ModInfo (
    modName = "Checkpoint",
    description = "Provides permissions for users and commands.",
    modFamily = "Eden base modules",
    version = "1.4.1",
    modIcon = Material.IRON_BARS,
    maintainer = "Yuki_emeralis",
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2"}
)
@PreventUnload(CallerToken.EDEN)
@EdenConfig
@DefaultConfig(
    keys =   {"notifyElevate", "blockPasswordsInChat"},
    values = {"true",          "true"}
)
public class SecurityCore extends EdenModule
{
    private static List<String> security_log = new ArrayList<>();

    private static EdenModule module;

    public SecurityCore()
    {
        addListener(
            new ChatUtils()
        );
    }

    @Override
    public void onEnable() 
    {
        FileUtils.ensureFolder("./plugins/Eden/playerdata/");
        FileUtils.ensureFolder("./plugins/Eden/permissiongroups/");

        module = this;
        Eden.setPermissionsManager(new EdenPermissionManager());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() 
    {
        if (Eden.getPermissionsManager() instanceof EdenPermissionManager)
        {
            ((EdenPermissionManager) Eden.getPermissionsManager()).getAllGroups().forEach((label, group) -> {
                JsonUtils.toJsonFile("./plugins/Eden/permissiongroups/" + label + ".json", group);
            });
        }
    }

    /**
     * Gets a list of all account requests.
     * @return A list of all account requests.
     * @deprecated Secure player accounts are now deprecated.
     */
    @Deprecated
    public static Map<String, com.yukiemeralis.blogspot.eden.auth.old.SecurePlayerAccount> getAccountRequests() 
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

    public static EdenModule getModuleInstance()
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

        File logFile = new File("./plugins/Eden/security_log-" + date.toString() + "-" + FileUtils.getFileNameCount("./plugins/Eden/", "security_log-" + date.toString()) + ".txt");

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