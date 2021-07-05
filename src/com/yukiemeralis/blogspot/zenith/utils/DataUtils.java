package com.yukiemeralis.blogspot.zenith.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
}
