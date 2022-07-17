package fish.yukiemeralis.eden.module;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class ModuleConfig
{
    @Expose
    private Map<String, Object> data = new HashMap<>();

    public ModuleConfig() { }

    public ModuleConfig(Map<String, Object> data)
    {
        this.data = data;
    }

    public int getSize()
    {
        return this.data.size();
    }

    public boolean hasKey(String key)
    {
        return this.data.containsKey(key);
    }

    public <T> T getKey(String key, Class<T> clazz)
    {
        if (!data.containsKey(key))
            return null;
        if (!clazz.isAssignableFrom(data.get(key).getClass()))
            return null;
        
        return clazz.cast(data.get(key));
    }

    public Object getKey(String key)
    {
        return data.get(key);
    }

    public boolean getBoolean(String key)
    {
        return getKey(key, Boolean.class);
    }

    public String getString(String key)
    {
        return getKey(key, String.class);
    }

    public int getInt(String key)
    {
        return getKey(key, Integer.class);
    }

    public Object removeKey(String key)
    {
        return data.remove(key);
    }

    /**
     * Sets a value in a config.
     * @param key
     * @param value
     * @return
     */
    public Object setKey(String key, Object value)
    {
        return data.put(key, value);
    }

    /**
     * Sets a value in a config. Does not permit setting an initial value, and does not permit changing the type of an existing key.
     * @param <T> Type
     * @param key Key
     * @param value Value
     * @param clazz Class type
     * @return Old value
     */
    @SuppressWarnings("unchecked") // Cast is checked
    public <T> T setKeySafe(String key, T value, Class<T> clazz)
    {
        if (!data.containsKey(key))
            return null;
        if (!data.get(key).getClass().isAssignableFrom(clazz))
            return null;

        return (T) data.put(key, value);
    }
}
