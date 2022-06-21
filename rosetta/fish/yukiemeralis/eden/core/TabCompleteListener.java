package fish.yukiemeralis.eden.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.util.StringUtil;

import fish.yukiemeralis.eden.command.CommandManager;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.command.tabcomplete.TabCompleteBranch;

/**
 * Listener for Eden TabComplete suggestions.
 * @author Yuki_emeralis
 */
public class TabCompleteListener implements Listener
{
	/**
	 * Fired when a user attempts to generate a tabcomplete.
	 * @param event The event fired.
	 */
    @EventHandler
    public void onTabComplete(TabCompleteEvent event)
    {
        String[] commandFull = event.getBuffer().split(" ", -1);

        if (commandFull[0].length() == 0)
            return;

        String commandLabel = commandFull[0].substring(1); 
        
        EdenCommand cmd = CommandManager.getEdenCommand(commandLabel);

        if (cmd == null)
            return; // Not an Eden command

        List<String> buffer = new ArrayList<>();

        // Only the label is present
        if (commandFull.length <= 2)
        {
            if (commandFull.length == 2)
            {
                StringUtil.copyPartialMatches(commandFull[1], trimElevationMarkers(cmd.getTree().getBranchesFromHere()), buffer);
                event.setCompletions(buffer);
                return;
            }
            
            event.setCompletions(trimElevationMarkers(cmd.getTree().getBranchesFromHere()));
            return;
        }

        // At least one argument is present
        String[] args = new String[commandFull.length - 1];

        for (int i = 1; i < commandFull.length; i++)
        {
            args[i - 1] = commandFull[i];
        }

        TabCompleteBranch branch = cmd.getBranch(args[0]);
        if (branch == null)
        {
            return;
        }

        int index = -1;
        String finalArg = "";
        for (String arg : args)
        {
            index++;

            // Ignore elevated permission arguments
            if (arg.startsWith("^"))
                arg = arg.replace("^", "");

            // Ignore the first argument
            if (arg == args[0])
            {
                if (args.length <= 1)
                    break;

                continue;
            }

            finalArg = arg;

            if (branch != null)
            {
                if (branch.getBranchesFromHere().size() == 1)
                {
                    if (branch.getBranchesFromHere().get(0).startsWith("<") && branch.getBranchesFromHere().get(0).endsWith(">"))
                    {
                        branch = branch.getBranch(branch.getBranchesFromHere().get(0));

                        if (args.length > (index + 1))
                        {
                            continue;
                        }
                        
                        genUserArguments(branch, arg, event);
                        return;
                    }
                }

                if (branch.getBranch(arg) == null)
                {
                    break;
                }

                branch = branch.getBranch(arg);
                continue;
            }

            // If there are no more branches from here, break
            break;
        }

        StringUtil.copyPartialMatches(finalArg, trimElevationMarkers(branch.getBranchesFromHere()), buffer);

        event.setCompletions(buffer);
    }

    private void genUserArguments(TabCompleteBranch branch, String arg, TabCompleteEvent event)
    {
        String label = branch.getLabel().replaceAll("[<>]", "");

        if (CompletionsManager.hasCompletion(label))
        {
            List<String> buffer = new ArrayList<>();
            StringUtil.copyPartialMatches(arg, CompletionsManager.getCompletions(label), buffer);

            // User args cannot have an elevation uptick, so we don't need to trim any markers
            event.setCompletions(buffer);
            return;
        }

        event.setCompletions(trimElevationMarkers(Arrays.asList(new String[] {label})));
    }

    private List<String> trimElevationMarkers(List<String> input)
    {
        List<String> buffer = new ArrayList<>(input);
        for (int i = 0; i < input.size(); i++)
            buffer.set(i, input.get(i).replace("^", ""));
        return buffer;
    }
}
