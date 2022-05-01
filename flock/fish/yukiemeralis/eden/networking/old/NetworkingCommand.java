package fish.yukiemeralis.eden.networking.old;

import java.io.File;

import org.bukkit.command.CommandSender;

import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.java.annotations.Unimplemented;
import fish.yukiemeralis.eden.networking.NetworkingUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;

@Unimplemented("Command is potentially dangerous, and generally unstable.")
public class NetworkingCommand extends EdenCommand
{
	public NetworkingCommand(EdenModule module) 
	{
		super("edendl", module);

		this.addBranch("tdl").addBranch("<URL>").addBranch("<filepath>"); 
	}
	
	@EdenCommandHandler(usage = "edendl tdl <URL> <filepath>", description = "Download a file from a URL inside of a thread.", argsCount = 3)
	public void edencommand_tdl(CommandSender sender, String commandLabel, String[] args)
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
					filepath = "./plugins/Eden/dlcache/" + NetworkingUtils.getFinalURLPortion(args[1]);
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
