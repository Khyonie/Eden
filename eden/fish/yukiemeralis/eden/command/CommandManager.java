package fish.yukiemeralis.eden.command;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.annotation.PreventUnload;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;
import fish.yukiemeralis.eden.utils.option.Option;


/**
 * General-purpose handler for registering and unregistering Eden commands into the server.
 * @author Yuki_emeralis
 */
public class CommandManager 
{
    private static CommandMap commandMap;
    private static List<EdenCommand> knownEdenCommands = new ArrayList<>();

    /**
     * Registers a command to the commandmap.</p>
     * - Note that if this is executed outside of the server startup, commands will be missing from /help and suggestions.
     * @param fallback The parent command name to use
     * @param command The actual EdenCommand to be executed
     */
    public static void registerCommand(String fallback, EdenCommand command)
    {
        try {
            if (commandMap == null)
            {
                Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);

                commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            }

            if (knownEdenCommands.contains(command))
                return;

            commandMap.register(fallback, command);
            knownEdenCommands.add(command);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            PrintUtils.printPrettyStacktrace(e);
        }
    }
    
    public static void unregisterCommand(String commandName)
    {
        unregisterCommand(commandName, false);
    }

    /**
     * Removes a command from the commandmap
     * @param commandName The name of the command to remove
     */
    public static void unregisterCommand(String commandName, boolean force)
    {
        Command target = getCommand(commandName);

        if (target == null)
        {
            return;
        }

        if (target.getClass().isAnnotationPresent(PreventUnload.class) && !force)
        {
            PrintUtils.logVerbose("Attempted to unregister command \"[/" + commandName + "]\" but this command cannot be unregistered!" , InfoType.INFO);
            return;
        }

        try {
            Field scmField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            scmField.setAccessible(true);

            CommandMap commandMap = (CommandMap) getCraftCommandMapClass().cast(scmField.get(Bukkit.getServer()));

            knownEdenCommands.remove(getCommand(commandName));
            getReflectedCommandMap(commandMap).remove(commandName);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
            PrintUtils.printPrettyStacktrace(e);
        }
    }

    /**
     * Obtains a generic (not necessarily Eden) command from Bukkit's commandmap.
     * @param name The name of the command.
     * @return A command matching the name given.
     */
    public static Command getCommand(String name)
    {
        return commandMap.getCommand(name);
    }

    /**
     * Obtains a registered Eden command.
     * @param name The label of the command.
     * @return An Eden command matching the name given.
     */
    public static Option getEdenCommand(String name)
    {
        for (EdenCommand cmd : knownEdenCommands)
            if (cmd.getLabel().equals(name))
                return Option.some(cmd);
        return Option.none();
    }

    /**
     * Obtains a list of registered Eden commands.
     * @return
     */
    public static List<EdenCommand> getKnownCommands()
    {
        return knownEdenCommands;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Command> getReflectedCommandMap(CommandMap map) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method getKnownCommands = map.getClass().getMethod("getKnownCommands");

        return (Map<String, Command>) getKnownCommands.invoke(map);
    }

    private static Class<?> getCraftCommandMapClass() throws ClassNotFoundException
    {
        return Class.forName("org.bukkit.craftbukkit." + Eden.getNMSVersion() + ".command.CraftCommandMap");
    }
}
