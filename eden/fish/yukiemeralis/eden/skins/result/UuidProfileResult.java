package fish.yukiemeralis.eden.skins.result;

import com.google.gson.annotations.Expose;

public class UuidProfileResult 
{
    @Expose
    String name, id;    

    public String getName()
    {
        return this.name;
    }

    public String getId()
    {
        return this.id;
    }
}
