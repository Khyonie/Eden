package fish.yukiemeralis.eden.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.EdenModule.EdenConfig;
import fish.yukiemeralis.eden.module.EdenModule.ModInfo;
import fish.yukiemeralis.eden.module.annotation.ModuleFamily;
import fish.yukiemeralis.eden.module.java.annotations.DefaultConfig;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.FileUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;

import org.bukkit.Material;

@ModInfo (
    modName = "Checkpoint",
    description = "Provides permissions for users and commands.",
    version = "1.4.1",
    modIcon = Material.IRON_BARS,
    maintainer = "Yuki_emeralis",
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2"}
)
@ModuleFamily(name = "Eden core modules", icon = Material.ENDER_EYE)
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
        //Eden.setPermissionsManager(new EdenPermissionManager());
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
    public static Map<String, fish.yukiemeralis.eden.auth.old.SecurePlayerAccount> getAccountRequests() 
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