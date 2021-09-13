package com.yukiemeralis.blogspot.zenithnetworking;

import java.io.File;

import org.bukkit.command.CommandSender;

import com.yukiemeralis.blogspot.zenith.command.ZenithCommand;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

public class NetworkingCommand extends ZenithCommand
{
	public NetworkingCommand(ZenithModule module) 
	{
		super("zendl", module);

		this.addBranch("tdl").addBranch("<URL>").addBranch("<filepath>"); 
	}
	
	@ZenCommandHandler(usage = "zendl tdl <URL> <filepath>", description = "Download a file from a URL inside of a thread.", argsCount = 3)
	public void zcommand_tdl(CommandSender sender, String commandLabel, String[] args)
	{
		if (!args[2].endsWith("/"))
			args[2] = args[2] + "/";
		
		PrintUtils.sendMessage(sender, "Begun download...");
		PrintUtils.log("Downloading to [" + args[2] + NetworkingUtils.getFinalURLPortion(args[1]) + "]", InfoType.INFO);
		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				String filepath;
				if (args.length >= 3)
				{
					filepath = args[2] + NetworkingUtils.getFinalURLPortion(args[1]);
				} else {
					filepath = "./plugins/Zenith/dlcache/" + NetworkingUtils.getFinalURLPortion(args[1]);
				}
				
				File f = new File(filepath);
				
				if (!f.exists() || f.length() == 0)
				{
					PrintUtils.sendMessage(sender, "Download failed. Please ensure the URL is correct, and that you have read/write permissions to the target filepath.");
					return;
				}
				
				PrintUtils.sendMessage(sender, "Download finished!");
			}
		};
		
		NetworkingUtils.downloadFileFromURLThreaded(args[1], args[2] + NetworkingUtils.getFinalURLPortion(args[1]), t);
	}
}
