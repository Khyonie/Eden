package fish.yukiemeralis.eden.precipice;

import org.bukkit.Material;

import fish.yukiemeralis.eden.listeners.PlayerIPListener;
import fish.yukiemeralis.eden.listeners.UuidCacheListener;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.EdenModule.ModInfo;
import fish.yukiemeralis.eden.module.annotation.HideFromCollector;
import fish.yukiemeralis.eden.module.annotation.ModuleFamily;
import fish.yukiemeralis.eden.module.annotation.PreventUnload;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.utils.ChatUtils;

/**
 * Internal Eden module
 */
@HideFromCollector
@ModInfo(
    modName = "Prelude",
    description = "Eden's internal interface for various tasks.",
    maintainer = "Yuki_emeralis",
    modIcon = Material.WHITE_CONCRETE,
    version = "1.0.0",
    supportedApiVersions = { "v1_19_R1" }
)
@ModuleFamily(name = "Eden", icon = Material.BLUE_ICE)
@PreventUnload(CallerToken.EDEN)
public class Prelude extends EdenModule 
{
    /**
     * Module constructor.
     */
    public Prelude()
    {
        this.setInfo(this.getClass().getAnnotation(ModInfo.class));
        this.addListener(new UuidCacheListener(), new PlayerIPListener(), new ChatUtils());
        this.addCommand(new PrecipiceModuleCommand(this));
    }

    @Override
    public void onEnable() 
    {
        
    }

    @Override
    public void onDisable() 
    {
        
    }
} 