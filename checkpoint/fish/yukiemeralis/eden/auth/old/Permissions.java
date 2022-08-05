package fish.yukiemeralis.eden.auth.old;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.auth.SecurityCore;
import fish.yukiemeralis.eden.auth.old.SecurePlayerAccount.AccountType;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.ChatUtils.ChatAction;

/**
 * @deprecated Permissions are now handled by permission groups.
 */
@Deprecated
public class Permissions 
{
    private static Map<String, SecurePlayerAccount> accounts = new HashMap<>();
    private static List<String> locked_accounts = new ArrayList<>();
    private static Map<CommandSender, SecurePlayerAccount> logged_in_users = new HashMap<>();

    // Login timer
    private static Map<String, BukkitTask> login_attempts_timers = new HashMap<>();
    private static Map<String, Integer> login_attempts = new HashMap<>();


    /**
     * Loads all locally saved accounts into memory
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    static synchronized void populateAccountsList()
    {
        File accounts_file = new File("./plugins/Eden/user-accounts");
        SecurePlayerAccount account = null;

        for (File f : accounts_file.listFiles())
        {
            account = JsonUtils.fromJsonFile(f.getAbsolutePath(), SecurePlayerAccount.class);
            accounts.put(account.getUsername(), account);
        }
    }

    /**
     * Registers an account into the accounts list. If it doesn't already exist as a file, one will be created.
     * @param account The account to register
     * @return If the account registration was successful
     * @deprecated
     */
    @Deprecated
    public static synchronized boolean registerAccount(SecurePlayerAccount account)
    {
        if (accounts.containsKey(account.getUsername()))
        {
            SecurityCore.log("Failed to create new secure player account named \"" + account.getUsername() + "\", as one with that name already exists.", true);
            return false;
        }

        JsonUtils.toJsonFile("./plugins/Eden/user-accounts/" + account.getUsername() + ".json", account);
        accounts.put(account.getUsername(), account);
        return true;
    }

    /**
     * Gets all loaded accounts
     * @deprecated Permissions are now handled by permission groups.
     * @return
     */
    @Deprecated
    static Map<String, SecurePlayerAccount> getAccounts()
    {
        return accounts;
    }

    /**
     * Intended to be the used in conjunction with a command, the user will be prompted for a password for the account they wish to create.
     * @param sender The user intending to create this account
     * @param type The rank of this account
     * @param username The username of this account
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    public static void createNewAccount(CommandSender sender, AccountType type, String username)
    {
        ChatAction password_action = new ChatAction()
        {
            @Override
            public void run()
            {
                String message = ChatUtils.receiveResult(sender);
                ChatUtils.deleteResult(sender);

                SecurePlayerAccount account = new SecurePlayerAccount(username, message, type);

                if (account.getAccountType().getRank() > 0)
                {
                    storeAccountRequest(((Player) sender).getName(), account);

                    PrintUtils.sendMessage(sender, "A request for this account has been sent for administrator approval.");

                    for (CommandSender s : logged_in_users.keySet())
                    {
                        if (Permissions.isAuthorized(s, 3))
                        {
                            PrintUtils.sendMessage(sender, "Player " + ((Player) sender).getName() + " has made an account request. Run \"/eden requests view\" for more information.");
                        }
                    }
                    
                    return;
                }
                
                if (registerAccount(account))
                {
                    PrintUtils.sendMessage(sender, "Success! The account \"" + username + "\" has been created and registered.");
                    return;
                }

                PrintUtils.sendMessage(sender, "Something went wrong, please try again. If this issue persists, please contact an administrator.");
            }
        };

        ChatUtils.expectChat(sender, password_action);
    }

    @Deprecated
    private static void storeAccountRequest(String key, SecurePlayerAccount account)
    {
        JsonUtils.toJsonFile("./plugins/Eden/account-requests/" + key + ".json", account);
    }

    @Deprecated
    public static Map<String, SecurePlayerAccount> getAccountRequests()
    {
        Map<String, SecurePlayerAccount> requests = new HashMap<>();

        for (File f : new File("./plugins/Eden/account-requests/").listFiles())
        {
            requests.put(f.getName().replaceAll(".json", ""), JsonUtils.fromJsonFile(f.getAbsolutePath(), SecurePlayerAccount.class));
        }

        return requests;
    }

    @Deprecated
    public static SecurePlayerAccount getAccountRequest(String key)
    {
        SecurePlayerAccount account = JsonUtils.fromJsonFile("./plugins/Eden/account-requests/" + key + ".json", SecurePlayerAccount.class);

        return account == null ? null : account;
    }

    /**
     * Checks to see if a specific commandsender is authorized to access a resource
     * @param sender The commandsender
     * @param minimum The minimum allowed account rank to access a given resource
     * @return True if the commandsender is authorized, false if not
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    public static boolean isAuthorized(CommandSender sender, AccountType minimum)
    {
        return isAuthorized(sender, minimum.getRank());
    }

    /**
     * Checks to see if a specific commandsender is authorized to access a resource
     * @param sender The commandsender
     * @param minimum The minimum allowed account rank to access a given resource
     * @return True if the commandsender is authorized, false if not
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    public static boolean isAuthorized(CommandSender sender, int minimum)
    {
        if (sender instanceof ConsoleCommandSender) // Console users always have access. Maybe change this?
            return true;

        if (sender instanceof Player)
            if (((Player) sender).isOp())
                return true;

        int permission_level = 0;

        try {
            permission_level = logged_in_users.get(sender).getAccountType().getRank();
        } catch (NullPointerException e) {}

        return permission_level >= minimum;
    }

    /**
     * Primes ChatUtils to expect a user's password in chat.
     * @param target The player to expect from
     * @param username The account to expect the password from
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    public static void expectPassword(Player target, String username)
    {
        if (Eden.getModuleManager().getEnabledModuleByName("Checkpoint").getConfig().getBoolean("allow_console_logins") && username.equals("console") ||
            accounts.get(username).isDisabled())
        {
            PrintUtils.sendMessage(target, "This account has been disabled. Please contact an administrator if you believe this is a mistake.");
            return;
        }

        PrintUtils.sendMessage(target, "It is now safe to enter your password. Enter \"cancel\" to leave password entry mode.");
        PrintUtils.sendMessage(target, "Password for account \"" + username + "\":");

        ChatAction action = new ChatAction()
        {
            @Override
            public void run() 
            {
                String message = ChatUtils.receiveResult(target);
                ChatUtils.deleteResult(target);

                if (message.toLowerCase().equals("cancel"))
                {   
                    PrintUtils.sendMessage(target, "Exitted text entry mode. It is no longer safe to enter your password.");
                    return;
                }
                   
                login(target, username, message);
            }
        };

        ChatUtils.expectChat(target, action);
    }

    /**
     * Generates a hash for a given string. Useful for passwords and other data that we don't want to be stored in plaintext
     * @param password The input string
     * @return A string hash for the given input
     */
    public static String genHash(String password) throws NoSuchAlgorithmException
    {        
        MessageDigest msgDigest = MessageDigest.getInstance("MD5");
        msgDigest.update(password.getBytes());

        return new String(msgDigest.digest());
    }

    /**
     * Compares an input string to an account's password
     * @param account The account to check against
     * @param input The input to hash and check
     * @return True if the input, when hashed, matches the account's password hash, false if not
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    public static boolean comparePassword(SecurePlayerAccount account, String input)
    {
        return genHash(input).equals(account.getPassword()) ? true : false;
    }

    /**
     * Logs a player into a secure player account.
     * @param user The player to log in
     * @param username The username of the account to log in to\
     * @deprecated
     */
    @Deprecated
    static void login(CommandSender user, String username)
    {
        SecurePlayerAccount account = accounts.get(username);
        logged_in_users.put(user, account);
    }

    /**
     * Attempts to log a user into a secure account
     * @param user The commandsender to log in
     * @param username The username of the account to log into
     * @param password_plaintext The supposed password, in plaintext, for the given account
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    public static void login(CommandSender user, String username, String password_plaintext)
    {
        // We do our checking earlier, so we don't need to check if the accounts list contains the username
        SecurePlayerAccount account = accounts.get(username);

        if (locked_accounts.contains(username))
        {
            PrintUtils.sendMessage(user, "§cThis account has been temporarily locked. Please wait, or contact an administrator.");
            PrintUtils.sendMessage(user, "It is no longer safe to enter your password.");
            return;
        }
        
        if (!comparePassword(account, password_plaintext))
        {
            onFailedLogin(user, username);
            PrintUtils.sendMessage(user, "§cIncorrect password. Tries remaining: " + (max_failed_login_attempts - login_attempts.get(username)) + "/" + max_failed_login_attempts);

            if (!locked_accounts.contains(username))
            {
                expectPassword((Player) user, username);
            } else {
                PrintUtils.sendMessage(user, "It is no longer safe to enter your password.");
            }
                
            return;
        }

        logged_in_users.put(user, account);
        PrintUtils.sendMessage(user, "Logged in as \"" + username + "\"! It is no longer safe to enter your password.");
    }

    /**
     * Logs a user out of a secure account
     * @param user The user to logout
     * @return True if the user was successfully logged out, false if not
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    public static synchronized boolean logout(CommandSender user)
    {
        if (!logged_in_users.containsKey(user))
            return false;

        SecurityCore.log("User \"" + user.getName() + "\" logged out from account \"" + logged_in_users.get(user).getUsername() + "\".", false);
        logged_in_users.remove(user);
        return true;
    }

    private static int max_failed_login_attempts = -1;

    /**
     * Called when {@link #login(CommandSender, String, String)} fails due to an incorrect password
     * @param user The commandsender attempting to log in
     * @param username The username of the account
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    private static void onFailedLogin(CommandSender user, String username)
    {
        if (max_failed_login_attempts == -1)
            max_failed_login_attempts = Eden.getModuleManager().getEnabledModuleByName("Checkpoint").getConfig().getInt("max_failed_login_attempts");

        if (max_failed_login_attempts != Eden.getModuleManager().getEnabledModuleByName("Checkpoint").getConfig().getInt("max_failed_login_attempts"))
            max_failed_login_attempts = Eden.getModuleManager().getEnabledModuleByName("Checkpoint").getConfig().getInt("max_failed_login_attempts");

        if (locked_accounts.contains(username))
            return;

        if (!login_attempts.containsKey(username))
        {
            login_attempts.put(username, 0);

            int lockoutTime = 300;
            try {
                lockoutTime = Eden.getModuleManager().getEnabledModuleByName("Checkpoint").getConfig().getInt("account_lockout_time");
            } catch (IllegalArgumentException e) {}

            BukkitTask timer_thread = new BukkitRunnable()
            {
                @Override
                public void run() 
                {
                    locked_accounts.remove(username);
                    login_attempts_timers.remove(username);
                    login_attempts.remove(username);

                    SecurityCore.log("Login timer for " + username + " has expired.", true);
                }
            }.runTaskLater(Eden.getInstance(), lockoutTime*20);

            login_attempts_timers.put(username, timer_thread);
        }

        login_attempts.put(username, login_attempts.get(username) + 1);
        SecurityCore.log("User \"" + user.getName() + "\" failed to log into account \"" + username + "\". Attempt " + login_attempts.get(username) + "/" + max_failed_login_attempts, true);

        if (login_attempts.get(username) >= max_failed_login_attempts)
        {
            PrintUtils.sendMessage("Account has been locked.");
            // Lock account
            locked_accounts.add(username);
            SecurityCore.log("Too many failed login attempts. Locking account \"" + username + "\".", true);
            PrintUtils.sendMessage(user, "§cToo many failed login attempts, this account has been locked. Please try again later.");
            return;
        }
    }

    // We can let the account system hint that an account exists, without it directly giving us every account
    /**
     * Returns if an account exists or not. Does NOT return the account itself
     * @param username The possible username of an account
     * @return If an account exists and has been loaded into memory
     * @deprecated Permissions are now handled by permission groups.
     */
    @Deprecated
    public static boolean accountExists(String username)
    {
        return accounts.containsKey(username);
    }

    public static boolean isLoggedIn(CommandSender target)
    {
        return logged_in_users.containsKey(target);
    }

    @Deprecated
    public static SecurePlayerAccount getLoggedInAccount(CommandSender target)
    {
        return logged_in_users.get(target);
    }
}