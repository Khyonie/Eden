package com.yukiemeralis.blogspot.eden.networking;

import org.bukkit.Material;

import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.LoadBefore;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.eden.utils.FileUtils;

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
	public NetworkingModule()
	{
		
	}
	
	@Override
	public void onEnable()
	{
		FileUtils.ensureFolder("./plugins/Eden/dlcache");
	}
	
	@Override
	public void onDisable()
	{
		
	}
}
