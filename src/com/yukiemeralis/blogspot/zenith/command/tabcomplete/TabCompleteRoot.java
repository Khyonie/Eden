package com.yukiemeralis.blogspot.zenith.command.tabcomplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabCompleteRoot
{
    private Map<String, TabCompleteBranch> branches = new HashMap<>();
    private List<String> options = new ArrayList<>();
    private String parent;

    public TabCompleteRoot(String parent)
    {
        this.parent = parent;
    }

    public String getParent()
    {
        return this.parent;
    }

    public TabCompleteRoot addBranches(TabCompleteBranch... data)
    {
        for (TabCompleteBranch tcb : data)
            tcb.getOptions().forEach(label -> {
                options.add(label);
                branches.put(label, tcb);
            });

        return this;
    }

    private TabCompleteBranch branchVariant = null;
    public TabCompleteBranch asBranch()
    {
        if (branchVariant != null)
            return branchVariant;

        branchVariant = new TabCompleteBranch(parent);

        branches.forEach((label, tcb) -> {
            branchVariant.addBranches(tcb);
        });

        return asBranch();
    }

    public List<String> getOptions()
    {
        return options;
    }

    public TabCompleteBranch getBranch(String name)
    {
        return branches.get(name);
    }

    public TabCompleteBranch getBranch(String[] data)
    {
        if (data.length < 2)
            return null;

        TabCompleteBranch current = getBranch(data[0]);
        for (String label : data)
        {
            if (current == null)
                break;
            current = current.getBranches().get(label);
        }

        return current;
    }

    public Map<String, TabCompleteBranch> getBranches()
    {
        return this.branches;
    }
}
