package fish.yukiemeralis.flock;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.EdenModule.LoadBefore;
import fish.yukiemeralis.eden.module.EdenModule.ModInfo;
import fish.yukiemeralis.eden.module.annotation.ModuleFamily;
import fish.yukiemeralis.eden.utils.FileUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.option.Option;
import fish.yukiemeralis.flock.enums.AddRepositoryStatus;
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
@ModuleFamily(name = "Eden core modules", icon = Material.ENDER_EYE)
@LoadBefore(loadBefore = { "Surface" })
public class Flock extends EdenModule
{
    private static final Map<String, ModuleRepository> REPOSITORIES = new HashMap<>();
    private static Map<String, Double> REPOSITORY_SYNC_TIMES = new HashMap<>(); 

    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() 
    {
        File repoFolder = new File("./plugins/Eden/repositories/");
        File repoSyncFile = new File("./plugins/Eden/repositories/.syncdata");

        FileUtils.ensureFolder(repoFolder.getAbsolutePath());

        for (File f : repoFolder.listFiles())
        {
            if (!f.getName().endsWith(".json"))
                continue;

            ModuleRepository repo = JsonUtils.fromJsonFile(f.getAbsolutePath(), ModuleRepository.class);

            if (repo == null)
            {
                PrintUtils.log("<Repository file \"" + f.getName() + "\" is corrupt! Moving to lost and found...>");
                FileUtils.moveToLostAndFound(f);
                continue;
            }

            if (REPOSITORIES.containsKey(repo.getName()))
            {
                PrintUtils.log("<Name conflict between two repositories both named \"" + repo.getName() + "\"! Ignoring second repository...>");
                continue;
            }

            repo.updateReferences(); // Assign all entry references
            REPOSITORIES.put(repo.getName(), repo);
        }

        if (!repoSyncFile.exists())
            JsonUtils.toJsonFile(repoSyncFile.getAbsolutePath(), REPOSITORY_SYNC_TIMES);

        REPOSITORY_SYNC_TIMES = JsonUtils.fromJsonFile(repoSyncFile.getAbsolutePath(), Map.class);
    }

    @Override
    public void onDisable() 
    {
        REPOSITORIES.forEach((name, repo) -> {
            repo.save();
        });
    }

    /**
     * Searches for any module entries matching a given name. This may return several entries.
     * @param name
     * @return
     */
    public static List<ModuleRepositoryEntry> getModule(String name)
    {
        List<ModuleRepositoryEntry> entries = new ArrayList<>();

        for (ModuleRepository repo : REPOSITORIES.values())
            if (repo.contains(name))
                entries.add(repo.get(name));

        return entries;
    }

    public static List<ModuleRepository> getRepositories()
    {
        return new ArrayList<>(REPOSITORIES.values());
    }

    public static Option getRepository(String name)
    {
        return hasRepository(name) ? Option.some(REPOSITORIES.get(name)) : Option.none();
    }

    public static boolean hasRepository(String name)
    {
        return REPOSITORIES.containsKey(name);
    }

    public static void updateRepoSyncTime(ModuleRepositoryEntry entry)
    {
        
    }

    public static Option addRepository(ModuleRepository repository)
    {
        if (repository == null)
            return Option.some(AddRepositoryStatus.NULL_REPOSITORY);
        if (REPOSITORIES.containsKey(repository.getName()))
            return Option.some(AddRepositoryStatus.NAME_CONFLICT);

        REPOSITORIES.put(repository.getName(), repository);
        
        return Option.none();
    }
}