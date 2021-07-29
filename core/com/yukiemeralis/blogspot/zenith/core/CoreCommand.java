package com.yukiemeralis.blogspot.zenith.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.auth.Permissions;
import com.yukiemeralis.blogspot.zenith.auth.SecurePlayerAccount;
import com.yukiemeralis.blogspot.zenith.auth.SecurityCore;
import com.yukiemeralis.blogspot.zenith.auth.SecurePlayerAccount.AccountType;
import com.yukiemeralis.blogspot.zenith.command.CommandManager;
import com.yukiemeralis.blogspot.zenith.command.ZenithCommand;
import com.yukiemeralis.blogspot.zenith.command.tabcomplete.*;
import com.yukiemeralis.blogspot.zenith.core.modgui.ModuleGui;
import com.yukiemeralis.blogspot.zenith.core.modgui.ModuleTracker;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ZenConfig;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.DefaultConfig;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.zenith.utils.FileUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.Result;
import com.yukiemeralis.blogspot.zenith.utils.Result.UndefinedResultException;

@PreventUnload(CallerToken.ZENITH)
public class CoreCommand extends ZenithCommand
{
    public CoreCommand(ZenithModule mod) 
    {
        super("zen", mod);

        this.tabCompleteTree = new TabCompleteTree().attachRoot(
            new TabCompleteRoot("zen")
                .addBranches(
                    new TabCompleteBranch("mods")
                        .addBranches(
                            new TabCompleteBranch("ENABLED_MODULES")
                                .addBranches(
                                    new TabCompleteBranch("readconfig"),
                                    new TabCompleteBranch("saveconfig"),
                                    new TabCompleteBranch("reloadconfig"),
                                    new TabCompleteBranch("config")
                                        .addBranches(
                                            new TabCompleteBranch("<key>")
                                                .addBranches(
                                                    new TabCompleteBranch("<value>")
                                                )
                                        )
                                )
                        ),
                    new TabCompleteBranch("logging")
                        .addBranches(
                            new TabCompleteBranch("export"),
                            new TabCompleteBranch("verbose")
                        ),
                    new TabCompleteBranch("auth")
                        .addBranches(
                            new TabCompleteBranch("login")
                                .addBranches(
                                    new TabCompleteBranch("<username>")
                                ),
                            new TabCompleteBranch("logout"),
                            new TabCompleteBranch("autologin"),
                            new TabCompleteBranch("create")
                                .addBranches(
                                    new TabCompleteBranch("user", "staff", "admin", "superadmin")
                                        .addBranches(
                                            new TabCompleteBranch("<username>")
                                        )
                                )
                        ),
                    new TabCompleteBranch("requests")
                        .addBranches(
                            new TabCompleteBranch("view"),
                            new TabCompleteBranch("rejectall"),
                            new TabCompleteBranch("approve", "reject")
                                .addBranches(
                                    new TabCompleteBranch("<username>")
                                )
                        ),
                    new TabCompleteBranch("mm")
                        .addBranches(
                            new TabCompleteBranch("enable", "disable", "unload")
                                .addBranches(
                                    new TabCompleteBranch("GATHERED_MODULES")
                                ),
                            new TabCompleteBranch("load")
                                .addBranches(
                                    new TabCompleteBranch("<module file>")
                                )
                        ),
                    new TabCompleteBranch("helpall")
                )
        );
    }

    private static LocalDate time = LocalDate.now();
    private PrintWriter text_writer;
    private String fileName;
    private File logFile;
    private ZenithModule module;
    
    @ZenCommandHandler(usage = "zen mods <Module name> <config | readconfig>", description = "View and configure Zenith modules.", argsCount = 1, minAuthorizedRank = 2)
    public void zcommand_mods(CommandSender sender, String commandLabel, String[] args)
    {
        if (args.length == 1)
        {
            if (sender instanceof ConsoleCommandSender)
            {
                PrintUtils.sendMessage(sender, "§6-----[ §eModules §6]-----");
                for (ZenithModule m : Zenith.getModuleManager().getEnabledModules())
                    PrintUtils.sendMessage(sender, "§a" + m.getName());

                for (ZenithModule m : Zenith.getModuleManager().getDisabledModules())
                    PrintUtils.sendMessage(sender, "§c" + m.getName());
                return;
            }

            ModuleTracker.update();
            ModuleGui gui = new ModuleGui();
            gui.display((Player) sender);
            
            return;
        }

        module = Zenith.getModuleManager().getEnabledModuleByName(args[1]);

        if (module == null)
        {
            PrintUtils.sendMessage(sender, "No enabled module named \"" + args[1] + "\" was found.");
            return;
        }
            
        if (args.length <= 2)
        {
            PrintUtils.sendMessage(sender, "Expected a subcommand, received none.");
            return;
        }

        switch (args[2])
        {
            case "config":
                if (!ensureArgsCount(args, 4, 2, "config", sender))
                    break;

                String oldValue = module.getConfig().get(args[3]);

                module.getConfig().put(args[3], args[4]);

                // Call an event to notify anything that might want to listen for things like this
                Zenith.getInstance().getServer().getPluginManager().callEvent(new ZenithConfigChangeEvent(module, args[3], oldValue, args[4]));
                PrintUtils.sendMessage(sender, "Entered configuration value \"§a" + args[4] + "§7\" into key \"§b" + args[3] + "§7\" in module " + module.getName() + ".");
                break;
            case "readconfig":
                module.getConfig().forEach((key, value) -> {
                    String color = "e";
                    if (value.equals("true")) {
                        color = "a";
                    } else if (value.equals("false")) {
                        color = "c";
                    }

                    PrintUtils.sendMessage(sender, "Config: §b" + key + "§7 | §" + color + value);
                });
                break;
            case "reloadconfig":
                if (module.getClass().isAnnotationPresent(ZenConfig.class))
                {
                    module.loadConfig();
                    PrintUtils.sendMessage(sender, "Reloaded config from file. You should double-check the new values by using /zen mods " + args[1] + " readconfig.");
                    break;
                }
                    
                PrintUtils.sendMessage(sender, "This module does not have an associated configuration file.");
                break;
            case "saveconfig":
                if (module.getClass().isAnnotationPresent(ZenConfig.class))
                {
                    module.saveConfig();
                    PrintUtils.sendMessage(sender, "Saved configuration from memory to local file.");
                    break;
                }
                    
                PrintUtils.sendMessage(sender, "This module does not have an associated configuration file.");
                break;
            case "clean":
                if (module.getClass().isAnnotationPresent(ZenConfig.class) && module.getClass().isAnnotationPresent(DefaultConfig.class))
                {
                    DefaultConfig dc = module.getClass().getAnnotation(DefaultConfig.class);
                    Map<String, String> config = module.getConfig();
                    
                    Iterator<String> iter = config.keySet().iterator();
                    int removed = 0;
                    while (iter.hasNext())
                    {
                        String key = iter.next();

                        if (!Arrays.asList(dc.keys()).contains(key))
                        {
                            iter.remove();
                            removed++;
                        }
                    }

                    PrintUtils.sendMessage(sender, "Cleaned " + removed + " key(s) from configuration file.");
                    break;
                }

                PrintUtils.sendMessage(sender, "This module does not have an associated configuration file.");
                break;
            default:
                this.sendErrorMessage(sender, args[2], "<Module>");
                break;
        }
        return;
    }

    @ZenCommandHandler(usage = "zen logging <verbose | export>", description = "Toggle verbose logging or export the current log to a file.", argsCount = 2, minAuthorizedRank = 2)
    @ZenCommandRedirect(labels = {"logs", "log"}, command = "zen logging args")
    public void zcommand_logging(CommandSender sender, String commandLabel, String[] args)
    {
        switch (args[1])
        {
            case "verbose":
                if (PrintUtils.isVerboseLoggingEnabled())
                {
                    PrintUtils.sendMessage(sender, "Disabled verbose logging.");
                    Zenith.getModuleManager().getEnabledModuleByName("Zenith").getConfig().put("verboseLogging", "false");
                    PrintUtils.disableVerboseLogging();
                    break;
                }
                
                PrintUtils.sendMessage(sender, "Enabled verbose logging.");
                PrintUtils.enableVerboseLogging();

                Zenith.getModuleManager().getEnabledModuleByName("Zenith").getConfig().put("verboseLogging", "true");
                break;
            case "export":
                time = LocalDate.now();
                fileName = "log-" + time.toString() + "-" + FileUtils.getFileNameCount("./plugins/Zenith/", "log-" + time.toString() + "-") + ".txt";
                logFile = new File("./plugins/Zenith/" + fileName);

                try {
                    text_writer = new PrintWriter(logFile);

                    for (String str : PrintUtils.getLog())
                        text_writer.println(str);
                } catch (FileNotFoundException e) {
                    break;
                }

                text_writer.flush();
                text_writer.close();
                
                PrintUtils.sendMessage(sender, "Done! Log exported to §a" + fileName + "§7.");
                break;
            default:
                sendErrorMessage(sender, args[1], "logging");
                break;
        }
    }
    
    @ZenCommandHandler(usage = "zen auth <login | logout | create | autologin>", description = "Login in, logout of, or create a SecurePlayerAccount.", argsCount = 2, minAuthorizedRank = 0)
    @ZenCommandRedirect(labels = {"login", "logout", "create"}, command = "zen auth label args")
    public void zcommand_auth(CommandSender sender, String commandLabel, String[] args)
    {
        switch (args[1])
        {
            case "login":
                if (!ensureArgsCount(args, 3, "login", sender))
                    break;

                if (sender instanceof ConsoleCommandSender)
                {
                    PrintUtils.sendMessage(
                        sender, 
                        "Console users cannot log into an account. Please try \"/sudo " + args[2] + " <command>\" to run a command as an account."
                    );
                    break;
                }

                if (!Permissions.accountExists(args[2]))
                {
                    PrintUtils.sendMessage(sender, "No account exists by the name \"" + args[2] + "\".");
                    break;
                }

                Permissions.expectPassword((Player) sender, args[2]);
                break;
            case "logout":
                if (Permissions.logout(sender))
                {
                    PrintUtils.sendMessage(sender, "Logged out from account.");
                    break;
                }

                PrintUtils.sendMessage(sender, "Cannot log out, as you are not logged into an account.");
                break;
            case "create":
                if (!ensureArgsCount(args, 4, "create", sender))
                {
                    PrintUtils.sendMessage(sender, "Usage: /zen auth create <user | staff | admin | superadmin> <username>");
                    PrintUtils.sendMessage(sender, "Note that upon creating an account that is of rank \"staff\" or higher, a request will be sent for a superadmin to approve.");
                    break;
                }

                AccountType type = null;
            
                try {
                    type = AccountType.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    PrintUtils.sendMessage(sender, "Usage: /zen auth create <user | staff | admin | superadmin> <username>");
                    PrintUtils.sendMessage(sender, "Note that upon creating an account that is of rank \"staff\" or higher, a request will be sent for a superadmin to approve.");
                    break;
                }

                String name = args[3];

                if (Permissions.accountExists(name))
                {
                    PrintUtils.sendMessage(sender, "An account named \"" + name + "\" already exists. Please contact an administrator if you think this is a mistake.");
                    break;
                }

                PrintUtils.sendMessage(sender, "Please enter a password for \"" + name + "\":");
                Permissions.createNewAccount(sender, type, name);
                break;
            case "autologin":
                if (sender instanceof ConsoleCommandSender)
                    break;

                if (SecurityCore.getPlayerData((Player) sender).isAutoLogin())
                {
                    SecurityCore.getPlayerData((Player) sender).disableAutoLogin();
                    PrintUtils.sendMessage(sender, "Disabled automatic login.");
                    break;
                }

                if (!Permissions.isLoggedIn(sender))
                {
                    PrintUtils.sendMessage(sender, "You must be logged into an account to enable auto-login.");
                    break;
                }

                SecurePlayerAccount account = Permissions.getLoggedInAccount(sender);
                PrintUtils.sendMessage(sender, "Enabled automatic login for account \"" + account.getUsername() + "\".");
                SecurityCore.getPlayerData((Player) sender).enableAutoLogin((Player) sender, account.getUsername(), account.getPassword());
                break;
            default:
                sendErrorMessage(sender, args[1], "auth");
                break;
        }
    }

    @ZenCommandHandler(usage = "zen requests <view | approve | reject | rejectall> <name>", description = "View, approve, and reject account requests.", argsCount = 2, minAuthorizedRank = 3)
    public void zcommand_requests(CommandSender sender, String commandLabel, String[] args)
    {
        SecurePlayerAccount spaccount;
        File f;
        switch (args[1])
        {
            case "view":
                SecurityCore.getAccountRequests().forEach((username, account) -> {
                    PrintUtils.sendMessage(sender, "Request from player §a" + username + "§7 | Username: §b" + account.getUsername() + "§7, type: §b" + account.getAccountType().name());
                });
                break;
            case "approve":
                if (!ensureArgsCount(args, 3, 1, "approve", sender))
                    break;

                f = new File("./plugins/Zenith/account-requests/" + args[2] + ".json");
                if (!f.exists())
                {
                    PrintUtils.sendMessage(sender, "No request has been made by this player!");
                    break;
                }

                spaccount = Permissions.getAccountRequest(args[2]);

                if (!Permissions.registerAccount(spaccount))
                {
                    PrintUtils.sendMessage(sender, "Failed to approve account. One may already exist under the name \"" + spaccount.getUsername() + "\".");
                    SecurityCore.getAccountRequests().remove(args[2]);
                    break;
                }

                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                PrintUtils.sendMessage(sender, "Approved account request.");
                break;
            case "reject":
                if (!ensureArgsCount(args, 3, 1, "approve", sender))
                    break;

                f = new File("./plugins/Zenith/account-requests/" + args[2] + ".json");
                if (!f.exists())
                {
                    PrintUtils.sendMessage(sender, "No request has been made by this player!");
                    break;
                }

                SecurityCore.getAccountRequests().remove(args[2]);
                PrintUtils.sendMessage(sender, "Rejected account request.");

                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case "rejectall":
                for (File file : new File("./plugins/Zenith/account-requests/").listFiles())
                {
                    try {
                        Files.deleteIfExists(file.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                PrintUtils.sendMessage(sender, "Rejected all account requests.");
                break;
            default:
                sendErrorMessage(sender, args[1], "requests");
                break;
        }
    }

    @ZenCommandHandler(usage = "zen helpall", description = "Provides a list of all parent commands.", argsCount = 1, minAuthorizedRank = 0)
    public void zcommand_helpall(CommandSender sender, String commandLabel, String[] args)
    {
        PrintUtils.sendMessage(sender, "§6----[ §eRegistered Zenith Commands §6]----");

        for (ZenithCommand parent : CommandManager.getKnownCommands())
        {
            try {
                PrintUtils.sendMessage(sender, "§7/§a" + parent.getName() + " §7| from module §b" + parent.getParentModule().getName());
            } catch (NullPointerException e) {
                PrintUtils.sendMessage(sender, "§7/§a" + parent.getName());
            }
        }

        PrintUtils.sendMessage(sender, "- Every Zenith command has a \"help\" subcommand, to display a list of subcommands.");
    }

    @ZenCommandHandler(usage = "zen mm <load | unload | enable | disable | download> <module name | URL>", description = "Command interface for the Zenith module manager.", argsCount = 3, minAuthorizedRank = 3)
    public void zcommand_mm(CommandSender sender, String commandLabel, String[] args)
    {
        ZenithModule module;
        switch (args[1])
        {
            case "load":
            	Result<ZenithModule, String> result = Zenith.getModuleManager().loadSingleModule("./plugins/Zenith/mods/" + args[2] + ".jar");
            	
            	switch (result.getState())
            	{
            		case OK:
						try {
							module = (ZenithModule) result.unwrap();
						} catch (UndefinedResultException e1) { return; }
            			break;
            		case ERR:
            			try {
            				PrintUtils.sendMessage(sender, "Failed to load module! Error: \"" + result.unwrap() + "\"");
            				return;
            			} catch (UndefinedResultException e) { return; }
            		default: return;
            	}

                if (module == null)
                {
                    PrintUtils.sendMessage(sender, "File doesn't exist.");
                    return;
                }

                PrintUtils.sendMessage(sender, "Done! You may now enable this module by running \"/zen mm enable " + module.getName() + "\".");

                break;
            case "unload":
                module = Zenith.getModuleManager().getDisabledModuleByName(args[2]);

                if (module == null)
                {
                    PrintUtils.sendMessage(sender, "Failed to find a disabled module by that name. Ensure that the module you wish to unload is disabled.");
                    return;
                }

                Zenith.getModuleManager().removeModuleFromMemory(module.getName(), CallerToken.fromCommandSender(sender));

                if (Zenith.getModuleManager().getDisabledModules().contains(module))
                {
                    PrintUtils.sendMessage(sender, "Failed to unload module from memory!");
                    return;
                }

                PrintUtils.sendMessage(sender, "Successfully unloaded module from memory. To reload it, execute \"/zen mm load\" on the module file.");
                break;
            case "enable":
                module = Zenith.getModuleManager().getDisabledModuleByName(args[2]);

                if (module == null)
                {
                    PrintUtils.sendMessage(sender, "Failed to find a gathered module by that name. Ensure that the module you wish to enable has been loaded into memory.");
                    return;
                }

                PrintUtils.sendMessage(sender, "Enabling module \"" + module.getName() + "\"...");

                try {
                    Zenith.getModuleManager().enableModule(module);
                    module.setEnabled();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Zenith.getModuleManager().getEnabledModuleByName(module.getName()) != null)
                {
                    PrintUtils.sendMessage(sender, "Done! Module has been enabled. If this is a cold start for this module, then command suggestions may not work.");
                    return;
                }
                
                PrintUtils.sendMessage(sender, "Failed to enable module. Please check the console or export the log using /zen logging export for details.");
                break;
            case "disable":
                module = Zenith.getModuleManager().getEnabledModuleByName(args[2]);

                if (module == null)
                {
                    PrintUtils.sendMessage(sender, "Failed to find a gathered module by that name. Ensure that the module you wish to enable has been loaded into memory.");
                    return;
                }

                Zenith.getModuleManager().disableModule(module.getName(), CallerToken.fromCommandSender(sender));

                if (Zenith.getModuleManager().getDisabledModuleByName(module.getName()) != null)
                {
                    module.setDisabled();
                    PrintUtils.sendMessage(sender, "Disabled module.");
                    break;
                }
                
                PrintUtils.sendMessage(sender, "Failed to disable module. Perhaps it cannot be disabled?");

                break;
            case "download":
                if (!Zenith.getModuleManager().isModulePresent("ZenithNetworking"))
                {
                    PrintUtils.sendMessage(sender, "This functionality requires the ZenithNetworking module.");
                    return;
                }

                try {
                    // We have to ask the module manager to grab this class from the cache, since it doesn't exist in this context
                    Class<?> nu_class = Zenith.getModuleManager().getCachedClass("com.yukiemeralis.blogspot.zenithnetworking.NetworkingUtils");

                    String finalPortion = (String) nu_class.getMethod("getFinalURLPortion", String.class).invoke(null, args[2]);
                    nu_class.getMethod("downloadFileFromURLThreaded", String.class, String.class).invoke(null, args[2], "./plugins/Zenith/dlcache/" + finalPortion);

                    PrintUtils.sendMessage(sender, "Done! File has been saved as " + finalPortion + ".");
                } catch (InvocationTargetException e) {
                    PrintUtils.sendMessage(sender, "Failed to download file. Ensure that the given URL is correct.");
                    e.printStackTrace();
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            default:
                sendErrorMessage(sender, args[1], "mm");
                break;
        }
    }

    @ZenCommandHandler(usage = "zen data <user> <subcommand>", description = "View and repair player data.", argsCount = 2, minAuthorizedRank = 3)
    public void zcommand_data(CommandSender sender, String commandLabel, String[] args)
    {
        Player p = Zenith.getInstance().getServer().getPlayerExact(args[1]);

        if (p == null)
        {
            PrintUtils.sendMessage(sender, "No player named \"" + args[1] + "\" is online.");
            return;
        }

        if (args.length == 2)
        {   
            PrintUtils.sendMessage(sender, "Player information about user \"§b" + args[1] + "§7\":");
            PrintUtils.sendMessage(sender, "UUID: §a" + p.getUniqueId().toString());
            PrintUtils.sendMessage(sender, "Current IP: §a" + p.getAddress().getAddress().getHostAddress());
            PrintUtils.sendMessage(sender, "Has data? §a" + (SecurityCore.getPlayerData(p) != null));
            PrintUtils.sendMessage(sender, "Is logged into an SPA? §a" + (Permissions.isLoggedIn(p)));
            return;
        }

        switch (args[2])
        {
            case "repair":
                break;
            case "replace":
                break;
        }
    }

    @ZenCommandHandler(usage = "zen", description = "Provides the user with Zenith's version, and a module count.", argsCount = 0, minAuthorizedRank = 0)
    public void zcommand_nosubcmd(CommandSender sender, String commandLabel, String[] args)
    {
        String buffer = "┌§m";

        if (!(sender instanceof Entity))
        {
            PrintUtils.sendMessage(sender, "Version: Zenith " + VersionCtrl.getVersion());
            PrintUtils.sendMessage(sender, 
                "Enabled modules: §a" + Zenith.getModuleManager().getEnabledModules().size() + 
                "§7 (§c" + Zenith.getModuleManager().getDisabledModules().size() + "§7)/" + 
                Zenith.getModuleManager().getAllModules().size()
            );
            return;
        }

        // Line 1
        for (int i = 0; i < 11; i++)
            buffer = buffer + "─";
        buffer = buffer + "§r§7┐";
        PrintUtils.sendMessage(sender, buffer);

        // Line 2
        PrintUtils.sendMessage(sender, "│ " + "Zenith " + VersionCtrl.getVersion() + "│");
        
        buffer = buffer.replace("┌", "└").replace("┐", "┘");
        PrintUtils.sendMessage(sender, buffer);

        PrintUtils.sendMessage(sender, "§oWith love from Yuki_emeralis §r§c❤");
        PrintUtils.sendMessage(sender, "Project is maintained here: §9https://github.com/YukiEmeralis/Zenith");

        PrintUtils.sendMessage(sender, 
            "Enabled modules: §a" + Zenith.getModuleManager().getEnabledModules().size() + 
            "§7 (§c" + Zenith.getModuleManager().getDisabledModules().size() + "§7)/" + 
            Zenith.getModuleManager().getAllModules().size()
        );
    }
}