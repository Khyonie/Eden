package coffee.khyonieheart.eden.command.tabcomplete;

/**
 * Interface for classes that are made to be part of a TabComplete structure
 */
public interface TabCompleter 
{
    /**
     * Gets a branch from a structure.
     * @param label The branch label to obtain
     * @return A branch matching the given label, or null if no branch was found
     */
    public TabCompleter getBranch(String label);
    /**
     * Adds one or more branches to a structure. The contract for this operation goes as follows:<p>
     * - If exactly one branch is given, return that branch.<p>
     * - If more than one branch is given, return a TabCompleteMultiBranch. 
     * @param branchLabels The labels of branches to add
     * @return A TabCompleteBranch of only one branch is specified, or a TabCompleteMultiBranch if two or more branches are specified
     * @throws UnsupportedOperationException If no branches are given
     */
    public TabCompleter addBranch(String... branchLabels);
    /**
     * Obtains a TabCompleteMultiBranch with the given inputs.
     * @param labels The exact labels contained in the TabCompleteMultiBranch to obtain
     * @return A TabCompleteMultiBranch with the exact labels given, or null if none was found
     */
    public TabCompleteMultiBranch getMultiBranch(String... labels);
}
