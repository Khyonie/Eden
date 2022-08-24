package fish.yukiemeralis.flock.repository;

import java.lang.reflect.Field;

import org.bukkit.Material;

import com.google.gson.annotations.Expose;

import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.flock.gui.EditRepositoryEntryGui;

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
    private String[] dependencies; // Dependency list, with each entry formatted as "repository:module"

    @Expose
    private double timestamp;

    private ModuleRepository host;

    @Override
    public GuiItemStack generate()
    {
        return SimpleComponentBuilder.build(Material.BOOK, "§9§l" + name, (event) -> new EditRepositoryEntryGui(this, event.getWhoClicked()).display(event.getWhoClicked()));
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
        return this.description;
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

    public void attachHost(ModuleRepository repo)
    {
        this.host = repo;
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
}