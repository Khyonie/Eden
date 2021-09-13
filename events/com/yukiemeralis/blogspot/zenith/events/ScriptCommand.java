package com.yukiemeralis.blogspot.zenith.events;

import com.yukiemeralis.blogspot.zenith.command.ZenithCommand;
import com.yukiemeralis.blogspot.zenith.events.gui.GlobalScriptListGui;
import com.yukiemeralis.blogspot.zenith.events.gui.ScriptEditGui;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScriptCommand extends ZenithCommand
{
    public ScriptCommand(ZenithModule parent_module)
    {
        super("scripts", parent_module);    
        
        this.addBranch("add", "remove", "edit");
        this.getBranch("add").addBranch("<name>");
        this.getBranch("remove").addBranch("<name>");
        this.getBranch("edit").addBranch("<name>");
    }   

    @ZenCommandHandler(usage = "scripts", description = "Displays a list of scripts.", argsCount = 0)
    public void zcommand_nosubcmd(CommandSender sender, String commandLabel, String[] args)
    {
        if (!(sender instanceof Player))
        {
            PrintUtils.sendMessage(sender, "Only players may use this command.");
            return;
        }

        new GlobalScriptListGui((Player) sender).display((Player) sender);
    }

    @ZenCommandHandler(usage = "scripts add <name>", description = "Creates a new blank script.", argsCount = 2)
    public void zcommand_add(CommandSender sender, String commandLabel, String[] args)
    {
        if (ZenithEvents.getScripts().containsKey(args[1]))
        {
            PrintUtils.sendMessage(sender, "A script with that name already exists. Please choose a different name.");
            return;
        }

        PrintUtils.sendMessage(sender, "Successfully created an empty script named \"" + args[1] + "\".");
        ZenithEvents.getScripts().put(args[1], new EventScript(args[1]));
        new ScriptEditGui((Player) sender, ZenithEvents.getScripts().get(args[1])).display((Player) sender);
    }

    @ZenCommandHandler(usage = "scripts remove <name>", description = "Deletes a script.", argsCount = 2)
    @ZenCommandRedirect(labels = "delete", command = "scripts remove args")
    public void zcommand_remove(CommandSender sender, String commandLabel, String[] args)
    {

    }

    @ZenCommandHandler(usage = "scripts edit <name>", description = "Edit a created script.", argsCount = 2)
    public void zcommand_edit(CommandSender sender, String commandLabel, String[] args)
    {
        if (!ZenithEvents.getScripts().containsKey(args[1]))
        {
            PrintUtils.sendMessage(sender, "Could not find a script by that name.");
            return;
        }

        new ScriptEditGui((Player) sender, ZenithEvents.getScripts().get(args[1])).display((Player) sender);
    }
}
