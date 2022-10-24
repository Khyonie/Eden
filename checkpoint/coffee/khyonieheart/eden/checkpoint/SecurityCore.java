package coffee.khyonieheart.eden.checkpoint;

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

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.EdenModule.ModInfo;
import coffee.khyonieheart.eden.module.annotation.EdenConfig;
import coffee.khyonieheart.eden.module.annotation.ModuleFamily;
import coffee.khyonieheart.eden.module.annotation.PreventUnload;
import coffee.khyonieheart.eden.module.java.enums.CallerToken;
import coffee.khyonieheart.eden.rosetta.CompletionsManager;
import coffee.khyonieheart.eden.rosetta.CompletionsManager.ObjectMethodPair;
import coffee.khyonieheart.eden.utils.FileUtils;
import coffee.khyonieheart.eden.utils.JsonUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.option.Option;

/**
 * Checkpoint module class. Checkpoint handles various small security tasks, as well as providing two options for
 * permissions managers in EdenPermissionManager and BukkitPermissionManager.
 * @since 1.0
 * @author Yuki_emeralis
 */
@ModInfo (
    modName = "Checkpoint",
    description = "Provides permissions for users and commands.",
    version = "1.4.4",
    modIcon = Material.IRON_BARS,
    maintainer = "Yuki_emeralis",
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2", "v1_19_R1"}
)
@ModuleFamily(name = "Eden core modules", icon = Material.ENDER_EYE)
@PreventUnload(CallerToken.EDEN)
@EdenConfig
public class SecurityCore extends EdenModule
{
    private static List<String> security_log = new ArrayList<>();
    private static List<UuidBanEntry> uuid_bans = new ArrayList<>();
    private static EdenModule module;

    @SuppressWarnings("unused")
    private Map<String, Object> EDEN_DEFAULT_CONFIG = Map.of(
        "notifyElevate", true,
        "blockPasswordsInChat", true,
        "deopOnIpChange", true,
        "obscureDisallowedCommands", true  
    );

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

    private static LocalDate date;
    private static LocalTime time;
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

    /**
     * Issue a UUID ban to a user. Users that have been UUID banned cannot join, and is immune to IP and username changes.
     * @param player The player to issue a ban to.
     * @param kickMessage The message to send to the player when the ban is submitted.
     * @param banMessage The message to send when the player attempts to join.
     * @return If the ban was successful or not.
     */
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

    /**
     * Pardons a UUID banned player.
     * @param username The username of the player to pardon.
     * @return If the pardon was successful or not.
     */
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

    /**
     * Check if the given player is UUID banned. If so, a UUID ban entry is supplied.
     * @param player The player to check.
     * @return If the player is UUID banned.
     */
    public static Option isBanned(Player player)
    {
        for (UuidBanEntry ban : uuid_bans)
            if (ban.getUuid().equals(player.getUniqueId().toString()))
                return Option.some(ban);

        return Option.none();
    }

    /**
     * Obtains a list of all current UUID bans.
     * @return A list of all current UUID bans.
     */
    public List<String> allUuidBans()
    {
        List<String> data = new ArrayList<>();

        for (UuidBanEntry ban : uuid_bans)
            data.add(ban.getUsername());

        return data;
    }

    private static PrintWriter writer;
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