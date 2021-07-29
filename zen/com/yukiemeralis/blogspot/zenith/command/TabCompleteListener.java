package com.yukiemeralis.blogspot.zenith.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import com.yukiemeralis.blogspot.zenith.module.java.annotations.Unimplemented;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

/**
 * Listener for Zenith TabComplete suggestions.
 * @author Yuki_emeralis
 */
@Unimplemented("Traversal for TabCompleteTrees has not been finished. Signed Yuki_emeralis 6/24/2021")
public class TabCompleteListener implements Listener
{
	/**
	 * Fired when a user attempts to generate a tabcomplete.
	 * @param event The event fired.
	 */
    public void onTabComplete(TabCompleteEvent event)
    {
        String[] splitBuffer = event.getBuffer().split(" ");

        String label = event.getBuffer().split(" ")[0].replace("/", "");
        CommandSender sender = event.getSender();
        String[] args;

        PrintUtils.log("------", InfoType.WARN);

        PrintUtils.log("Parsing: \"" + event.getBuffer() + "\"", InfoType.INFO);
        PrintUtils.log("Command label: " + label, InfoType.INFO);

        if (splitBuffer.length == 0)
            return;

        args = new String[splitBuffer.length - 1];

        for (int i = 1; i < splitBuffer.length; i++)
        {
            args[i - 1] = splitBuffer[i];
            PrintUtils.log("Argument: " + splitBuffer[i], InfoType.INFO);
        }

        Command command = CommandManager.getCommand(label);

        if (CommandManager.getCommand(label) == null)
            return;

        ZenithCommand zencommand = (ZenithCommand) command;

        List<String> suggestions = zencommand.onTabComplete(sender, command, label, args);
        PrintUtils.log("Suggestions list size contains " + suggestions.size() + " elements", InfoType.INFO);
        event.getCompletions().addAll(suggestions);
    }
}
