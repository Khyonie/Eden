package fish.yukiemeralis.flock.repository;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

import fish.yukiemeralis.eden.utils.exception.TimeSpaceDistortionException;
import fish.yukiemeralis.eden.utils.option.Option;
import fish.yukiemeralis.eden.utils.result.Result;
import fish.yukiemeralis.flock.DownloadUtils;
import fish.yukiemeralis.flock.enums.JsonDownloadStatus;

public class ModuleRepository 
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
        this.name = name;
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

    public void sync(ModuleRepository data)
    {
        this.entries = data.getEntries();
    }

    public boolean contains(String name)
    {
        return this.entries.containsKey(name);
    }

    public String getName()
    {
        return this.name;
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
}
