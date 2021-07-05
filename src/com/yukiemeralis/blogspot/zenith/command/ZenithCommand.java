package com.yukiemeralis.blogspot.zenith.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.command.tabcomplete.TabCompleteBranch;
import com.yukiemeralis.blogspot.zenith.command.tabcomplete.TabCompleteTree;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

public abstract class ZenithCommand extends Command implements TabCompleter
{
    private Map<String, String> redirects = new HashMap<>();

    private ZenithModule parent_module = null;
    protected TabCompleteTree tabCompleteTree = null;

    private static Class<?> permissions;
    private static Method isAuthorized = null;

    public void init()
    {
        if (permissions == null)
        {
            permissions = Zenith.getModuleManager().getCachedClass("com.yukiemeralis.blogspot.zenith.auth.Permissions");

            try {
                isAuthorized = permissions.getMethod("isAuthorized", CommandSender.class, int.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public ZenithCommand(String name) 
    {
        super(name, "ZenithCommand: " + name, "ZenithCommand: " + name, new ArrayList<>());
        generateRedirects();
    }
    
    public ZenithCommand(String name, List<String> aliases) 
    {
        super(name, "ZenithCommand: " + name, "ZenithCommand: " + name, aliases);
        generateRedirects();
    }

    public ZenithCommand(String name, ZenithModule parent_module) 
    {
        super(name, "ZenithCommand: " + name, "ZenithCommand: " + name, new ArrayList<>());
        generateRedirects();
        this.parent_module = parent_module;
    }
    
    public ZenithCommand(String name, List<String> aliases, ZenithModule parent_module) 
    {
        super(name, "ZenithCommand: " + name, "ZenithCommand: " + name, aliases);
        generateRedirects();
        this.parent_module = parent_module;
    }

    public TabCompleteTree getTabCompleteTree()
    {
        return this.tabCompleteTree;
    }

    private void generateRedirects()
    {
        for (Method m : this.getClass().getMethods())
        {
            if (m.isAnnotationPresent(ZenCommandRedirect.class))
            {
                ZenCommandRedirect a = m.getAnnotation(ZenCommandRedirect.class);
                
                for (String label : a.labels())
                {
                    if (redirects.containsKey(label))
                    {
                        PrintUtils.log("Command " + this.getName() + "(" + this.getClass().getName() + ") contains multiple redirects for redirect label \"" + label + "\"!", InfoType.WARN);
                        continue;
                    }

                    redirects.put(label, a.command());
                }
            }
        }
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args)
    {
        Method cmd_method = null;

        // Attempt to locate a subcommand based on a label
        if (args.length != 0) 
        {
            if (this.redirects.containsKey(args[0].toLowerCase()))
            {
                redirectCommand(args[0], sender, commandLabel, args);
                return true;
            }

            try {
                cmd_method = this.getClass().getMethod("zcommand_" + args[0].toLowerCase(), CommandSender.class, String.class, String[].class);

                invokeCommand(
                    cmd_method, // The command method name
                    false,      // Whether or not this method is a subcommand
                    sender, commandLabel, args // And finally, our command params
                );
            } catch (NoSuchMethodException e) {
                PrintUtils.sendMessage(sender, "§cUnknown subcommand \"" + args[0] + "\" of parent \"/" + commandLabel + "\".");
                
            }
        } else { // If no subcommand is specified, attempt to find a blank command
            try {
                cmd_method = this.getClass().getMethod("zcommand_nosubcmd", CommandSender.class, String.class, String[].class);
                invokeCommand(
                    cmd_method, 
                    true,
                    sender, commandLabel, args
                );
            } catch (NoSuchMethodException e2) {
                // No subcommand is specified and blank command isn't set
                PrintUtils.sendMessage(sender, "§cCommand \"/" + commandLabel + "\" requires a subcommand! Please see \"/" + commandLabel + " help\" for a list of sucommands.");
            }
        }

        return true;
    }

    private void invokeCommand(Method cmd_method, boolean isBlankCommand, CommandSender sender, String commandLabel, String[] args)
    {
        // Then perform some checking to make sure the command has the info it needs
        ZenCommandHandler cmd_info = null;

        if (!cmd_method.isAnnotationPresent(ZenCommandHandler.class))
        {
            PrintUtils.sendMessage(sender, "§cThis command does not contain a handler! Cannot execute, please contact an administrator.");

            if (!isBlankCommand)
            {
                PrintUtils.log("Command \"/" + args[0] + "\" inside " + this.getClass().getName() + " does not specify a handler! Please contact this module's maintainer.", InfoType.ERROR);
                return;
            }
            
            PrintUtils.log("Command \"/" + commandLabel + "\" inside " + this.getClass().getName() + " does not specify a handler! Please contact this module's maintainer.", InfoType.ERROR);
            return;
        }

        cmd_info = cmd_method.getAnnotation(ZenCommandHandler.class);
        
        // Check args count
        if (args.length < cmd_info.argsCount() && !isBlankCommand)
        {
            PrintUtils.sendMessage(sender, "§cSubcommand \"" + args[0] + "\" requires " + (cmd_info.argsCount() - 1) + " arguments, but " + (args.length - 1) + " " + PrintUtils.indicative(args.length - 1) + " provided!");
            return;
        }

        // Check permissions
        
        try {
            if (!((boolean) isAuthorized.invoke(permissions, sender, cmd_info.minAuthorizedRank())))
            {
                PrintUtils.sendMessage(sender, "§cYou do not have permission to use this command.");
                return;
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            cmd_method.invoke(this, sender, commandLabel, args);
        } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
            PrintUtils.sendMessage(sender, "An internal error occurred. Please contact an administrator.");
            PrintUtils.log("Failed to execute a command! See below for details and a stacktrace.", InfoType.ERROR);
            e.printStackTrace();
        }
    }

    @ZenCommandHandler(usage = "<parent command> help", description = "Provides a list of subcommands tied to this parent command.", argsCount = 1, minAuthorizedRank = 0)
    public void zcommand_help(CommandSender sender, String commandLabel, String[] args)
    {
        PrintUtils.sendMessage(sender, "§6----[ §eSubcommands for " + commandLabel + " §6]----");
        for (Method m : this.getClass().getDeclaredMethods())
        {
            if (!m.getName().startsWith("zcommand_")) // Make sure the method is tied to a command
                continue;
            if (!m.isAnnotationPresent(ZenCommandHandler.class)) // Ensure that the command has a handler attached
            {
                PrintUtils.sendMessage(sender, "§c/§6" + commandLabel + "§c " + m.getName().replaceAll("zcommand_|nosubcmd", "") + " - This command does not specify a handler!");
                continue;
            }

            // Make sure the player has permission to use the command
            try {
                if (!((boolean) isAuthorized.invoke(permissions, sender, m.getAnnotation(ZenCommandHandler.class).minAuthorizedRank())))
                    continue;
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            PrintUtils.sendMessage(sender, "§7/§a" + commandLabel + "§e " + m.getName().replaceAll("zcommand_|nosubcmd", "") + " §7- " + m.getAnnotation(ZenCommandHandler.class).description());
        }
    }

    @ZenCommandHandler(usage = "/parent tree <args>", description = "View suggestions from this command's tabcomplete tree.", argsCount = 2, minAuthorizedRank = 3)
    public void zcommand_tree(CommandSender sender, String commandLabel, String[] args)
    {
        String[] treeArgs = new String[args.length - 1];

        // Copy args to a format we can use
        for (int i = 1; i < args.length; i++)
            treeArgs[i - 1] = args[i];

        for (int i = 0; i < treeArgs.length; i++)
            PrintUtils.sendMessage(sender, "Argument " + i + ": " + treeArgs[i]);

        List<TabCompleteBranch> branches = tabCompleteTree.traverse(treeArgs);

        for (TabCompleteBranch b : branches)
            b.getOptions().forEach(label -> {
                PrintUtils.sendMessage(sender, "Branch: " + label + " (" + b.getBranches().size() + " branch(es) from here)");
            });
    }

    public ZenithModule getParentModule()
    {
        return this.parent_module;
    }

    public void redirectCommand(String label, CommandSender sender, String commandLabel, String[] args)
    {
        String redirect = redirects.get(label);
        if (redirect == null)
            return;

        String newCommand = redirect.replace("label", label);

        // Debug
        String oldCommand = commandLabel;
        for (String str : args)
            oldCommand = oldCommand + " " + str;

        if (newCommand.endsWith("args") && args.length > 1)
        {
            newCommand = newCommand.replace(" args", "");
            for (int i = 1; i < args.length; i++)
            {
                newCommand = newCommand + " " + args[i];
            };
        } else {
            newCommand = newCommand.replace(" args", "");
        }

        PrintUtils.logVerbose("Redirect command | " + oldCommand + " > " + newCommand, InfoType.INFO);
        
        Zenith.getInstance().getServer().dispatchCommand(sender, newCommand);
    }

    protected boolean isRedirect(String label)
    {
        return redirects.containsKey(label);
    }

    protected boolean ensureArgsCount(String[] args, int expected, String label, CommandSender sender)
    {
        if (args.length >= expected)
            return true;

        PrintUtils.sendMessage(sender, "§cSubcommand \"" + label + "\" requires " + (expected - 1) + " arguments, but " + (args.length - 1) + " " + PrintUtils.indicative(args.length - 1) + " provided!");
        return false;
    }

    protected boolean ensureArgsCount(String[] args, int expected, int offset, String label, CommandSender sender)
    {
        if (args.length >= expected)
            return true;

        PrintUtils.sendMessage(sender, "§cSubcommand \"" + label + "\" requires " + (expected - 1 - offset) + " arguments, but " + (args.length - 1 - offset) + " " + PrintUtils.indicative(args.length - 1 - offset) + " provided!");
        return false;
    }

    protected boolean ensurePermission(CommandSender sender, int minRank)
    {
        try {
            if ((boolean) isAuthorized.invoke(permissions, sender, minRank))
                return true;
        } catch (InvocationTargetException | IllegalAccessException e) {
            PrintUtils.sendMessage(sender, "An exception occurred when attempting to check permissions. Please contact an administrator.");
            return false;
        }

        PrintUtils.sendMessage(sender, "§cYou do not have permission to use this command.");
        return false;
    }

    protected void sendErrorMessage(CommandSender sender, String input, String cmdLabel)
    {
        PrintUtils.sendMessage(sender, "§cUnknown subcommand \"" + input + "\" of parent \"" + cmdLabel + "\".");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args)
    {
        List<String> suggestions = new ArrayList<>();

        if (this.tabCompleteTree == null || args.length == 0)
            return suggestions;
        
        List<TabCompleteBranch> branches = tabCompleteTree.traverse(args);
        List<String> fullSuggestions = new ArrayList<>();
        
        branches.forEach(branch -> {
            fullSuggestions.addAll(branch.getOptions());
        });

        // Prune
        StringUtil.copyPartialMatches(args[args.length - 1], fullSuggestions, suggestions);

        return suggestions;
    }

    @Retention(RetentionPolicy.RUNTIME)
    protected @interface ZenCommandHandler
    {
        String usage();
        String description();
        int argsCount();
        int minAuthorizedRank();
    }

    @Retention(RetentionPolicy.RUNTIME)
    protected @interface ZenCommandRedirect
    {
        String[] labels();
        String command();
    }
}
