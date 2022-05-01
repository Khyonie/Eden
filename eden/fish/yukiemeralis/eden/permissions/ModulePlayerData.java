package fish.yukiemeralis.eden.permissions;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class ModulePlayerData 
{
    @Expose
    private String modName;
    @Expose
    private Map<String, Object> data = new HashMap<>();

    public ModulePlayerData(String modName)
    {
        this.modName = modName;
    }

    public ModulePlayerData(String modName, Map<String, Object> data)
    {
        this.modName = modName;
        this.data = data;
    }

    public String getModuleName()
    {
        return this.modName;
    }

    /**
     * Pulls a value stored in this dataset. Type is checked prior to retrieval, so only valid requests get valid values.
     * @param <T> Type
     * @param key Key the value is stored under.
     * @param type Type
     * @return The value stored under the given key.
     */
    @SuppressWarnings("unchecked") // Cast is checked
    public <T> T getValue(String key, Class<T> type) 
    {
        if (!data.containsKey(key))
            return null;
        if (!type.isAssignableFrom(data.get(key).getClass()))
            return null;
        return (T) data.get(key);
    }

    /**
     * Manual-cast version of {@link ModulePlayerData#getValue()}.<p>
     * Please ensure all values that you pull from this method are type-checked down the line.
     * @param key
     * @return
     */
    public Object getValue(String key)
    {
        return data.get(key);
    }

    public void setValue(String key, Object value)
    {
        this.data.put(key, value);
    }

    public boolean toggleValue(String key)
    {
        if (!(this.data.get(key) instanceof Boolean))
            return false;
        this.data.replace(key, !getValue(key, Boolean.class));
        return getValue(key, Boolean.class);
    }

    public Map<String, Object> getModuleData()
    {
        return this.data;
    }
}