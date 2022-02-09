package com.yukiemeralis.blogspot.eden.networking;

import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.eden.core.CompletionsManager;
import com.yukiemeralis.blogspot.eden.core.CompletionsManager.ObjectMethodPair;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.LoadBefore;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.eden.networking.repos.EdenRepository;
import com.yukiemeralis.blogspot.eden.utils.FileUtils;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

@ModInfo(
	modName = "EdenNetworking", 
	description = "Handler for Eden-related internet tasks.",
	modFamily = "Eden base modules",
	modIcon = Material.END_PORTAL_FRAME, 
	maintainer = "Yuki_emeralis", 
	version = "1.1",
	supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1"}
)
@LoadBefore(loadBefore = {"EdenAuth"})
@PreventUnload(CallerToken.PLAYER)
public class NetworkingModule extends EdenModule
{
	private static Map<String, EdenRepository> KNOWN_REPOSITORIES = new HashMap<>();

	public NetworkingModule()
	{
		
	}
	
	@Override
	public void onEnable()
	{
		FileUtils.ensureFolder("./plugins/Eden/dlcache");

		for (File f : FileUtils.ensureFolder("./plugins/Eden/repos").listFiles())
		{
			if (!f.getName().endsWith(".json"))
				continue;

			// FIXME Ensure validity
			EdenRepository repo = JsonUtils.fromJsonFile(f.getAbsolutePath(), EdenRepository.class);

			KNOWN_REPOSITORIES.put(repo.getName(), repo);
		}

		try {
			CompletionsManager.registerCompletion("ALL_REPOSITORIES", new ObjectMethodPair(this, this.getClass().getMethod("getKnownRepoNames")), true);
		} catch (NoSuchMethodException | SecurityException e) {
			PrintUtils.log("<Failed to register completions for NetworkingModule. This may mean the module is outdated or corrupt.>", InfoType.ERROR);
			PrintUtils.printPrettyStacktrace(e);
		}

		PrintUtils.log("Loaded [" + KNOWN_REPOSITORIES.size() + "] repositories.");
	}
	
	@Override
	public void onDisable()
	{
		
	}

	public static Map<String, EdenRepository> getKnownRepositories()
	{
		return KNOWN_REPOSITORIES;
	}

	public List<String> getKnownRepoNames()
	{
		return new ArrayList<>(KNOWN_REPOSITORIES.keySet());
	}
}
