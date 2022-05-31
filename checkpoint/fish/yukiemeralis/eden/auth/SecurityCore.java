package fish.yukiemeralis.eden.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.core.CompletionsManager;
import fish.yukiemeralis.eden.core.CompletionsManager.ObjectMethodPair;
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
import fish.yukiemeralis.eden.utils.Option;
import fish.yukiemeralis.eden.utils.PrintUtils;

@ModInfo (
    modName = "Checkpoint",
    description = "Provides permissions for users and commands.",
    version = "1.4.4",
    modIcon = Material.IRON_BARS,
    maintainer = "Yuki_emeralis",
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2"}
)
@ModuleFamily(name = "Eden core modules", icon = Material.ENDER_EYE)
@PreventUnload(CallerToken.EDEN)
@EdenConfig
@DefaultConfig(
    keys =   {"notifyElevate", "blockPasswordsInChat", "deopOnIpChange", "obscureDisallowedCommands"},
    values = {"true",          "true",                 "true",           "true"}
)
public class SecurityCore extends EdenModule
{
    private static List<String> security_log = new ArrayList<>();
    private static List<UuidBanEntry> uuid_bans = new ArrayList<>();
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

        File banFile = new File("./plugins/Eden/banned-uuids.json");
        if (!banFile.exists())
        {
            JsonUtils.toJsonFile(banFile.getAbsolutePath(), new UuidBanList(new ArrayList<UuidBanEntry>()));
        }

        uuid_bans = JsonUtils.fromJsonFile(banFile.getAbsolutePath(), UuidBanList.class).getData();

        try {
            CompletionsManager.registerCompletion("UUID_BANNED_PLAYERS", new ObjectMethodPair(this, this.getClass().getMethod("allUuidBans")), true);
        } catch (NoSuchMethodException | SecurityException e) {
            PrintUtils.printPrettyStacktrace(e);
        }

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

        JsonUtils.toJsonFile("./plugins/Eden/banned-uuids.json", new UuidBanList(uuid_bans));
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

    public static boolean banUuid(Player player, String kickMessage, String banMessage)
    {
        for (UuidBanEntry ban : uuid_bans)
            if (ban.getUuid().equals(player.getUniqueId().toString()))
                return false;

        SecurityCore.log("Player " + player.getName() + " has been UUID-banned. Reason: " + banMessage, true);
        uuid_bans.add(new UuidBanEntry(player, banMessage));
        
        if (player.isOnline())
            player.kickPlayer(kickMessage);

        return true;
    }

    public static boolean pardonUuid(String username)
    {
        for (UuidBanEntry ban : uuid_bans)
            if (ban.getUuid().equals(Eden.getUuidCache().get(username)))
            {
                uuid_bans.remove(ban);
                return true;
            }

        return false;
    }

    public static Option<UuidBanEntry> isBanned(Player player)
    {
        Option<UuidBanEntry> option = new Option<>(UuidBanEntry.class);
        for (UuidBanEntry ban : uuid_bans)
            if (ban.getUuid().equals(player.getUniqueId().toString()))
                return option.some(ban);

        return option.none();
    }

    public List<String> allUuidBans()
    {
        List<String> data = new ArrayList<>();

        for (UuidBanEntry ban : uuid_bans)
            data.add(ban.getUsername());

        return data;
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