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

package fish.yukiemeralis.eden.core;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.listeners.PlayerIPListener;
import fish.yukiemeralis.eden.listeners.UuidCacheListener;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.EdenModule.LoadBefore;
import fish.yukiemeralis.eden.module.EdenModule.ModInfo;
import fish.yukiemeralis.eden.module.annotation.ModuleFamily;
import fish.yukiemeralis.eden.module.EdenModule.EdenConfig;
import fish.yukiemeralis.eden.module.java.annotations.DefaultConfig;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;
import fish.yukiemeralis.eden.utils.FileUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;

@ModInfo(
    modName = "Rosetta", 
    description = "Eden's core module.",
    maintainer = "Yuki_emeralis",
    version = "1.6.5",
    modIcon = Material.ENDER_EYE,
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2", "v1_19_R1"}
)
@ModuleFamily(name = "Eden core modules", icon = Material.ENDER_EYE)
@LoadBefore(loadBefore = {"Checkpoint", "Surface2"})
@EdenConfig
@DefaultConfig(
    keys = {
        "loginGreeting", "warnIfNotRelease", "prettyLoginMessage", "eColor"
    },
    values = {
        "true", "true", "false", "47ff9a"
    }
)
@PreventUnload(CallerToken.EDEN)
public class CoreModule extends EdenModule
{
    static List<DisableRequest> EDEN_DISABLE_REQUESTS = new ArrayList<>();
    static List<String> EDEN_WARN_DISABLE_REQUESTS = new ArrayList<>();

    public CoreModule()
    {
        addListener(new UuidCacheListener());
        addListener(new PlayerIPListener());
    }

    @Override
    public void onEnable() 
    {
        //this.version = VersionCtrl.getVersion();

        FileUtils.ensureFolder("./plugins/Eden/playerdata");

        if (Boolean.parseBoolean(this.config.get("verboseLogging")))
            PrintUtils.enableVerboseLogging();

        // ModuleFamily.registerFamily("Eden base modules", Material.ENDER_EYE, "Required core modules.");
        // ModuleFamily.registerFamily("Eden extra modules", Material.ENDER_PEARL, "Additional offical modules.");
        // ModuleTracker.update();
    }

    @Override
    public void onDisable() 
    {
        
    }

    public static EdenModule getModuleInstance()
    {
        return Eden.getModuleManager().getModuleByName("Rosetta");
    }

    static class DisableRequest
    {
        private EdenModule module;
        private int type; // 0 = disable, 1 = unload

        public DisableRequest(EdenModule module, int type)
        {
            this.module = module;
            this.type = type;
        }

        public EdenModule getModule()
        {
            return this.module;
        }

        public int getType()
        {
            return this.type;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
                return false;
            if (!(obj instanceof DisableRequest))
                return false;
            if (this.module.equals(((DisableRequest) obj).getModule()) && this.type == ((DisableRequest) obj).getType())
                return true;
            return false;
        }
    }
}
