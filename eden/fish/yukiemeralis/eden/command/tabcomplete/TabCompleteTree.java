package fish.yukiemeralis.eden.command.tabcomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a TabComplete structure. Tree is traversed top down to attempt to reach a given branch.
 */
public class TabCompleteTree 
{
    private Map<String, TabCompleteBranch> branches = new HashMap<>();

    /**
     * Adds a branch to the tree.
     * @return The branch to be added. If multiple branches are added, <code>null</code> is returned.
     */
    public TabCompleteBranch addBranch(String... labels)
    {
        for (String str : labels)
            branches.put(str, new TabCompleteBranch(str));			

        if (labels.length == 1)
            return branches.get(labels[0]);
        return null;
    }

    /**
     * Obtains a branch with the given name.
     * @return A branch with the given name.
     */
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

    /**
     * Obtains a list of all branches that extend from this branch.
     * @return A list of all branches that extend from this branch.
     */
    public List<String> getBranchesFromHere()
    {
        return Collections.unmodifiableList(new ArrayList<>(branches.keySet())); 
    }
}
