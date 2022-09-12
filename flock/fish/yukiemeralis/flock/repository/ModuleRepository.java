package fish.yukiemeralis.flock.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;

import com.google.gson.annotations.Expose;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.exception.TimeSpaceDistortionException;
import fish.yukiemeralis.eden.utils.logging.Logger.InfoType;
import fish.yukiemeralis.eden.utils.option.Option;
import fish.yukiemeralis.eden.utils.result.Result;
import fish.yukiemeralis.flock.DownloadUtils;
import fish.yukiemeralis.flock.Flock;
import fish.yukiemeralis.flock.enums.JsonDownloadStatus;
import fish.yukiemeralis.flock.gui.GlobalRepositoryGui;
import fish.yukiemeralis.flock.gui.RepositoryGui;
import fish.yukiemeralis.flock.gui.SnakeLoadingGui;
import net.md_5.bungee.api.ChatColor;

public class ModuleRepository implements GuiComponent
{
    @Expose
    private String 
        name,
        url;

    @Expose
    private double timestamp;

    @Expose
    private Map<String, ModuleRepositoryEntry> entries = new HashMap<>();

    public ModuleRepository(String name, String url)
    {
        this.name = ChatColor.stripColor(name);
        this.url = url;
        this.timestamp = System.currentTimeMillis();
    } 

    public Option sync()
    {
        return sync((Runnable) null);
    }

    public Option sync(Runnable toRun)
    {
        PrintUtils.logVerbose("Synchronizing " + this.name + " with upstream...", InfoType.INFO);
        Result result = DownloadUtils.downloadJson(this.url, ModuleRepository.class);

        switch (result.getState())
        {
            case OK:
                // Copy data over
                ModuleRepository remote = result.unwrapOk(ModuleRepository.class);
                this.entries = remote.getEntries();
                this.entries.values().forEach((entry) -> entry.attachHost(this)); // Attach everything to a this repo

                this.timestamp = remote.getTimestamp();
                this.name = remote.getName();

                PrintUtils.logVerbose("Synchronization for " + this.name + " completed, " + this.entries.size() + " entries are available.", InfoType.INFO);
                if (toRun != null) 
                    new Thread(toRun).start();

                return Option.none();
            case ERR:
                PrintUtils.logVerbose("Synchronization failed! Error: " + result.unwrapErr(JsonDownloadStatus.class).name(), InfoType.ERROR);
                if (toRun != null) 
                    new Thread(toRun).start();

                return Option.some(result.unwrapErr(JsonDownloadStatus.class));
            default: throw new TimeSpaceDistortionException();
        }
    }

    @Override
    public GuiItemStack generate()
    {
        boolean canUpdate = canUpdate();
        PrintUtils.logVerbose("Generating icon, can update? " + canUpdate, InfoType.INFO);
        return SimpleComponentBuilder.build(Material.CHEST, "§r§b§l" + this.name, (event) -> {
                if (event.getAction().equals(InventoryAction.PICKUP_ALL))
                {
                    // Left click, open
                    new RepositoryGui(this, event.getWhoClicked()).display(event.getWhoClicked());
                    return;
                }

                // Right click, sync
                event.getWhoClicked().closeInventory();
                new SnakeLoadingGui().display(event.getWhoClicked());

                Runnable finishRunnable = () -> {
                    PrintUtils.sendMessage(event.getWhoClicked(), "§aSynchronization finished!");
                    Bukkit.getScheduler().runTask(Eden.getInstance(), () -> new GlobalRepositoryGui(event.getWhoClicked()).display(event.getWhoClicked()));
                };

                sync(finishRunnable);
            },
            "§7§o" + this.entries.size() + " " + PrintUtils.plural(this.entries.size(), "module", "modules") + " available (" + this.getNumberInstalled() + " installed)",
            "",
            "§7Left-click to open repository.",
            canUpdate ? "§aRight-click to sync repository." : "§7This repository is up to date."
        );
    }

    /**
     * Checks for updates in a thread, and notifies a secondary thread when it is finished.
     * After {@link ModuleRepository#canUpdate()} is called, call {@link ModuleRepository#cleanupPrefetch()} to allow for another prefetch.
     * @param toNotify
     */
    public void prefetch(Runnable toNotify)
    {
        PrintUtils.logVerbose("Prefetching " + this.name + "...", InfoType.INFO);
        new Thread()
        {
            @Override
            public void run()
            {
                prefetchedCanUpdate = canUpdate();
                prefetched = true;
                PrintUtils.logVerbose("Prefetch for repo " + name + " finished, starting toNotify thread...", InfoType.INFO);
                new Thread(toNotify).start();
            }
        }.start();
    }

    private boolean prefetched = false;
    private boolean prefetchedCanUpdate = false;

    public synchronized void cleanupPrefetch()
    {
        prefetched = false;
        prefetchedCanUpdate = false;
        PrintUtils.logVerbose("Cleaned prefetch for " + this.name, InfoType.INFO);
    }

    public boolean canUpdate()
    {
        PrintUtils.logVerbose("Beginning non-commital sync for repository " + this.name + " at " + this.url + "...", InfoType.INFO);
        if (prefetched)
        {
            PrintUtils.logVerbose(this.name + " was prefetched! Returning prefetch...", InfoType.INFO);
            return prefetchedCanUpdate;
        }

        PrintUtils.logVerbose("Downloading repo for comparison...", InfoType.INFO);
        Result result = DownloadUtils.downloadJson(this.url, ModuleRepository.class);

        switch (result.getState())
        {
            case OK:
                PrintUtils.logVerbose("Download OK, comparing remote " + result.unwrapOk(ModuleRepository.class).getTimestamp() + " to local " + this.timestamp + ". Can update? " + (result.unwrapOk(ModuleRepository.class).getTimestamp() > this.timestamp), InfoType.INFO);
                return result.unwrapOk(ModuleRepository.class).getTimestamp() > this.timestamp;        
            default:
                PrintUtils.logVerbose("Download ERR of type " + result.unwrapErr(JsonDownloadStatus.class) + ", cannot update", InfoType.ERROR);
                break;
        }

        return false;
    }

    public List<ModuleRepositoryEntry> getEntryList()
    {
        return new ArrayList<>(this.entries.values());
    }

    public void sync(ModuleRepository data)
    {
        this.entries = data.getEntries();
    }

    public boolean contains(String name)
    {
        return this.entries.containsKey(name);
    }

    public ModuleRepositoryEntry get(String name)
    {
        return entries.get(name);
    }

    public int getNumberInstalled()
    {
        int count = 0;
        for (String s : this.entries.keySet())
            if (Eden.getModuleManager().isModulePresent(s))
                count++;

        return count;
    }

    public String getName()
    {
        return ChatColor.stripColor(this.name);
    }

    private Map<String, ModuleRepositoryEntry> getEntries()
    {
        return this.entries;
    }

    public void updateTimestamp(double timestamp)
    {
        this.timestamp = timestamp;
        Flock.updateRepoSyncTime(this);
    }

    public double getTimestamp()
    {
        return this.timestamp;
    }

    public void updateEntry(ModuleRepositoryEntry entry)
    {
        if (entry.getName() == null)
            throw new IllegalArgumentException("Entry name must not be null.");

        this.entries.put(entry.getName(), entry);
    }

    public void removeEntry(ModuleRepositoryEntry entry)
    {
        this.entries.remove(entry.getName());
    }

    /**
     * Forces all contained entries to contain a reference back to this repository.
     */
    public void updateReferences()
    {
        entries.values().forEach((entry) -> entry.attachHost(this));
    }

    public List<ModuleRepositoryEntry> getInvalidEntries()
    {
        List<ModuleRepositoryEntry> data = new ArrayList<>();

        for (ModuleRepositoryEntry entry : entries.values())
            if (!entry.isValid())
                data.add(entry);

        return data;
    }

    public void save()
    {
        JsonUtils.toJsonFile("./plugins/Eden/repositories/" + this.getName() + ".json", this);
    }
}
