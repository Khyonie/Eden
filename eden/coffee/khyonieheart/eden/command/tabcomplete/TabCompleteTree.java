package coffee.khyonieheart.eden.command.tabcomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An instance of a TabComplete tree. Note that this class does NOT implement {@link TabCompleter}, and does not fulfill 
 * the contract of such classes.
 * @since 1.3.0
 * @author Yuki_emeralis
 */
public class TabCompleteTree 
{
    private Map<String, TabCompleteBranch> branches = new HashMap<>();

    /**
     * Adds a branch to this tree, returning the created branch if only one is made, otherwise returns null.
     * @param labels Labels of branches to be created
     * @return The newly created branch.
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
     * Gets a branch from this node.
     * @param label
     * @return
     */
    public TabCompleteBranch getBranch(String label)
    {
        if (label.startsWith("<") && label.endsWith(">"))
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
     * Obtains a list of branches extending from this node.
     * @return A list of branches extending from this node.
     */
    public List<String> getBranchesFromHere()
    {
        return Collections.unmodifiableList(new ArrayList<>(branches.keySet())); 
    }
}