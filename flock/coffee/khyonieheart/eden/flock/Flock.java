package coffee.khyonieheart.eden.flock;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import coffee.khyonieheart.eden.flock.enums.AddRepositoryStatus;
import coffee.khyonieheart.eden.flock.repository.ModuleRepository;
import coffee.khyonieheart.eden.flock.repository.ModuleRepositoryEntry;
import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.EdenModule.LoadBefore;
import coffee.khyonieheart.eden.module.EdenModule.ModInfo;
import coffee.khyonieheart.eden.module.annotation.ModuleFamily;
import coffee.khyonieheart.eden.utils.FileUtils;
import coffee.khyonieheart.eden.utils.JsonUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.logging.Logger.InfoType;
import coffee.khyonieheart.eden.utils.option.Option;

@ModInfo(
    modName = "Flock",
    description = "Handler for module repositories.",
    maintainer = "Yuki_emeralis",
    modIcon = Material.END_PORTAL_FRAME,
    version = "2.0.0",
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

        PrintUtils.log("", InfoType.WARN);
        PrintUtils.log("Developer note: There is a known issue where updating a module via a Flock repository will cause a LinkageError to be thrown.", InfoType.WARN);
        PrintUtils.log("The cause is currently unknown, and is tracked here:", InfoType.WARN);
        PrintUtils.log("https://github.com/YukiEmeralis/Eden/issues/24", InfoType.WARN);
        PrintUtils.log("", InfoType.WARN);

        // TODO Check for eden updates
    }

    @Override
    public void onDisable() 
    {
        REPOSITORIES.forEach((name, repo) -> {
            repo.save();
        });

        JsonUtils.toJsonFile("./plugins/Eden/repositories/.syncdata", REPOSITORY_SYNC_TIMES);
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

    public static void updateEntrySyncTime(ModuleRepositoryEntry entry)
    {
        REPOSITORY_SYNC_TIMES.put(entry.getHostRepository().getName() + ":" + entry.getName(), entry.getTimestamp());
    }

    public static void updateRepoSyncTime(ModuleRepository repo)
    {
        REPOSITORY_SYNC_TIMES.put(repo.getName(), repo.getTimestamp());
    }

    public static boolean hasEntrySyncTime(ModuleRepositoryEntry entry)
    {
        return REPOSITORY_SYNC_TIMES.containsKey(entry.getHostRepository().getName() + ":" + entry.getName());
    }

    public static boolean hasRepoSyncTime(ModuleRepository repo)
    {
        return REPOSITORY_SYNC_TIMES.containsKey(repo.getName());
    }

    public static double getEntrySyncTime(ModuleRepositoryEntry entry)
    {
        return REPOSITORY_SYNC_TIMES.get(entry.getHostRepository().getName() + ":" + entry.getName());
    }

    public static double getRepoSyncTime(ModuleRepository repo)
    {
        return REPOSITORY_SYNC_TIMES.get(repo.getName());
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