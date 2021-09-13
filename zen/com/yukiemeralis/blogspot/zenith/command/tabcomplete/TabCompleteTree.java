package com.yukiemeralis.blogspot.zenith.command.tabcomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabCompleteTree 
{
    private Map<String, TabCompleteBranch> branches = new HashMap<>();

    public TabCompleteBranch addBranch(String... labels)
    {
        for (String str : labels)
            branches.put(str, new TabCompleteBranch(str));

        if (labels.length == 1)
            return branches.get(labels[0]);
        return null;
    }

    public TabCompleteBranch getBranch(String label)
    {
        if (label.startsWith("<") && label.startsWith(">"))
        {
            if (branches.size() > 1 || branches.size() == 0)
                throw new IllegalArgumentException("Requested branch from tree using user parameter, yet tree has " + branches.size() + " branches. Must be 1 branch.");

            return this.branches.values().toArray(new TabCompleteBranch[1])[0];
        }

        // Handle elevation uptick
        if (this.branches.get(label) == null)
            if (this.branches.get("^" + label) != null)
                return this.branches.get("^" + label);

        return branches.get(label);
    }

    public List<String> getBranchesFromHere()
    {
        return Collections.unmodifiableList(new ArrayList<>(branches.keySet())); 
    }
}
