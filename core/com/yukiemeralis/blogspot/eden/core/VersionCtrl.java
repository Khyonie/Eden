package com.yukiemeralis.blogspot.eden.core;

import java.io.File;

import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

public class VersionCtrl 
{
    private static String version = null;

    public static String getVersion()
    {
        if (version == null)
        {
            for (String filename : new File("./plugins/").list())
            {
                if (filename.startsWith("Eden-"))
                {
                    PrintUtils.logVerbose("Found Eden plugin, pulling the version from that...", InfoType.INFO);
                    version = "v" + filename.replaceAll("Eden-|.jar", "");
                    return version;
                }
            }

            PrintUtils.log("(Failed to locate the Eden plugin file! Version has been set to \"unknown\". Please ensure that the plugin starts with \"Eden-\".)", InfoType.WARN);
            version = "Unknown";
        }

        return version;
    }
}
