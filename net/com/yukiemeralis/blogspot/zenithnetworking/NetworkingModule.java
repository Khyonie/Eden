package com.yukiemeralis.blogspot.zenithnetworking;

import org.bukkit.Material;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.LoadBefore;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ModInfo;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.zenith.utils.FileUtils;

@ModInfo(
	modName = "ZenithNetworking", 
	description = "Handler for Zenith-related internet tasks.",
	modFamily = "Zenith base modules",
	modIcon = Material.END_PORTAL_FRAME, 
	maintainer = "Yuki_emeralis", 
	version = "1.1"
)
@LoadBefore(loadBefore = {"ZenithAuth"})
@PreventUnload(CallerToken.ZENITH)
public class NetworkingModule extends ZenithModule
{
	public NetworkingModule()
	{
		//addCommand(new NetworkingCommand());
	}
	
	@Override
	public void onEnable()
	{
		FileUtils.ensureFolder("./plugins/Zenith/dlcache");
	}
	
	@Override
	public void onDisable()
	{
		
	}
}
