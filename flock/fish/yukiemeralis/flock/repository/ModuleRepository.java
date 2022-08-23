package fish.yukiemeralis.flock.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import fish.yukiemeralis.eden.utils.option.Option;
import fish.yukiemeralis.eden.utils.result.Result;
import fish.yukiemeralis.flock.DownloadUtils;
import fish.yukiemeralis.flock.enums.JsonDownloadStatus;
import fish.yukiemeralis.flock.gui.RepositoryGui;
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
        Result result = DownloadUtils.downloadJson(this.url, ModuleRepository.class);

        switch (result.getState())
        {
            case OK:
                this.entries = result.unwrapOk(ModuleRepository.class).getEntries();
                return Option.none();
            case ERR:
                return Option.some(result.unwrapErr(JsonDownloadStatus.class));
            default: throw new TimeSpaceDistortionException();
        }
    }

    @Override
    public GuiItemStack generate()
    {
        return SimpleComponentBuilder.build(Material.CHEST, "§r§b§l" + this.name, (event) -> {
                if (event.getAction().equals(InventoryAction.PICKUP_ALL))
                {
                    // Left click, open
                    new RepositoryGui(this, event.getWhoClicked()).display(event.getWhoClicked());
                    return;
                }

                // Right click, sync
            },
            "§7§o" + this.entries.size() + " " + PrintUtils.plural(this.entries.size(), "module", "modules") + " available (" + this.getNumberInstalled() + " installed)",
            "",
            "§7Left-click to open repository.",
            "§7Right-click to sync repository."
        );
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
