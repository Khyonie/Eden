package com.yukiemeralis.blogspot.eden.networking;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.core.CompletionsManager;
import com.yukiemeralis.blogspot.eden.core.CompletionsManager.ObjectMethodPair;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.EdenConfig;
import com.yukiemeralis.blogspot.eden.module.EdenModule.LoadBefore;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;
import com.yukiemeralis.blogspot.eden.module.java.annotations.DefaultConfig;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.eden.networking.enums.ModuleUpgradeStatus;
import com.yukiemeralis.blogspot.eden.networking.repos.EdenRepository;
import com.yukiemeralis.blogspot.eden.networking.repos.EdenRepositoryEntry;
import com.yukiemeralis.blogspot.eden.utils.FileUtils;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

@ModInfo(
	modName = "Flock", 
	description = "Handler for Eden-related internet tasks.",
	modFamily = "Eden base modules",
	modIcon = Material.END_PORTAL_FRAME, 
	maintainer = "Yuki_emeralis", 
	version = "1.2.0",
	supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2"}
)
@LoadBefore(loadBefore = {"Checkpoint", "Surface"})
@EdenConfig
@DefaultConfig(
	keys =   { "defaultDownloadBehavior" },
	values = { "LOAD_ENABLE" }
)
@PreventUnload(CallerToken.EDEN)
public class NetworkingModule extends EdenModule
{
	private static Map<String, EdenRepository> KNOWN_REPOSITORIES = new HashMap<>();
	private static Map<String, Long> LAST_KNOWN_SYNC = new HashMap<>();
	private static NetworkingModule instance;

	private static final String SYNC_FILE = "./plugins/Eden/repos/.syncdata";

	public NetworkingModule()
	{
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onEnable()
	{
		instance = this;
		FileUtils.ensureFolder("./plugins/Eden/dlcache");

		for (File f : FileUtils.ensureFolder("./plugins/Eden/repos").listFiles())
		{
			if (!f.getName().endsWith(".json"))
				continue;

			EdenRepository repo = JsonUtils.fromJsonFile(f.getAbsolutePath(), EdenRepository.class);

			if (repo == null)
			{
				PrintUtils.log("<Repository file \"" + f.getAbsolutePath() + "\" is corrupt! Moving to lost and found...>");
				FileUtils.moveToLostAndFound(f);
				continue;
			}
			
			repo.setRepoReferences();
			KNOWN_REPOSITORIES.put(repo.getName(), repo);
		}

		Type mapType = new TypeToken<Map<String, Long>>() {}.getType();

		File syncFile = new File(SYNC_FILE);
		if (syncFile.exists())
			LAST_KNOWN_SYNC = (Map<String, Long>) JsonUtils.fromJsonFile(SYNC_FILE, mapType);

		if (LAST_KNOWN_SYNC == null)
		{
			PrintUtils.log("<Sync timestamp file is corrupt! Starting anew...>");
			FileUtils.moveToLostAndFound(syncFile);

			LAST_KNOWN_SYNC = new HashMap<>();
		}

		try {
			CompletionsManager.registerCompletion("ALL_REPOSITORIES", new ObjectMethodPair(this, this.getClass().getMethod("getKnownRepoNames")), true);
		} catch (NoSuchMethodException | SecurityException e) {
			PrintUtils.log("<Failed to register completions for NetworkingModule. This may mean the module is outdated or corrupt.>", InfoType.ERROR);
			PrintUtils.printPrettyStacktrace(e);
		}

		PrintUtils.log("Loaded [" + KNOWN_REPOSITORIES.size() + "] " + PrintUtils.plural(KNOWN_REPOSITORIES.size(), "repository", "repositories") + ".");
	}
	
	@Override
	public void onDisable()
	{
		JsonUtils.toJsonFile(SYNC_FILE, LAST_KNOWN_SYNC);
	}

	public static Map<String, EdenRepository> getKnownRepositories()
	{
		return KNOWN_REPOSITORIES;
	}

	public static ModuleUpgradeStatus getModuleUpgradeStatus(String name, EdenRepositoryEntry entry)
	{
		if (!Eden.getModuleManager().isModulePresent(name))
		{
			return Eden.getModuleManager().isCompatible(entry.getSupportedApiVersions()) ? ModuleUpgradeStatus.NOT_INSTALLED : ModuleUpgradeStatus.INCOMPATIBLE_SERVER;
		}

		if (Eden.getModuleManager().isCompatible(entry.getSupportedApiVersions()))
		{
			// Check timestamps first
			if (getLastKnownSync(name) != -1)
				if (entry.getTimestamp() > getLastKnownSync(name))
					return ModuleUpgradeStatus.UPGRADABLE;

			// Otherwise check actual version
			return Eden.getModuleManager().getModuleByName(name).getVersion().equals(entry.getVersion()) ? ModuleUpgradeStatus.SAME_VERSION : ModuleUpgradeStatus.UPGRADABLE;
		}

		return ModuleUpgradeStatus.UPGRADABLE_INCOMPATIBLE_SERVER;
	}

	public static EdenModule getModuleInstance()
	{
		return instance;
	}

	public static List<String> getKnownRepoNames()
	{
		return new ArrayList<>(KNOWN_REPOSITORIES.keySet());
	}

	public static long getLastKnownSync(String name)
	{
		if (!LAST_KNOWN_SYNC.containsKey(name))
			return -1;

		try {
			return LAST_KNOWN_SYNC.get(name);
		} catch (ClassCastException e) {
			for (Player p : Eden.getInstance().getServer().getOnlinePlayers())
			{
				PrintUtils.sendMessage(p, "§9Observed instance of Issue #012. This bug is under active investigation, please send a screenshot to Yuki_emeralis if I'm not already on! (https://github.com/YukiEmeralis/Eden/issues/12)");
			}
			return -1;
		}
	}

	public static void updateLastKnownSync(EdenRepositoryEntry entry)
	{
		LAST_KNOWN_SYNC.put(entry.getName(), entry.getTimestamp());
	}
}
