package com.yukiemeralis.blogspot.zenith.command.tabcomplete;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabCompleteBranch 
{
    private final List<String> data;
    private Map<String, TabCompleteBranch> branches = new HashMap<>();

    public TabCompleteBranch(String... data)
    {
        this.data = Arrays.asList(data);
    }

    @Override
    public String toString()
    {
        String value = "[";
        for (String str : data)
            value = value + ", " + str;

        value = value + "]";

        value = value.replaceFirst(", ", "");

        return value;
    }

    public List<String> getOptions()
    {
        return this.data;
    }

    public Map<String, TabCompleteBranch> getBranches()
    {
        return this.branches;
    }

    public TabCompleteBranch addBranches(TabCompleteBranch... data)
    {
        for (TabCompleteBranch tcb : data)
            for (String label : tcb.getOptions())
                branches.put(label, tcb);

        return this;
    }
}
