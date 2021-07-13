package com.yukiemeralis.blogspot.zenith.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.yukiemeralis.blogspot.zenith.Zenith;

import org.bukkit.plugin.java.JavaPlugin;

public class DataUtils 
{
    public static boolean hasMethod(String methodName, Class<?> class_, Class<?>... classParams)
    {
        try {
            class_.getMethod(methodName, classParams);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static File getZenithJar()
    {
        JavaPlugin plugin = (JavaPlugin) Zenith.getInstance().getServer().getPluginManager().getPlugin("ZenithCore");

        // Reflection fun
        try {
            Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
            getFile.setAccessible(true);

            File file = (File) getFile.invoke(plugin);
            return file;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void generateStackTrace(String message)
    {
        try {
            throw new IntentionalStackTrace(message);
        } catch (IntentionalStackTrace e) {
            e.printStackTrace();
        }
    }

    private static class IntentionalStackTrace extends Exception
    {
		private static final long serialVersionUID = 2438785758452796394L;

		public IntentionalStackTrace(String message)
        {
            System.out.println(message);
        }
    }

    /**
     * Combines a variable number of collections of type T into one ArrayList.</p>
     * 
     * This ArrayList cannot be modified and is NOT backed by any of the collections given.
     * Please ensure you understand this principle before invoking this method.
     * 
     * @param <T> Type of all the given collections.
     * @param collections A variable argument list of collections of type T.
     * @return An unmodifiable list containing all the elements of all the collections given.
     */
    @SafeVarargs
    public static <T> List<T> concatMultipleCollections(Collection<T>... collections)
    {
        List<T> buffer = new ArrayList<>();

        for (Collection<T> coll : collections)
            buffer.addAll(coll);

        return Collections.unmodifiableList(buffer);
    }
}
