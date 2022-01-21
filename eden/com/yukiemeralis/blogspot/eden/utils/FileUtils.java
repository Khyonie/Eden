package com.yukiemeralis.blogspot.eden.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

import com.google.common.io.Files;

/**
 * Utility class for files and folders.
 * @author Yuki_emeralis
 */
public class FileUtils 
{
	/**
	 * "Ensures" a folder by generating if the filepath doesn't exist.
	 * @param path A filepath
	 * @return The folder represented by the filepath
	 */
    public static File ensureFolder(String path)
    {
        File f = new File(path);
        if (!f.exists())
            f.mkdirs();

        return f;
    }

    /**
     * Streams an inputstream to a file in /mods/temp/
     * @param stream
     * @param filename
     * @return A file generated from a given input stream
     */
    public static File inputStreamToFile(InputStream stream, String filename)
    {
        ensureFolder("./plugins/Eden/mods/temp/");

        File file = new File("./plugins/Eden/mods/temp/" + filename);

        try {
            java.nio.file.Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            PrintUtils.printPrettyStacktrace(e);
        }

        return file;
    }

    /**
     * Obtains a count of how many similarly named files are in a folder.
     * @param path The folder filepath
     * @param name The name of a file
     * @return A count of how many similarly named files are in a folder
     */
    public static int getFileNameCount(String path, String name)
    {
        File f = new File(path);

        if (!f.exists())
            return 0;

        int count = 0;
        for (String str : f.list())
            if (str.startsWith(name))
                count++;

        return count;
    }

    /**
     * Copies a supposedly corrupt file to /Eden/lost-and-found for later analysis and retrieval.
     * @param file The supposedly corrupt file.
     * @return The copied file, now in lost-and-found.
     */
    public static File moveToLostAndFound(File file)
    {
        String newFileName = file.getName() + ".old" + getFileNameCount("./plugins/Eden/lost-and-found/", file.getName() + ".old");
        File newFile = new File("./plugins/Eden/lost-and-found/" + newFileName);

        try {
            Files.copy(file, newFile);
        } catch (IOException e) {
            PrintUtils.printPrettyStacktrace(e);
        }
        
        return newFile;
    }

    /**
     * Copies one file to another place.
     * @param from
     * @param to
     */
    public static void copy(File from, File to)
    {
        try {
            Files.copy(from, to);
        } catch (IOException e) {
            PrintUtils.printPrettyStacktrace(e);
        }
    }
}
