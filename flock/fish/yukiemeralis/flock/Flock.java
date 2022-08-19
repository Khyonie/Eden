package fish.yukiemeralis.flock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.EdenModule.ModInfo;
import fish.yukiemeralis.eden.utils.option.Option;
import fish.yukiemeralis.flock.repository.ModuleRepository;
import fish.yukiemeralis.flock.repository.ModuleRepositoryEntry;

@ModInfo(
    modName = "Flock",
    description = "Handler for module repositories.",
    maintainer = "Yuki_emeralis",
    modIcon = Material.END_PORTAL,
    version = "2.0",
    supportedApiVersions = { "v1_19_R1" }
)
public class Flock extends EdenModule
{
    private static final Map<String, ModuleRepository> REPOSITORIES = new HashMap<>();

    @Override
    public void onEnable() 
    {
        
    }

    @Override
    public void onDisable() 
    {
        
    }

    /**
     * Searches for any module entries matching a given name. This may return several entries.
     * @param name
     * @return
     */
    public static List<ModuleRepositoryEntry> getModule(String name)
    {
        List<ModuleRepositoryEntry> entries = new ArrayList<>();


        return entries;
    }

    public static Option getRepository(String name)
    {
        return REPOSITORIES.containsKey(name) ? Option.some(REPOSITORIES.get(name)) : Option.none();
    }

    public static Option addRepository(ModuleRepository repository)
    {
        return null;
    }
}