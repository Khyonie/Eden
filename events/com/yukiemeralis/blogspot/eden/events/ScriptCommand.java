package com.yukiemeralis.blogspot.eden.events;

import com.yukiemeralis.blogspot.eden.command.EdenCommand;
import com.yukiemeralis.blogspot.eden.events.gui.GlobalScriptListGui;
import com.yukiemeralis.blogspot.eden.events.gui.ScriptEditGui;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScriptCommand extends EdenCommand
{
    public ScriptCommand(EdenModule parent_module)
    {
        super("scripts", parent_module);    
        
        this.addBranch("add", "remove", "edit");
        this.getBranch("add").addBranch("<name>");
        this.getBranch("remove").addBranch("<name>");
        this.getBranch("edit").addBranch("<name>");
    }   

    @EdenCommandHandler(usage = "scripts", description = "Displays a list of scripts.", argsCount = 0)
    public void edencommand_nosubcmd(CommandSender sender, String commandLabel, String[] args)
    {
        if (!(sender instanceof Player))
        {
            PrintUtils.sendMessage(sender, "Only players may use this command.");
            return;
        }

        new GlobalScriptListGui((Player) sender).display((Player) sender);
    }

    @EdenCommandHandler(usage = "scripts add <name>", description = "Creates a new blank script.", argsCount = 2)
    public void edencommand_add(CommandSender sender, String commandLabel, String[] args)
    {
        if (EdenEvents.getScripts().containsKey(args[1]))
        {
            PrintUtils.sendMessage(sender, "A script with that name already exists. Please choose a different name.");
            return;
        }

        PrintUtils.sendMessage(sender, "Successfully created an empty script named \"" + args[1] + "\".");
        EdenEvents.getScripts().put(args[1], new EventScript(args[1]));
        new ScriptEditGui((Player) sender, EdenEvents.getScripts().get(args[1])).display((Player) sender);
    }

    @EdenCommandHandler(usage = "scripts remove <name>", description = "Deletes a script.", argsCount = 2)
    @EdenCommandRedirect(labels = "delete", command = "scripts remove args")
    public void edencommand_remove(CommandSender sender, String commandLabel, String[] args)
    {

    }

    @EdenCommandHandler(usage = "scripts edit <name>", description = "Edit a created script.", argsCount = 2)
    public void edencommand_edit(CommandSender sender, String commandLabel, String[] args)
    {
        if (!EdenEvents.getScripts().containsKey(args[1]))
        {
            PrintUtils.sendMessage(sender, "Could not find a script by that name.");
            return;
        }

        new ScriptEditGui((Player) sender, EdenEvents.getScripts().get(args[1])).display((Player) sender);
    }
}
