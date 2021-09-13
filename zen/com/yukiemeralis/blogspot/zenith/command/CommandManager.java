package com.yukiemeralis.blogspot.zenith.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

/**
 * General-purpose handler for registering and unregistering Zenith commands into the server.
 * @author Yuki_emeralis
 */
public class CommandManager 
{
    private static CommandMap commandMap;
    private static List<ZenithCommand> knownZenCommands = new ArrayList<>();

    /**
     * Registers a command to the commandmap.</p>
     * - Note that if this is executed outside of the server startup, commands will be missing from /help and suggestions.
     * @param fallback The parent command name to use
     * @param command The actual ZenithCommand to be executed
     */
    public static void registerCommand(String fallback, ZenithCommand command)
    {
        try {
            if (commandMap == null)
            {
                Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);

                commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            }

            if (knownZenCommands.contains(command))
                return;

            commandMap.register(fallback, command);
            knownZenCommands.add(command);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            PrintUtils.printPrettyStacktrace(e);
        }
    } 

    /**
     * Removes a command from the commandmap
     * @param commandName The name of the command to remove
     */
    public static void unregisterCommand(String commandName)
    {
        Command target = getCommand(commandName);

        if (target == null)
        {
            return;
        }

        if (target.getClass().isAnnotationPresent(PreventUnload.class))
        {
            PrintUtils.logVerbose("Attempted to unregister command \"[/" + commandName + "]\" but this command cannot be unregistered!" , InfoType.INFO);
            return;
        }

        try {
            Field scmField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            scmField.setAccessible(true);

            org.bukkit.command.CommandMap scm;

            switch (Zenith.getNMSVersion())
            {
                case "v1_16_R3":
                    scm = (org.bukkit.craftbukkit.v1_16_R3.command.CraftCommandMap) scmField.get(Bukkit.getServer());
                    knownZenCommands.remove(getCommand(commandName));
                    ((org.bukkit.craftbukkit.v1_16_R3.command.CraftCommandMap) scm).getKnownCommands().remove(commandName);
                    break;
                case "v1_17_R1":
                    scm = (org.bukkit.craftbukkit.v1_17_R1.command.CraftCommandMap) scmField.get(Bukkit.getServer());
                    knownZenCommands.remove(getCommand(commandName));
                    ((org.bukkit.craftbukkit.v1_17_R1.command.CraftCommandMap) scm).getKnownCommands().remove(commandName);
                    break;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            PrintUtils.printPrettyStacktrace(e);
        }
    }

    /**
     * Obtains a generic (not necessarily Zenith) command from Bukkit's commandmap.
     * @param name The name of the command.
     * @return A command matching the name given.
     */
    public static Command getCommand(String name)
    {
        return commandMap.getCommand(name);
    }

    /**
     * Obtains a registered Zenith command.
     * @param name The label of the command.
     * @return A Zenith command matching the name given.
     */
    public static ZenithCommand getZenithCommand(String name)
    {
        for (ZenithCommand cmd : knownZenCommands)
            if (cmd.getLabel().equals(name))
                return cmd;
        return null;
    }

    /**
     * Obtains a list of registered Zenith commands.
     * @return
     */
    public static List<ZenithCommand> getKnownCommands()
    {
        return knownZenCommands;
    }
}
