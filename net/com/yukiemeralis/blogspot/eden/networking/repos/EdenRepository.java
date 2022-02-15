package com.yukiemeralis.blogspot.eden.networking.repos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.networking.NetworkingModule;
import com.yukiemeralis.blogspot.eden.networking.NetworkingUtils;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

                PrintUtils.sendMessage(feedbackViewer, "§aSuccessfully downloaded and synced repository \"§b" + repo.getName() + "§a\"!");
            }
        });
    }

    private GuiItemStack item;

    @Override
    public GuiItemStack toIcon() 
    {
        if (item != null)
            return item;
        item = new GuiItemStack(ItemUtils.build(Material.BOOKSHELF, "§r§9§l" + name, "§r§7§o" + entries.size() + " " + PrintUtils.plural(entries.size(), "entry", "entries"))) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                new RepositoryGui((Player) event.getWhoClicked(), getInstance()).display(event.getWhoClicked());;
            }
        };

        return item;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event) 
    {
        if (item != null)
        {
            item.onIconInteract(event);
            return;
        }

        toIcon().onIconInteract(event);
    }

    private EdenRepository getInstance()
    {
        return this;
    }
}
