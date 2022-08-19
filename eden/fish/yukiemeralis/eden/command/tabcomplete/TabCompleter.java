package fish.yukiemeralis.eden.command.tabcomplete;

public interface TabCompleter 
{
    public TabCompleter getBranch(String label);
    public TabCompleter addBranch(String... branchLabels);
    public TabCompleteMultiBranch getMultiBranch(String... labels);
}
