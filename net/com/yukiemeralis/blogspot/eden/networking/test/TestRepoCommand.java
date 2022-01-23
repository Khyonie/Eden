package com.yukiemeralis.blogspot.eden.networking.test;

import com.yukiemeralis.blogspot.eden.command.EdenCommand;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.networking.repos.RepositoryGui;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestRepoCommand extends EdenCommand 
{
    public TestRepoCommand(EdenModule parent_module) 
    {
        super("tedrepo", parent_module);
    }   

    @EdenCommandHandler(argsCount = 1, description = "Opens the test repository GUI.", usage = "tedrepo open")
    public void edencommand_open(CommandSender sender, String commandLabel, String[] args)
    {
        if (!(sender instanceof Player))
        {
            return;   
        }

        new RepositoryGui((Player) sender, TestRepository.repo).display((Player) sender);;
    }
}
