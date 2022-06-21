package fish.yukiemeralis.eden.networking.repos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import fish.yukiemeralis.eden.networking.NetworkingModule;
import fish.yukiemeralis.eden.networking.NetworkingUtils;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.utils.ItemUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EdenRepository implements GuiComponent
{
    /**
     * The name of this repository.
     */
    @Expose
    private String name;

    /**
     * URL the document that represents this repository is hosted on.
     */
    @Expose
    private String hostUrl;

    /**
     * Current time in unix seconds. Used to compare upstream and installed versions.
     */
    @Expose
    private long timestamp;

    /**
     * A list of the modules this repository contains
     */
    @Expose
    private List<EdenRepositoryEntry> entries; 

    public EdenRepository(String name, String hostUrl, long timestamp)
    {
        this.name = name;
        this.hostUrl = hostUrl;
        this.timestamp = timestamp;
        this.entries = new ArrayList<>();
    }

    public String getName()
    {
        return this.name;
    }

    public List<EdenRepositoryEntry> getEntries()
    {
        return this.entries;
    }

    public String getHostUrl()
    {
        return this.hostUrl;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    public static enum RepoSyncResult
    {
        CORRUPT,
        INVALID_URL,
        NOT_A_REPO
        ;
    }

    public static void downloadRepoFromUrl(CommandSender feedbackViewer, String url)
    {
        downloadRepoFromUrl(feedbackViewer, url, false);
    }

    public static void downloadRepoFromUrl(CommandSender feedbackViewer, String url, boolean deleteAfter)
    {
        File f = new File("./plugins/Eden/repos/" + NetworkingUtils.getFinalURLPortion(url));

        if (!f.getName().endsWith(".json"))
        {
            PrintUtils.sendMessage(feedbackViewer, "§cCannot pull filename from the given URL, going to try and load it anyways...");
        }

        NetworkingUtils.downloadFileFromURLThreaded(url, f.getAbsolutePath(), new Thread() {
            @Override
            public void run()
            {
                // Parse to make sure it's valid
                if (f.length() == 0)
                {
                    PrintUtils.sendMessage(feedbackViewer, "§cFailed to download module repository file! Please check the URL or try again later.");
                    return;
                }
                
                EdenRepository repo = JsonUtils.fromJsonFile(f.getAbsolutePath(), EdenRepository.class);
                String oldName = null;

                if (repo == null)
                {
                    PrintUtils.sendMessage(feedbackViewer, "§cInvalid or corrupt module repository!");
                    f.delete();
                    return;
                }

                if (!f.getName().endsWith(".json"))
                {
                    if (repo.getName() == null)
                    {
                        PrintUtils.sendMessage(feedbackViewer, "§cInvalid or corrupt module repository!");
                        f.delete();
                        return;
                    }
                
                    // Fix name
                    oldName = f.getName();
                    File target = new File(f.getParentFile().getAbsolutePath() + "/" + repo.getName() + ".json");
                    if (target.exists())
                        target.delete();
                    f.renameTo(target);
                    
                    PrintUtils.sendMessage(feedbackViewer, "§aFixed repository name.");
                }
                
                if (f.exists())
                    f.delete();
                
                JsonUtils.toJsonFile(f.getAbsolutePath(), repo);

                // If we fixed the name, delete the old copy
                if (oldName != null)
                {
                    try {
                        File oldFile = new File(f.getParentFile().getCanonicalPath() + "/" + oldName);
                        Files.delete(oldFile.toPath());
                    } catch (IOException e) {
                        PrintUtils.printPrettyStacktrace(e);
                    }
                }

                NetworkingModule.getKnownRepositories().put(repo.getName(), repo);
                repo.setRepoReferences();

                PrintUtils.sendMessage(feedbackViewer, "§aSuccessfully downloaded and synced repository \"§b" + repo.getName() + "§a\"!");
            }
        });

        if (deleteAfter)
            f.delete();
    }

    /**
     * Sets all repo entries in this repo to have a reference back to its host
     */
    public void setRepoReferences()
    {
        for (EdenRepositoryEntry e : entries)
            e.setHostRepo(this);
    }

    private GuiItemStack item;

    @Override
    public GuiItemStack generate() 
    {
        if (item != null)
            return item;
        item = new GuiItemStack(ItemUtils.build(Material.BOOKSHELF, "§r§9§l" + name, "§r§7§o" + entries.size() + " " + PrintUtils.plural(entries.size(), "entry", "entries"))) 
        {
            @Override
            public void onInteract(InventoryClickEvent event) 
            {
                new RepositoryGui(getInstance()).display(event.getWhoClicked());;
            }
        };

        return item;
    }

    @Override
    public void onInteract(InventoryClickEvent event) 
    {
        if (item != null)
        {
            item.onInteract(event);
            return;
        }

        generate().onInteract(event);
    }

    private EdenRepository getInstance()
    {
        return this;
    }
}
