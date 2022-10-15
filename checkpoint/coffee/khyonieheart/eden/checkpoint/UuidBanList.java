package coffee.khyonieheart.eden.checkpoint;

import java.util.List;

import com.google.gson.annotations.Expose;

/** Container class so gson can stop abusing me */
public class UuidBanList 
{
    @Expose
    private List<UuidBanEntry> data;

    /**
     * Gson constructor
     */
    public UuidBanList() { }

    /**
     * A UUID ban list
     * @param data
     */
    public UuidBanList(List<UuidBanEntry> data)
    {
        this.data = data;
    }

    /** 
     * Returns the contained data
     * @return Contained List
     */
    public List<UuidBanEntry> getData()
    {
        return data;
    }
}
