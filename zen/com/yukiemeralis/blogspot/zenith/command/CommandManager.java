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

            command.init();
            commandMap.register(fallback, command);
            knownZenCommands.add(command);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
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
            //PrintUtils.logVerbose("Attemped to unregister unknown command /\"" + commandName + "\"!", InfoType.ERROR);
            return;
        }

        if (target.getClass().isAnnotationPresent(PreventUnload.class))
        {
            PrintUtils.logVerbose("Attempted to unregister command \"/" + commandName + "\" but this command cannot be unregistered!" , InfoType.INFO);
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
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param name
     * @return
     */
    public static Command getCommand(String name)
    {
        return commandMap.getCommand(name);
    }

    public static List<ZenithCommand> getKnownCommands()
    {
        return knownZenCommands;
    }
}
