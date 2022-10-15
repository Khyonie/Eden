package fish.yukiemeralis.eden.skins.result;

import java.util.Map;

import com.google.gson.annotations.Expose;

public class UserProfileResult 
{
    @Expose
    String id, name;

    @Expose
    Map<String, String> properties;

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
