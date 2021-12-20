/*
Copyright 2021 Yuki_emeralis https://yukiemeralis.blogspot.com/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.yukiemeralis.blogspot.zenith.core;

import org.bukkit.Material;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.core.modgui.ModuleTracker;
import com.yukiemeralis.blogspot.zenith.listeners.UuidCacheListener;
import com.yukiemeralis.blogspot.zenith.module.ModuleFamily;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.LoadBefore;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ModInfo;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ZenConfig;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.DefaultConfig;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.zenith.utils.FileUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

@ModInfo(
    modName = "Zenith", 
    description = "Zenith's core module.",
    modFamily = "Zenith base modules",
    maintainer = "Yuki_emeralis",
    version = "72821-2.2.0",
    modIcon = Material.ENDER_EYE,
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1"}
)
@LoadBefore(loadBefore = {"ZenithAuth", "ZenithGui"})
@ZenConfig
@DefaultConfig(
    keys = {
        "loginGreeting", "warnIfNotRelease", "prettyLoginMessage"
    },
    values = {
        "true", "true", "false"
    }
)
@PreventUnload(CallerToken.ZENITH)
public class CoreModule extends ZenithModule
{
    public CoreModule()
    {
        addListener(new UuidCacheListener());
    }

    @Override
    public void onEnable() 
    {
        this.version = VersionCtrl.getVersion();

        FileUtils.ensureFolder("./plugins/Zenith/playerdata");

        if (Boolean.parseBoolean(this.config.get("verboseLogging")))
            PrintUtils.enableVerboseLogging();

        ModuleFamily.registerFamily("Zenith base modules", Material.ENDER_EYE, "Required core modules.");
        ModuleFamily.registerFamily("Zenith extra modules", Material.ENDER_PEARL, "Additional offical modules.");
        ModuleTracker.update();
    }

    @Override
    public void onDisable() 
    {
        
    }

    public static ZenithModule getModuleInstance()
    {
        return Zenith.getModuleManager().getModuleByName("Zenith");
    }
}
