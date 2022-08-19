package fish.yukiemeralis.flock.repository;

import com.google.gson.annotations.Expose;

public class ModuleRepositoryEntry 
{
    @Expose
    private String 
        name,
        description,
        url,
        version,
        author;

    @Expose
    private double timestamp;

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
}