package com.yukiemeralis.blogspot.zenith.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Collection of utilities to manage .json files.
 * @author Hailey
 *
 */
public class JsonUtils 
{
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;
    private static Gson uglygson;

    static 
    {
        uglygson = gsonBuilder.disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();

        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    }

    /**
     * @param path
     * @deprecated Use {@link FileUtils#ensureFolder(String)}
     */
    @Deprecated
    public static void initDir(String path)
    {
        if (!(new File(path).exists()))
        {
            new File(path).mkdirs();
        }
    }

    /**
     * Obtains the current gson instance.
     * @return A gson instance with pretty printing.
     */
    public static Gson getGson()
    {
        return gson;
    }

    /**
     * Obtains the current gson instance without pretty printing.
     * @return A gson instance.
     */
    public static Gson getUglyGson()
    {
        return uglygson;
    }

    /**
     * Reads an object of type T from a file.
     * @param <T> The type of the object to expect
     * @param path Filepath
     * @param type The type of the object to expect
     * @return An object from a .json file
     */
    public static <T> Object fromJsonFile(String path, Class<T> type)
    {
        File file = new File(path);

        try {
            FileReader f = new FileReader(file);
            T obj = gson.fromJson(f, type);

            f.close();

            return obj;
        } catch (IOException error) {
            return null;
        }
    }

    /**
     * Exports an object to a .json file.
     * @param path Filepath
     * @param obj The object to export
     */
    public static void toJsonFile(String path, Object obj)
    {
        File file = new File(path);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            writer.write(gson.toJson(obj));
            writer.flush();

            writer.close();
        } catch (IOException e) {}
    }
}
