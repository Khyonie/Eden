package com.yukiemeralis.blogspot.zenith.core;

import java.io.File;

import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

public class VersionCtrl 
{
    private static String version = null;

    public static String getVersion()
    {
        if (version == null)
        {
            for (String filename : new File("./plugins/").list())
            {
                if (filename.startsWith("Zenith-"))
                {
                    PrintUtils.logVerbose("Found ZenithCore plugin, pulling the version from that...", InfoType.INFO);
                    version = "v" + filename.replaceAll("Zenith-|.jar", "");
                    return version;
                }
            }

            PrintUtils.log("(Failed to locate the Zenith plugin file! Version has been set to \"unknown\". Please ensure that the plugin starts with \"Zenith-\".)", InfoType.WARN);
            version = "Unknown";
        }

        return version;
    }
}
