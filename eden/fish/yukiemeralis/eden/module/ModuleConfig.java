package fish.yukiemeralis.eden.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.Expose;

import fish.yukiemeralis.eden.utils.option.Option;

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

    public Option getKey(String key, Class<?> clazz)
    {
        if (!data.containsKey(key))
            return Option.none();
        if (!clazz.isAssignableFrom(data.get(key).getClass()))
            return Option.none();
        
        return Option.some(clazz.cast(data.get(key)));
    }

    public <T extends Enum<T>> Enum<T> getEnum(String key, Class<T> clazz)
    {
        // TODO Java 17 preview feature
        Option opt = getString(key);
        return switch (opt.getState())
        {
            case SOME -> Enum.valueOf(clazz, opt.unwrap(String.class));
            default -> null;
        };
    }

    public Object getKey(String key)
    {
        return data.get(key);
    }

    /**
     * Safely obtains a boolean key. Returns false if the key does not exist, or if the value to the supplied key is not a boolean.
     * @param key Key to boolean value.
     * @return <code>true</code> if and only if this module's configuration contains the given key, the value is a boolean, and the value resolves to <code>true</code>.
     */
    public Boolean getBoolean(String key)
    {
        // TODO Java 17 preview feature
        Option opt = getKey(key, Boolean.class);
        Boolean val = switch (opt.getState())
        {
            case SOME -> opt.unwrap(Boolean.class);
            default -> false;
        };

        return val;
    }

    public Option getString(String key)
    {
        return getKey(key, String.class);
    }

    public Option getInt(String key)
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

    public Set<String> getKeys()
    {
        return data.keySet();   
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
