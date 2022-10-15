package fish.yukiemeralis.eden.command.tabcomplete;

import java.util.ArrayList;
import java.util.List;

/**
 * An assistive extension to a TabComplete tree that allows adding a branch to multiple branches at once.
 */
public class TabCompleteMultiBranch implements TabCompleter
{
    private TabCompleter[] data;

    /**
     * Constructor that takes in an array of TabCompleters
     * @param branches Array of branches
     */
    public TabCompleteMultiBranch(TabCompleter... branches)
    {
        this.data = branches;
    }

    /**
     * Constructor that takes in a List of TabCompleters
     * @param branches List of TabCompleters
     */
    public TabCompleteMultiBranch(List<TabCompleter> branches)
    {
        this.data = branches.toArray(new TabCompleter[branches.size()]);
    }

    @Override
    public TabCompleter getBranch(String label) 
    {
        throw new UnsupportedOperationException("Cannot get a branch from a multi-branch object. Must traverse the tree from its roots."); 
    }

    @Override
    public TabCompleter addBranch(String... branchLabels) 
    {
        List<TabCompleter> addedBranches = new ArrayList<>(); 
        if (branchLabels.length == 0)
            throw new UnsupportedOperationException("Must add at least one branch");

        for (TabCompleter tcb : data)
            addedBranches.add(tcb.addBranch(branchLabels));

        return new TabCompleteMultiBranch(addedBranches);
    }

    @Override
    public TabCompleteMultiBranch getMultiBranch(String... labels) 
    {
        throw new UnsupportedOperationException("Cannot get a branch from a multi-branch object. Must traverse the tree from its roots.");
    }
}
