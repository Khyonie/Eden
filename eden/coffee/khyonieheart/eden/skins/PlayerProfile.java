package coffee.khyonieheart.eden.skins;

import java.util.Map;

import com.google.gson.annotations.Expose;

public class PlayerProfile 
{
    @Expose
    private String id, name;

    @Expose
    private Map<String, String> properties; 

    public String getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public Map<String, String> getProperties()
    {
        return this.properties;
    }
}
