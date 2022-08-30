package fish.yukiemeralis.flock.repository;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;

import com.google.gson.annotations.Expose;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.surface2.GuiUtils;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.flock.DownloadFinishedThread;
import fish.yukiemeralis.flock.DownloadUtils;
import fish.yukiemeralis.flock.Flock;
import fish.yukiemeralis.flock.TextUtils;
import fish.yukiemeralis.flock.gui.EditRepositoryEntryGui;
import net.md_5.bungee.api.ChatColor;

public class ModuleRepositoryEntry implements GuiComponent
{
    @Expose
    private String 
        name,
        description,
        url,
        version,
        author;

    @Expose
    private List<String> dependencies; // Dependency list, with each entry formatted as "repository:module"

    @Expose
    private double timestamp;

    private ModuleRepository host;

    @Override
    public GuiItemStack generate()
    {
        if (this.dependencies == null) 
            this.dependencies = new ArrayList<>();

        return SimpleComponentBuilder.build(Material.BOOK, "§9§l" + name, (event) -> {
            switch (event.getAction())
            {
                case PICKUP_ALL -> { // Update
                    if (!this.isInstalled())
                    { 
                        // Fresh install
                        try {
                            DownloadUtils.downloadFile(this.url, "./plugins/Eden/mods/" + this.name + ".jar", new DownloadFinishedThread() {
                                @Override
                                public void run() 
                                {
                                    if (this.failed())
                                    {
                                        PrintUtils.sendMessage(event.getWhoClicked(), "§cDownload failed. Reason: " + this.getFailureException().getClass().getSimpleName() + ":" + this.getFailureException().getMessage());
                                        return;
                                    }    

                                    PrintUtils.sendMessage(event.getWhoClicked(), "§aDownload complete.");
                                }
                            });
                        } catch (MalformedURLException e) {
                            PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module's URL is corrupt! Cannot update.");
                        }
                        return;
                    }

                    // Update
                    if (!this.canUpdate())
                    {
                        PrintUtils.sendMessage(event.getWhoClicked(), "This module is up to date.");
                        return;
                    }

                    boolean enabled = Eden.getModuleManager().getModuleByName(this.name).getIsEnabled();
                    try {
                        DownloadUtils.downloadFile(this.url, "./plugins/Eden/mods/" + this.name + ".jar", new DownloadFinishedThread() {
                            @Override
                            public void run() 
                            {
                                if (this.failed())
                                {
                                    PrintUtils.sendMessage(event.getWhoClicked(), "§cDownload failed. Reason: " + this.getFailureException().getClass().getSimpleName() + ":" + this.getFailureException().getMessage());
                                    return;
                                }    

                                PrintUtils.sendMessage(event.getWhoClicked(), "§aDownload complete.");
                                if (!Eden.getModuleManager().forceReload(name, enabled, false))
                                {
                                    PrintUtils.sendMessage(event.getWhoClicked(), "Failed to automatically reload " + getName() + ". See console for details.");
                                    return;
                                }

                                if (Eden.getModuleManager().getModuleByName(getName()).getIsEnabled())
                                    PrintUtils.sendMessage(event.getWhoClicked(), "Successfully enabled " + getName() + "!");
                            }
                        });
                    } catch (MalformedURLException e) {
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module's URL is corrupt! Cannot update.");
                    }
                }
                case PICKUP_HALF -> { // Edit
                    new EditRepositoryEntryGui(this, event.getWhoClicked()).display(event.getWhoClicked());
                }
                case MOVE_TO_OTHER_INVENTORY -> { // Remove
                    this.host.removeEntry(this);
                    SurfaceGui.getOpenGui(event.getWhoClicked()).unwrap(SurfaceGui.class).updateSingleItem(event.getWhoClicked(), event.getSlot(), GuiUtils.BLACK_PANE, false);
                }
                default -> { return; }
            }
        },
            "§7§o" + TextUtils.pruneStringLength(this.url == null ? "§cNo URL set ⚠" : this.url, "...", 35),
            "§7§o" + TextUtils.pruneStringLength(this.description == null ? "No description set" : this.description, "...", 35),
            "§7§o" + TextUtils.pruneStringLength(this.author == null ? "No author set" : this.author, "...", 35),
            "§7§o" + TextUtils.pruneStringLength(this.version == null ? "No version set" : this.version, "...", 35),
            "§7§o" + dependencies.size() + PrintUtils.plural(dependencies.size(), " dependency", " dependencies"),
            "",
            "§7" + (this.isInstalled() ? (this.canUpdate() ? "Left-click to update module." : "This module is up to date.") : "Left-click to install module."),
            "§7Right-click to edit entry.",
            "§7Shift + Left-click to remove.",
            "§7entry"
        );
    }

    /**
     * Sets this entry's name.
     * @param name New name to be set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Sets this entry's description.
     * @param description New description to be set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Sets this entry's URL.
     * @param url New URL to be set
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * Sets this entry's version.
     * @param version
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public void updateTimestamp()
    {
        this.timestamp = System.currentTimeMillis();
        Flock.updateEntrySyncTime(this);
    }

    public String getName()
    {
        return this.name;
    }

    public String getDescription()
    {
        return ChatColor.stripColor(this.description);
    }

    public String getUrl()
    {
        return this.url;
    }

    public String getVersion()
    {
        return this.version;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public double getTimestamp()
    {
        return this.timestamp;
    }

    public ModuleRepository getHostRepository()
    {
        return this.host;
    }

    public void addDependency(String dependency)
    {
        if (this.dependencies == null)
            this.dependencies = new ArrayList<>();
        this.dependencies.add(dependency);
    }

    public List<String> getDependencies()
    {
        if (this.dependencies == null)
            this.dependencies = new ArrayList<>();
        return Collections.unmodifiableList(dependencies);
    }

    public String removeBottomDependency()
    {
        if (this.dependencies == null)
            this.dependencies = new ArrayList<>();

        if (dependencies.size() == 0)
            return null;
        return this.dependencies.remove(this.dependencies.size() - 1);
    }

    public void attachHost(ModuleRepository repo)
    {
        this.host = repo;
    }

    public void setProperty(RepositoryEntryProperty property, String value)
    {
        switch (property)
        {
            case AUTHOR -> setAuthor(value);
            case DESCRIPTION -> setDescription(value);
            case NAME -> setName(value);
            case URL -> setUrl(value);
            case VERSION -> setVersion(value);
        }
    }

    public boolean isInstalled()
    {
        return Eden.getModuleManager().isModulePresent(this.getName());
    }

    /**
     * Gets whether or not this entry's timestamp is greater than the last known sync timestamp.
     * An entry with a timestamp greater than the last sync time (if any) is assumed to be able to update.
     * @return Whether or not the associated module can update
     */
    public boolean canUpdate()
    {
        if (!Flock.hasEntrySyncTime(this))
            return true;

        return this.timestamp > Flock.getEntrySyncTime(this);
    }

    /**
     * Verifies a repository's validity. A valid repository must not contain any null fields.
     * @return Whether or not this repository is valid.
     */
    public boolean isValid()
    {
        for (Field f : this.getClass().getDeclaredFields())
        {
            f.setAccessible(true);
            try {
                if (f.get(this) == null)
                    return false;
            } catch (IllegalAccessException e) {
                continue;
            } finally {
                f.setAccessible(false);   
            }
        }

        return true;
    }

    public static enum RepositoryEntryProperty
    {
        NAME,
        DESCRIPTION,
        AUTHOR,
        VERSION,
        URL
    }
}