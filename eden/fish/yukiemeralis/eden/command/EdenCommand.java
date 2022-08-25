package fish.yukiemeralis.eden.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.annotations.EdenCommandHandler;
import fish.yukiemeralis.eden.command.annotations.EdenCommandRedirect;
import fish.yukiemeralis.eden.command.tabcomplete.TabCompleteBranch;
import fish.yukiemeralis.eden.command.tabcomplete.TabCompleteMultiBranch;
import fish.yukiemeralis.eden.command.tabcomplete.TabCompleteTree;
import fish.yukiemeralis.eden.command.tabcomplete.TabCompleter;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.ChatUtils.ChatAction;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;

/**
 * An instance of an Eden command. Please look at the wiki @ https://github.com/YukiEmeralis/Eden for documentation.
 * @author Yuki_emeralis
 */
public abstract class EdenCommand extends Command
{
    private Map<String, String> redirects = new HashMap<>();
    private EdenModule parent_module = null;

    private TabCompleteTree tree = new TabCompleteTree();

    /**
     * An Eden command with the specified name.
     * @param name The name of the command, as in "/name".
     * @deprecated All commands must specify their parent module, for the sake of permission generation. Will be removed on release.
     */
    @Deprecated(forRemoval = true)
    public EdenCommand(String name) 
    {
        super(name, "EdenCommand: " + name, "EdenCommand: " + name, new ArrayList<>());
        generateRedirects();
    }
    
    /**
     * An Eden command with the specified name and a list of potential aliases.
     * @param name The name of the command, as in "/name".
     * @param aliases A list of potential aliases for this command.
     * @deprecated All commands must specify their parent module, for the sake of permission generation. Will be removed on release.
     */
    @Deprecated(forRemoval = true)
    public EdenCommand(String name, List<String> aliases) 
    {
        super(name, "EdenCommand: " + name, "EdenCommand: " + name, aliases);
        generateRedirects();
    }

    /**
     * An Eden command with the specified name and tied to the given module. Commands tied to a module are categorized with the /eden helpall command.
     * @param name The name of the command, as in "/name".
     * @param parent_module The module this command is tied to.
     */
    public EdenCommand(String name, EdenModule parent_module) 
    {
        super(name, "EdenCommand: " + name, "EdenCommand: " + name, new ArrayList<>());
        generateRedirects();
        this.parent_module = parent_module;
    }
    
    /**
     * An Eden command with the specified name, a list of potential aliases, and tied to the given module. Commands tied to a module are categorized with the /eden helpall command.
     * @param name The name of the command, as in "/name".
     * @param aliases A list of potential aliases for this command.
     * @param parent_module parent_module The module this command is tied to.
     */
    public EdenCommand(String name, List<String> aliases, EdenModule parent_module) 
    {
        super(name, "EdenCommand: " + name, "EdenCommand: " + name, aliases);
        generateRedirects();
        this.parent_module = parent_module;
    }

    private void generateRedirects()
    {
        // Go over each method in this class to check if the method contains a redirect annotation
        for (Method m : this.getClass().getDeclaredMethods())
        {
            if (m.isAnnotationPresent(EdenCommandRedirect.class))
            {
                // Redirect is present, check for validity
                EdenCommandRedirect a = m.getAnnotation(EdenCommandRedirect.class);
                
                for (String label : a.labels())
                {
                    if (redirects.containsKey(label))
                    {
                        PrintUtils.log("(Command " + this.getName() + " \\\\(" + this.getClass().getName() + "\\\\) contains multiple redirects for redirect label \"" + label + "\")!", InfoType.WARN);
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
                cmd_method = this.getClass().getMethod("edencommand_" + args[0].toLowerCase(), CommandSender.class, String.class, String[].class);

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
                cmd_method = this.getClass().getMethod("edencommand_nosubcmd", CommandSender.class, String.class, String[].class);
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
        EdenCommandHandler cmd_info = null;

        if (!cmd_method.isAnnotationPresent(EdenCommandHandler.class))
        {
            PrintUtils.sendMessage(sender, "§cThis command does not contain a handler! Cannot execute, please contact an administrator.");

            if (!isBlankCommand)
            {
                PrintUtils.log("<Command \"/" + args[0] + "\" inside " + this.getClass().getName() + " does not specify a handler! Please contact this module's maintainer.>", InfoType.ERROR);
                return;
            }
            
            PrintUtils.log("<Command \"/" + commandLabel + "\" inside " + this.getClass().getName() + " does not specify a handler! Please contact this module's maintainer.>", InfoType.ERROR);
            return;
        }

        cmd_info = cmd_method.getAnnotation(EdenCommandHandler.class);
        
        // Check args count
        if (args.length < cmd_info.argsCount() && !isBlankCommand)
        {
            PrintUtils.sendMessage(sender, "§cSubcommand \"" + args[0] + "\" requires " + (cmd_info.argsCount() - 1) + " arguments, but " + (args.length - 1) + " " + PrintUtils.indicative(args.length - 1) + " provided!");
            return;
        }

        // Check permissions
        String permission = generatePermission(this.parent_module, commandLabel, args);
        
        if (!ensurePermission(sender, permission.replace("^", "")))
            return;

        // Check if any component of the command requires elevation

        if (permission.contains("^") && sender instanceof Player)
        {
            if (!Eden.getPermissionsManager().isElevated((Player) sender)) 
            {
                if (!Eden.getPermissionsManager().getPlayerData((Player) sender).hasPassword())
                {
                    PrintUtils.sendMessage(sender, "§cThis command requires elevated access, however you do not have a password set up for this account. Please contact an administrator.");
                    return;
                }

                handleElevatedCommand(cmd_method, sender, commandLabel, args);
                return;
            }
        }

        // Run the command
        try {
            cmd_method.invoke(this, sender, commandLabel, args);
        } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
            PrintUtils.sendMessage(sender, "§cAn internal error occurred. Please contact an administrator.");
            PrintUtils.log("§cFailed to execute a command! See below for details and a stacktrace.", InfoType.ERROR);
            PrintUtils.printPrettyStacktrace(e);
        }
    }

    /**
     * A shared help command, always present with every Eden command. 
     * <p><p>
     * <b>Should not be invoked manually, unless linked to another command.</b>
     * @param sender The command sender.
     * @param commandLabel The label used for this command.
     * @param args A list of arguments in string format.
     */
    @EdenCommandHandler(usage = "<parent command> help", description = "Provides a list of subcommands tied to this parent command.", argsCount = 1)
    public void edencommand_help(CommandSender sender, String commandLabel, String[] args)
    {
        PrintUtils.sendMessage(sender, "§6----[ §eSubcommands for " + commandLabel + " §6]----");
        for (Method m : this.getClass().getDeclaredMethods())
        {
            if (!m.getName().startsWith("edencommand_")) // Make sure the method is tied to a command
                continue;
                
            if (!m.isAnnotationPresent(EdenCommandHandler.class)) // Ensure that the command has a handler attached
            {
                PrintUtils.sendMessage(sender, "§c/§6" + commandLabel + "§c " + m.getName().replaceAll("edencommand_|nosubcmd", "") + " - This command does not specify a handler!");
                continue;
            }

            PrintUtils.sendMessage(sender, "§7/§a" + commandLabel + "§e " + m.getName().replaceAll("edencommand_|nosubcmd", "") + " §7- " + m.getAnnotation(EdenCommandHandler.class).description());
        }
    }

    private void handleElevatedCommand(Method cmd_method, CommandSender sender, String commandLabel, String[] args)
    {
        ChatAction action = new ChatAction()
        {
            @Override
            public void run()
            {
                String input = ChatUtils.receiveResult(sender);
                ChatUtils.deleteResult(sender);

                if (input.toLowerCase().equals("cancel"))
                {
                    PrintUtils.sendMessage(sender, "Exitted password entry mode. It is no longer safe to enter your password.");
                    return;
                }
                    
                if (!Eden.getPermissionsManager().getPlayerData((Player) sender).comparePassword(input))
                {
                    PrintUtils.sendMessage(sender, "§cIncorrect password. Please try again later.");
                    return;
                }

                // Synchronize with ticks, since downstream code isn't guaranteed to be async
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        try {
                            cmd_method.invoke(getInstance(), sender, commandLabel, args);
                        } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
                            PrintUtils.sendMessage(sender, "§cAn internal error occurred. Please contact an administrator.");
                            PrintUtils.log("<Failed to execute a command! See below for details and a stacktrace.>", InfoType.ERROR);
                            PrintUtils.printPrettyStacktrace(e);
                        }
                    }
                }.runTask(Eden.getInstance());

                // Mark user as elevated
                Eden.getPermissionsManager().addElevatedUser((Player) sender);
                PrintUtils.sendMessage(sender, "<#A9EED1>You are now elevated. With great power comes great responsibility.");
            }
        };

        PrintUtils.sendMessage(sender, "This command requires elevation, please enter your password:");
        ChatUtils.expectChat(sender, action);
    }

    /**
     * Obtains the module this command is tied to.
     * @return This command's parent module.
     */
    public EdenModule getParentModule()
    {
        return this.parent_module;
    }

    /**
     * Performs a command redirection.
     * @param label The label to redirect to.
     * @param sender The command sender.
     * @param commandLabel The label used for this command.
     * @param args A list of arguments in string format.
     */
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

        PrintUtils.logVerbose("Redirect command | " + oldCommand + " \\\\> " + newCommand, InfoType.INFO);
        
        Eden.getInstance().getServer().dispatchCommand(sender, newCommand);
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

    protected boolean ensurePermission(CommandSender sender, String permission)
    {
        if (!Eden.getPermissionsManager().isAuthorized(sender, permission))
        {
            PrintUtils.sendMessage(sender, "§cYou do not have permission to use this command.");
            return false;
        }

        return true;
    }

    /**
     * Sends a CommandSender an error message about an invalid subcommand.
     * @param sender The sender to notify
     * @param input The input given by the sender
     * @param cmdLabel The base command 
     */
    protected void sendErrorMessage(CommandSender sender, String input, String cmdLabel)
    {
        PrintUtils.sendMessage(sender, "§cUnknown subcommand \"" + input + "\" of parent \"" + cmdLabel + "\".");
    }

    //
    // Permissions
    //

    public boolean testBasePermission(CommandSender sender, String label)
    {
        return Eden.getPermissionsManager().isAuthorized(sender, this.parent_module.getName() + "." + label);
    }

    protected String generatePermission(EdenModule module, String base, String[] args)
    {
        // Start with a base permission, with the module and command label
        String permission = module.getName() + "." + base;

        // No args are present, return the base as-is
        if (args.length == 0)
        {
            PrintUtils.logVerbose("Generated singleton permission \"" + permission + "\"", InfoType.INFO);
            return permission;
        }

        // Traverse tree to see if any given arg is a parameter, or elevated
        List<Integer> paramIndexes = new ArrayList<>();
        List<Integer> elevatedIndexes = new ArrayList<>();

        // Start at a branch matching the given subcommand
        TabCompleteBranch branch = this.getBranch(args[0]);

        if (branch == null)
            return permission; // Command is likely invalid, which is a situation that is handled

        if (branch.getLabel().startsWith("^"))
        {
            elevatedIndexes.add(0);
        }

        // Start at the first command argument
        for (int i = 1; i < args.length; i++)
        {
            // Ignore branches that go nowhere
            if (branch.isLeaf())
                break;

            // Check for user argument
            if (branch.getBranchesFromHere().size() == 1) // Possibly a user argument, lets check some more
            {
                if (branch.getBranchesFromHere().get(0).startsWith("<") && branch.getBranchesFromHere().get(0).endsWith(">"))
                {
                    paramIndexes.add(i);

                    branch = (TabCompleteBranch) branch.getBranch(branch.getBranchesFromHere().get(0));
                    continue;
                }
            }

            branch = (TabCompleteBranch) branch.getBranch(args[i]);

            if (branch == null)
            {
                break;
            }

            // Check for elevation
            if (branch.getLabel().startsWith("^"))
            {
                elevatedIndexes.add(i);
            }
        }

        String marker = "";
        int index = -1;
        for (String str : args)
        {
            index++;
            marker = "";
            if (elevatedIndexes.contains(index))
                marker = "^";
            if (!paramIndexes.contains(index))
                permission = permission + "." + marker + str;
        }

        PrintUtils.logVerbose("Generated permission \"" + permission + "\"", InfoType.INFO);
        return permission;
    }

    /**
     * Gets the TabCompleteTree associated with this command.
     * @return This command's TabCompleteTree.
     */
    public TabCompleteTree getTree()
    {
        return this.tree;
    }

    protected TabCompleteBranch addBranch(String... labels)
    {
        tree.addBranch(labels);

        return tree.getBranch(labels[0]);
    }

    /**
     * Obtains a first-level branch for this command's TabCompleteTree.
     * @param label The full label for an existing branch.
     * @return A branch from this command's TabCompleteTree.
     */
    public TabCompleteBranch getBranch(String label)
    {
        return tree.getBranch(label);
    }

    public TabCompleteMultiBranch getMultiBranch(String... labels)
    {
        List<TabCompleter> data = new ArrayList<>();

        for (String label : labels)
            data.add(this.tree.getBranch(label));

        return new TabCompleteMultiBranch(data);
    }

    private EdenCommand getInstance()
    {
        return this;
    }
}