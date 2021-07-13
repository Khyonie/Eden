package com.yukiemeralis.blogspot.zenith.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

import com.google.common.io.Files;

public class FileUtils 
{
    public static File ensureFolder(String path)
    {
        File f = new File(path);
        if (!f.exists())
            f.mkdirs();

        return f;
    }

    public static File inputStreamToFile(InputStream stream, String filename)
    {
        ensureFolder("./plugins/Zenith/mods/temp/");

        File file = new File("./plugins/Zenith/mods/temp/" + filename);

        try {
            java.nio.file.Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

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

    public static File moveToLostAndFound(File file)
    {
        String newFileName = file.getName() + ".old" + getFileNameCount("./plugins/Zenith/lost-and-found/", file.getName() + ".old");
        File newFile = new File("./plugins/Zenith/lost-and-found/" + newFileName);

        try {
            Files.copy(file, newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return newFile;
    }

    public static void copy(File from, File to)
    {
        try {
            Files.copy(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
