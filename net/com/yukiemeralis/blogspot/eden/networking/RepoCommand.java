package com.yukiemeralis.blogspot.eden.networking;

import java.io.File;

import com.yukiemeralis.blogspot.eden.command.EdenCommand;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.networking.repos.EdenRepository;
import com.yukiemeralis.blogspot.eden.networking.repos.EdenRepositoryEntry;
import com.yukiemeralis.blogspot.eden.networking.repos.GlobalRepositoryGui;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RepoCommand extends EdenCommand 
{
    public RepoCommand(EdenModule parent_module) 
    {
        super("edenmr", parent_module);

        this.addBranch("sync", "update", "add", "remove", "open", "exportblank");
        this.getBranch("add").addBranch("<URL>");
        this.getBranch("remove").addBranch("<ALL_REPOSITORIES>");
        this.getBranch("open").addBranch("<ALL_REPOSITORIES>");
        this.getBranch("exportblank").addBranch("<NAME>");
    }

    @EdenCommandHandler(usage = "edenmr sync", description = "Synchronizes all added repos with their upstream repos.", argsCount = 1)
    @EdenCommandRedirect(labels = { "update" }, command = "edenmr sync")
    public void edencommand_sync(CommandSender sender, String commandLabel, String[] args)
    {
        for (EdenRepository repo : NetworkingModule.getKnownRepositories().values())
        {
            EdenRepository.downloadRepoFromUrl(sender, repo.getHostUrl());
        }
    }

    @EdenCommandHandler(usage = "edenmr add <URL>", description = "Adds an Eden repository from a URL", argsCount = 2)
    public void edencommand_add(CommandSender sender, String commandLabel, String[] args)
    {
        EdenRepository.downloadRepoFromUrl(sender, args[1]);
    }

    @EdenCommandHandler(usage = "edenmr remove <REPOSITORY>", description = "Removes an Eden repository.", argsCount = 2)
    public void edencommand_remove(CommandSender sender, String commandLabel, String[] args)
    {
        
    }

    @EdenCommandHandler(usage = "edenmr open <REPOSITORY>", description = "Opens a repository in a GUI. If a URL is given, syncs the repository and opens it.", argsCount = 2)
    public void edencommand_open(CommandSender sender, String commandLabel, String[] args)
    {
        
    }

    @EdenCommandHandler(usage = "edenmr", description = "Opens a GUI of all added repositories.", argsCount = 0)
    public void edencommand_nosubcmd(CommandSender sender, String commandLabel, String[] args)
    {
        if (!(sender instanceof Player))
        {
            PrintUtils.sendMessage(sender, "§e-----[ Repositories ]-----");
            NetworkingModule.getKnownRepositories().forEach((name, entry) -> PrintUtils.sendMessage(sender, name));
            return;
        }

        new GlobalRepositoryGui((Player) sender).display((Player) sender);
    }

    @EdenCommandHandler(usage = "edenmr exportblank <NAME>", description = "Generates a blank repository file with the given name.", argsCount = 2)
    public void edencommand_exportblank(CommandSender sender, String commandLabel, String[] args)
    {
        EdenRepository repo = new EdenRepository(args[1]);
        repo.getEntries().add(new EdenRepositoryEntry("Dummy", "https://google.com/", "0.0", "N/A", "v1_18_R1"));

        JsonUtils.toJsonFile("./plugins/Eden/dlcache/" + args[1] + ".json", repo);

        File f = new File("./plugins/Eden/dlcache/" + args[1] + ".json");
    
        if (!f.exists())
        {
            PrintUtils.sendMessage(sender, "§cFailed to create blank repo!");
            return;
        }

        if (f.length() == 0)
        {
            PrintUtils.sendMessage(sender, "§cFailed to export dummy repository!");
            return;
        }

        PrintUtils.sendMessage(sender, "§aCreated blank repository at \"§b" + f.getAbsolutePath() + "§a\"!");
    }
}