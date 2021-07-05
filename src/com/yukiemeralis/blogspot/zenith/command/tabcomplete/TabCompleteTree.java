package com.yukiemeralis.blogspot.zenith.command.tabcomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.util.StringUtil;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

public class TabCompleteTree 
{
    public TabCompleteRoot root;

    public TabCompleteTree attachRoot(TabCompleteRoot root)
    {
        this.root = root;
        return this;
    }

    public TabCompleteRoot getRoot()
    {
        return this.root;
    }

    public List<TabCompleteBranch> traverse(String[] args)
    {
        PrintUtils.log("Branches from root \"" + root.getParent() + "\": " + Arrays.toString(root.getBranches().values().toArray()), InfoType.INFO);

        long time = System.currentTimeMillis();

        // Match options
        List<String> options = new ArrayList<>();
        List<TabCompleteBranch> possibleBranches = new ArrayList<>();

        StringUtil.copyPartialMatches(args[0], root.getOptions(), options);

        PrintUtils.log("Valid branches from root: " + Arrays.toString(options.toArray()), InfoType.INFO);

        for (String label : options)
            possibleBranches.addAll(traverseRecursive(root.getBranch(label), args, 1));

        PrintUtils.log("Tabcomplete traversal took " + (System.currentTimeMillis() - time) + " ms.", InfoType.WARN);

        return possibleBranches;
    }

    private List<TabCompleteBranch> traverseRecursive(TabCompleteBranch tcb, String[] args, int depth)
    {
        List<TabCompleteBranch> validBranches = new ArrayList<>(); 

        PrintUtils.log("Navigating into higher branch...", InfoType.INFO);

        if (tcb == null)
        {
            PrintUtils.log("Navigated into non-existent branch! Returning nothing...", InfoType.INFO);
            return validBranches;
        }

        PrintUtils.log("Found higher branch! (" + (depth + 1) + "/" + args.length + " depth)", InfoType.INFO);

        try {
            PrintUtils.log("Looking for exact match for \"" + args[depth] + "\" out of " + Arrays.toString(tcb.getOptions().toArray()), InfoType.INFO);

            if (tcb.getBranches().containsKey(args[depth]))
            {
                PrintUtils.log("Found exact match! Proceeding...", InfoType.INFO);
                return traverseRecursive(tcb.getBranches().get(args[depth]), args, depth + 1);
            }

            PrintUtils.log("Didn't find an exact match.", InfoType.INFO);
        } catch (ArrayIndexOutOfBoundsException e) {
            PrintUtils.log("Extended past argument list, gathering all options from here and returning that...", InfoType.INFO);
            validBranches.addAll(tcb.getBranches().values());
            return validBranches;
        }

        
        if (tcb.getOptions().size() == 1)
        {
            if (tcb.getOptions().get(0).startsWith("<") && tcb.getOptions().get(0).endsWith(">"))
            {
                PrintUtils.log("Handling generic argument... TODO this", InfoType.INFO);
            }
        }

        List<String> options = new ArrayList<>();
        PrintUtils.log("Looking for partial matches...", InfoType.INFO);
        StringUtil.copyPartialMatches(args[depth], tcb.getOptions(), options);

        Collections.sort(options); // Sort alphabetically, prefer alphabetically higher branches
        PrintUtils.log("Partial matches, sorted alphabetically: " + Arrays.asList(options.toArray()), InfoType.INFO);

        if (args.length - 1 == depth)
        {
            PrintUtils.log("Reached maximum depth given a list of " + args.length + " arguments!", InfoType.INFO);
            for (String option : options)
                validBranches.add(tcb.getBranches().get(option));

            PrintUtils.log("Valid branch list contains " + validBranches.size() + " element(s). Returning that...", InfoType.INFO);

            return validBranches;
        }

        if (options.size() == 0)
        {
            PrintUtils.log("No options from here! Returning nothing...", InfoType.INFO);
            return validBranches;
        }

        PrintUtils.log("Proceeding down branch \"" + options.get(0) + "\"...", InfoType.INFO);
        return traverseRecursive(tcb.getBranches().get(options.get(0)), args, depth + 1);
    }

    public void printRecursive()
    {
        String fullCommand = "/" + root.getParent();
        root.getBranches().forEach((label, branch) -> {
            printRecursive(branch, fullCommand + " " + label);
        });
    }

    public void printRecursive(TabCompleteBranch branch, String command)
    {
        branch.getBranches().forEach((label, branch_) -> {
            if (branch_.getBranches().size() == 0) {
                switch (label)
                {
                    case "ENABLED_MODULES":
                        Zenith.getModuleManager().getEnabledModules().forEach(module -> {
                            PrintUtils.log("§8Branch §b-> §a" + (command + " " + module.getName()), InfoType.INFO);
                        });
                        break;
                    case "GATHERED_MODULES":
                        Zenith.getModuleManager().getGatheredModules().forEach((name, module) -> {
                            PrintUtils.log("§8Branch §b-> §a" + (command + " " + module.getName()), InfoType.INFO);
                        });
                        break;
                    default: 
                        PrintUtils.log("§8Branch §b-> §a" + (command + " " + label), InfoType.INFO);
                        break;
                }
            } else {
                PrintUtils.log("§8Branch §b-> §8" + (command + " " + label), InfoType.INFO);
                printRecursive(branch_, command + " " + label);
            }
        });
    }
}
