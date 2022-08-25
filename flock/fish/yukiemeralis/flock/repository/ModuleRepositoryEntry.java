package fish.yukiemeralis.flock.repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;

import com.google.gson.annotations.Expose;

import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.utils.PrintUtils;
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
            if (event.getAction().equals(InventoryAction.PICKUP_HALF))
            {
                new EditRepositoryEntryGui(this, event.getWhoClicked()).display(event.getWhoClicked());
            }
        },
            "§7§o" + TextUtils.pruneStringLength(this.description, "...", 35),
            "§7§o" + TextUtils.pruneStringLength(this.url, "...", 35),
            "§7§o" + TextUtils.pruneStringLength(this.author, "...", 35),
            "§7§o" + TextUtils.pruneStringLength(this.version, "...", 35),
            "§7§o" + dependencies.size() + PrintUtils.plural(dependencies.size(), " dependency", " dependencies"),
            "",
            "§7Left-click to sync repository.",
            "§7Right-click to edit entry."
        );
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

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