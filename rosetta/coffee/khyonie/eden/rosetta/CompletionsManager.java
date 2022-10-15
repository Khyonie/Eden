package coffee.khyonie.eden.rosetta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.utils.DataUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.logging.Logger.InfoType;
import coffee.khyonieheart.eden.utils.option.Option;

@SuppressWarnings("unused") // "Unused" methods are instead used reflectively
public class CompletionsManager 
{
    private static CompletionsManager instance = new CompletionsManager(); 

    private static Map<String, ObjectMethodPair> completionLists = new HashMap<>() 
    {{
        try {
            put("BOOLEAN", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("booleans")));
            put("ONLINE_PLAYERS", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("onlinePlayers")));
            put("ALL_PLAYERS", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("allPlayers")));
            put("ALL_MODULES", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("allModules")));
            put("ENABLED_MODULES", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("enabledModules")));
            put("DISABLED_MODULES", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("disabledModules")));
        } catch (NoSuchMethodException e) {
            PrintUtils.log("<Failed to generate completions.>", InfoType.ERROR);
            PrintUtils.printPrettyStacktrace(e);
        }
    }};

    public static boolean hasCompletion(String key)
    {
        return completionLists.containsKey(key);
    }

    public static Option registerCompletion(String label, ObjectMethodPair data, boolean overwrite)
    {
        if (completionLists.containsKey(label) && !overwrite)
            return Option.some("Completion with this name already exists.");

        completionLists.put(label, data);
        return Option.none();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getCompletions(String key)
    {
        if (!completionLists.containsKey(key))
            return new ArrayList<>();

        try {
            return (List<String>) completionLists.get(key).invoke();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
            PrintUtils.printPrettyStacktrace(e);
            return new ArrayList<>() {{ add("An error has occurred."); }};
        }
    }

    // Base completions

    private static final List<String> booleans = new ArrayList<>(), onlinePlayers = new ArrayList<>();

    static {
        booleans.add("true");
        booleans.add("false");
    }

    private List<String> booleans()
    {
        return booleans;
    }

    private List<String> onlinePlayers()
    {
        List<String> buffer = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) 
            buffer.add(p.getName());

        return buffer;
    }

    private List<String> allPlayers()
    {
        List<String> buffer = new ArrayList<>();

        buffer.addAll(Eden.getUuidCache().keySet());
        for (Player p : Bukkit.getOnlinePlayers())
            if (!buffer.contains(p.getName()))
                buffer.add(p.getName());

        return buffer;
    }

    private List<String> materials()
    {
        List<String> buffer = DataUtils.mapList(Arrays.asList(Material.values()), (in) -> in.name());

        return buffer;
    }

    private List<String> allModules()
    {
        List<String> buffer = new ArrayList<>();

        for (EdenModule mod : Eden.getModuleManager().getAllModules())
            buffer.add(mod.getName());

        return buffer;
    }

    private List<String> enabledModules()
    {
        List<String> buffer = new ArrayList<>();

        for (EdenModule mod : Eden.getModuleManager().getEnabledModules())
            buffer.add(mod.getName());

        return buffer;
    }

    private List<String> disabledModules()
    {
        List<String> buffer = new ArrayList<>();

        for (EdenModule mod : Eden.getModuleManager().getDisabledModules())
            buffer.add(mod.getName());

        return buffer;
    }

    /**
     * A data object containing an object and a method belonging to that object.
     */
    public static class ObjectMethodPair
    {
        private final Object obj;
        private final Method method;

        public ObjectMethodPair(Object obj, Method method)
        {
            this.method = method;
            this.obj = obj;
        }

        /**
         * Convenience constructor that obtains a class method by name.
         * @param obj
         * @param methodName
         * @throws NoSuchMethodException
         */
        public ObjectMethodPair(Object obj, String methodName) throws NoSuchMethodException
        {
            this(obj, obj.getClass().getDeclaredMethod(methodName));
        }

        public Object invoke(Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            method.setAccessible(true);
            return method.invoke(obj, args);
        }
    }
}
