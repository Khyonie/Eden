package fish.yukiemeralis.eden.auth;

import java.util.List;

import com.google.gson.annotations.Expose;

/** Container class so gson can stop abusing me */
public class UuidBanList 
{
    @Expose
    private List<UuidBanEntry> data;

    public UuidBanList() { }

    public UuidBanList(List<UuidBanEntry> data)
    {
        this.data = data;
    }

    public List<UuidBanEntry> getData()
    {
        return data;
    }
}
