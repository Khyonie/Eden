package com.yukiemeralis.blogspot.zenith.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.yukiemeralis.blogspot.zenith.auth.SecurePlayerAccount.AccountType;
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
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

@ModInfo (
    modName = "ZenithAuth",
    description = "Provides account security for users and commands.",
    modFamily = "Zenith base modules",
    version = "1.3.1",
    modIcon = Material.IRON_BARS,
    maintainer = "Yuki_emeralis"
)
@ZenConfig
@DefaultConfig (
    keys =   {"console_password",          "min_rank_for_2fa", "allow_console_logins", "max_failed_login_attempts", "account_lockout_time"},
    values = {"!ch4nge_m3-Plea5e_[{]}#@!", "1",                "true",                 "3",                         "300"}
)
@PreventUnload(CallerToken.ZENITH)
public class SecurityCore extends ZenithModule
{
    private static List<String> security_log = new ArrayList<>();
    static Map<Player, PlayerData> active_players = new HashMap<>();

    public SecurityCore()
    {
        addListener(
            new ChatUtils()
        );
    }

    @Override
    public void onEnable() 
    {
        FileUtils.ensureFolder("./plugins/Zenith/user-accounts");
        FileUtils.ensureFolder("./plugins/Zenith/account-requests");
        Permissions.populateAccountsList();

        if (Boolean.parseBoolean(this.config.get("allow_console_logins")))
        {
            PrintUtils.sendMessage("Console account is active!", InfoType.WARN);
            PrintUtils.sendMessage("This is discouraged, as the security with a proper SPA is greater than using a plaintext username and password.", InfoType.WARN);
            PrintUtils.sendMessage("Please create a superadmin account, and change \"allow_console_logins\" to \"false\" in the ZenithAuth config file.", InfoType.WARN);

            // If console user account doesn't exist, create it
            if (!Permissions.getAccounts().containsKey("console"))
            {
                SecurePlayerAccount console_account = new SecurePlayerAccount("console", this.config.get("console_password"), AccountType.SUPERADMIN);
                console_account.disable2FA();
                Permissions.registerAccount(console_account);
            } else {
                if (!Permissions.comparePassword(Permissions.getAccounts().get("console"), this.config.get("console_password")))
                {
                    SecurePlayerAccount console_account = new SecurePlayerAccount("console", this.config.get("console_password"), AccountType.SUPERADMIN);
                    console_account.disable2FA();

                    Permissions.getAccounts().remove("console");
                    Permissions.registerAccount(console_account);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() 
    {
        Permissions.getAccounts().forEach((name, account) -> {
            JsonUtils.toJsonFile("./plugins/Zenith/user-accounts/" + name + ".json", account);
        });
    }

    /**
     * Returns a player's data. </p>
     * If the account isn't loaded into memory, the account will be loaded from a local file, or generated fresh.
     * @param player The player to obtain an account to
     * @return The data tied to this player
     */
    public static PlayerData getPlayerData(Player player)
    {
        if (active_players.containsKey(player))
            return active_players.get(player);

        PlayerData account;

        File accountFile = new File("./plugins/Zenith/playerdata/" + player.getUniqueId() + ".json");
        if (!accountFile.exists())
        {
            account = new PlayerData(player);
            JsonUtils.toJsonFile(accountFile.getAbsolutePath(), account);
        }

        account = (PlayerData) JsonUtils.fromJsonFile(accountFile.getAbsolutePath(), PlayerData.class);
        
        if (account == null)
        {
            PrintUtils.log("Account file for " + player.getName() + " is corrupt! Moving to " + FileUtils.moveToLostAndFound(accountFile).getAbsolutePath(), InfoType.ERROR);
            accountFile.delete();

            return getPlayerData(player);
        }

        return account;
    }

    /**
     * Gets a list of all account requests.
     * @return A list of all account requests.
     */
    public static Map<String, SecurePlayerAccount> getAccountRequests()
    {
        return Permissions.getAccountRequests();
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
