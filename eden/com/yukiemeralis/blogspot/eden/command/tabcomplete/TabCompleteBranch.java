package com.yukiemeralis.blogspot.eden.command.tabcomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object that contains a String label and a list of branches.
 * @Author Yuki_emeralis
 *
 */
@SuppressWarnings("unused")
public class TabCompleteBranch 
{
    private final String label;
    private Map<String, TabCompleteBranch> attachedBranches = new HashMap<>(); 
    private final boolean requiresPassword;

    /**
     * TabComplete branch with a label.
     * @param label The label to use for this branch.
     */
    public TabCompleteBranch(String label)
    {
        this.label = label;
        if (label.startsWith("^"))
        {
            requiresPassword = true;
            return;
        }
            
        requiresPassword = false;
    }

    /**
     * Obtains the label in use for this branch.
     * @return This branch's label
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Adds one or more TabCompleteBranches to this tree structure.
     * @param branchLabels Label(s) of new branches.
     * @return The newly created branch if only one label was specified. Zero or more than one new branch results in null.
     */
    public TabCompleteBranch addBranch(String... branchLabels)
    {
        for (String str : branchLabels)
            attachedBranches.put(str, new TabCompleteBranch(str));

        if (branchLabels.length == 1)
            return attachedBranches.get(branchLabels[0]);
        return null;
    }

    /**
     * Obtains a branch attached to this branch which matches the given label.
     * @param label The label of a branch.
     * @return An attached branch with the given label.
     */
    public TabCompleteBranch getBranch(String label)
    {
        if (label.startsWith("<") && label.startsWith(">"))
        {
            if (attachedBranches.size() > 1 || attachedBranches.size() == 0)
                throw new IllegalArgumentException("Requested branch from tree using user parameter, yet tree has " + attachedBranches.size() + " branches. Must be 1 branch.");

            return this.attachedBranches.values().toArray(new TabCompleteBranch[1])[0];
        }

        // Handle elevation uptick
        if (this.attachedBranches.get(label) == null)
            if (this.attachedBranches.get("^" + label) != null)
                return this.attachedBranches.get("^" + label);

        return this.attachedBranches.get(label);
    }

    /**
     * Obtains a list of all attached branches, by label.
     * @return A list of all attached branch labels.
     */
    public List<String> getBranchesFromHere()
    {
        return Collections.unmodifiableList(new ArrayList<>(attachedBranches.keySet())); 
    }

    /**
     * @return Whether or not this branch has any attached branches.
     */
    public boolean isLeaf()
    {
        return attachedBranches.size() == 0;
    }
}
