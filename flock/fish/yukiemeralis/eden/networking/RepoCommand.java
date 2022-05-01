package fish.yukiemeralis.eden.networking;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.java.ModuleDisableFailureData;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;
import fish.yukiemeralis.eden.networking.enums.ModuleUpgradeStatus;
import fish.yukiemeralis.eden.networking.repos.EdenRepository;
import fish.yukiemeralis.eden.networking.repos.EdenRepositoryEntry;
import fish.yukiemeralis.eden.networking.repos.GlobalRepositoryGui;
import fish.yukiemeralis.eden.networking.repos.RepositoryGui;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.Option;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.Result;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@PreventUnload(CallerToken.EDEN)
public class RepoCommand extends EdenCommand 
{
    public RepoCommand(EdenModule parent_module) 
    {
        super("edenmr", parent_module);

        this.addBranch("^sync", "^upgrade", "^add", "^remove", "^open", "^exportblank", "^synctimestamps", "gentimestamp");
        this.getBranch("^add").addBranch("<URL>");
        this.getBranch("^remove").addBranch("<ALL_REPOSITORIES>");
        this.getBranch("^open").addBranch("<ALL_REPOSITORIES>");
        this.getBranch("^exportblank").addBranch("<NAME>");
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

    @EdenCommandHandler(usage = "edenmr upgrade", description = "Updates all updatable modules from their upstream repos.", argsCount = 1)
    public void edencommand_upgrade(CommandSender sender, String commandLabel, String[] args)
    {
        for (EdenRepository repo : NetworkingModule.getKnownRepositories().values())
        {
            for (EdenRepositoryEntry entry : repo.getEntries())
            {
                if (!NetworkingModule.getModuleUpgradeStatus(entry.getName(), entry).equals(ModuleUpgradeStatus.UPGRADABLE))
                    continue;

                String name = entry.getName();
                PrintUtils.sendMessage(sender, "§aAttempting to update " + name + "... (" + Eden.getModuleManager().getModuleByName(name).getVersion() + " -> " + entry.getVersion() + ")");

                String upstreamFilename = entry.fixUpstreamFilename(sender, NetworkingUtils.getFinalURLPortion(entry.getUrl()));

                if (upstreamFilename == null)
                    return;

                // Disable and unload relevant module
                EdenModule target = Eden.getModuleManager().getModuleByName(name);
                boolean enabled = target.getIsEnabled(); // Keep enabled modules enabled and disabled modules disabled

                if (enabled)
                {
                    Option<ModuleDisableFailureData> result = Eden.getModuleManager().disableModule(target.getName(), CallerToken.EDEN);

                    data: switch (result.getState())
                    {
                        case NONE: // Don't need to do anything
                            break data;
                        case SOME: // Disable failed, reload given modules
                            PrintUtils.sendMessage(sender, "§cFailed to disable module! Attempting to perform rollback on " + result.unwrap().getDownstreamModules().size() + " " + PrintUtils.plural(result.unwrap().getDownstreamModules().size(), "module", "modules") + "...");
                            PrintUtils.sendMessage(sender, "§c§oTechnical failure reason: " + result.unwrap().getReason().name());

                            if (result.unwrap().performRollback())
                            {
                                PrintUtils.sendMessage(sender, "§cRollback complete.");
                                return;
                            }

                            PrintUtils.sendMessage(sender, "§cRollback failed.");
                            return;
                    }
                }
                
                Eden.getModuleManager().removeModuleFromMemory(target.getName(), CallerToken.EDEN);

                // Delete the module file
                File f = new File(Eden.getModuleManager().getReferences().get(name));
                boolean success = f.delete();

                if (!success)
                {
                    PrintUtils.sendMessage(sender, "§cFailed to delete module file for this module! Please delete manually.");
                    return;
                }
                
                NetworkingUtils.downloadFileFromURLThreaded(entry.getUrl(), EdenRepositoryEntry.MODULE_FOLDER + upstreamFilename, new Thread() {
                    @Override
                    public void run()
                    {
                        PrintUtils.sendMessage(sender, "§aDownloaded " + name + "!");
                        NetworkingModule.updateLastKnownSync(entry);

                        // Run as sync
                        new BukkitRunnable() {
                            @Override
                            public void run() 
                            {
                                Result<EdenModule, String> result = Eden.getModuleManager().loadSingleModule(f.getAbsolutePath());

                                switch (result.getState())
                                {
                                    case ERR:
                                        PrintUtils.sendMessage(sender, "§cFailed to reload module! (Error: " + result.unwrap() + ")");
                                        return;
                                    case OK:
                                        if (sender instanceof Player)
                                            new RepositoryGui(entry.getHostRepo()).display((Player) sender);
                                        
                                            PrintUtils.sendMessage(sender, "§aUpdated " + name + "!");
                                        break;
                                }

                                if (enabled)
                                    Eden.getModuleManager().enableModule((EdenModule) result.unwrap());
                            }
                        }.runTask(Eden.getInstance());
                    }
                });
            }
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
        if (!NetworkingModule.getKnownRepositories().containsKey(args[1]))
        {
            PrintUtils.sendMessage(sender, "§cUnknown repository \"" + args[1] + "\".");
            return;
        }
        
        File f = new File("./plugins/Eden/repos/" + NetworkingModule.getKnownRepositories().get(args[1]).getName() + ".json");
        if (!f.exists())
        {
            PrintUtils.sendMessage(sender, "§cUnknown repository \"" + args[1] + "\"! Ensure the file's name and the repository's name are the same.");
        }
        
        if (f.delete())
        {
            NetworkingModule.getKnownRepositories().remove(args[1]);
            PrintUtils.sendMessage(sender, "§aDeleted repository!");
            return;
        }

        PrintUtils.sendMessage(sender, "§cFailed to delete repository!");
    }

    @EdenCommandHandler(usage = "edenmr open <REPOSITORY>", description = "Opens a repository in a GUI. If a URL is given, syncs the repository and opens it.", argsCount = 2)
    public void edencommand_open(CommandSender sender, String commandLabel, String[] args)
    {
        if (sender instanceof ConsoleCommandSender)
        {
            PrintUtils.sendMessage(sender, "§cOnly players can use this command.");
            return;
        }

        // Check for a URL first
        try {
            URL url = new URL(args[1]);

            // Download repo to a temp file, open it, and delete the file
            EdenRepository repo = null;
            try (InputStream in = url.openStream())
            {
                repo = JsonUtils.getGson().fromJson(new InputStreamReader(in), EdenRepository.class);
            } catch (IOException | JsonIOException e) {
                // URL is valid but connection failed for whatever reason
                PrintUtils.sendMessage(sender, "§cFailed to open repo from URL! Please check the URL or try again later.");
                return;
            } catch (JsonSyntaxException e) {
                PrintUtils.sendMessage(sender, "§cInvalid or corrupt repository!");
                return;
            }

            if (repo == null)
            {
                PrintUtils.sendMessage(sender, "§cInvalid or corrupt repository!");
                return;
            }

            // Repo is probably fine
            repo.setRepoReferences(); // So we can sync upstream just from this temp repo
            new RepositoryGui(repo, true).display((Player) sender);

            return;
        } catch (MalformedURLException e) {}

        // Not a URL, so try to find a synced repo with the given name

        if (!NetworkingModule.getKnownRepositories().containsKey(args[1]))
        {
            PrintUtils.sendMessage(sender, "§cUnknown repository \"§e" + args[1] + "§c\"!");
            return;
        }

        new RepositoryGui(NetworkingModule.getKnownRepositories().get(args[1])).display((Player) sender);
    }

    @EdenCommandHandler(usage = "edenmr synctimestamps", description = "Syncs module timestamps to their upstream repos.", argsCount = 1)
    public void edencommand_synctimestamps(CommandSender sender, String commandLabel, String[] args)
    {
        for (EdenRepository repo : NetworkingModule.getKnownRepositories().values())
            for (EdenRepositoryEntry entry : repo.getEntries())
                if (entry.getTimestamp() < NetworkingModule.getLastKnownSync(entry.getName()))
                    NetworkingModule.updateLastKnownSync(entry);
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

        new GlobalRepositoryGui().display((Player) sender);
    }

    @EdenCommandHandler(usage = "edenmr exportblank <NAME>", description = "Generates a blank repository file with the given name.", argsCount = 2)
    public void edencommand_exportblank(CommandSender sender, String commandLabel, String[] args)
    {
        EdenRepository repo = new EdenRepository(args[1], "https://google.com", System.currentTimeMillis() / 1000L);
        repo.getEntries().add(new EdenRepositoryEntry("Dummy", "https://google.com/", "0.0", "N/A", System.currentTimeMillis() / 1000L, "v1_18_R2"));

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

    @EdenCommandHandler(usage = "edenmr gentimestamp", description = "Generates a UNIX epoch timestamp for repo syncs.", argsCount = 1)
    public void edencommand_gentimestamp(CommandSender sender, String commandLabel, String[] args)
    {
        PrintUtils.sendMessage(sender, "Current UNIX epoch timestamp: [ §b" + (System.currentTimeMillis() / 1000L) + "§7 ]");
    }
}