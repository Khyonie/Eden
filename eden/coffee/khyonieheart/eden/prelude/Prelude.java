package coffee.khyonieheart.eden.prelude;

import org.bukkit.Material;

import coffee.khyonieheart.eden.listeners.PlayerIPListener;
import coffee.khyonieheart.eden.listeners.UuidCacheListener;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.EdenModule.ModInfo;
import coffee.khyonieheart.eden.module.annotation.HideFromCollector;
import coffee.khyonieheart.eden.module.annotation.ModuleFamily;
import coffee.khyonieheart.eden.module.annotation.PreventUnload;
import coffee.khyonieheart.eden.module.java.enums.CallerToken;
import coffee.khyonieheart.eden.utils.ChatUtils;

/**
 * Internal Eden module
 */
@HideFromCollector
@ModInfo(
    modName = "Prelude",
    description = "Module interface for Eden internals.",
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
        this.addCommand(new PreludeModuleCommand(this));
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