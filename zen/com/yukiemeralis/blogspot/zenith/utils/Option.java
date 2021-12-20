package com.yukiemeralis.blogspot.zenith.utils;

/** Represents an option where the two states are SOME, where the value is defined, and NONE, where the value is null. */
public class Option<T>
{
    public static enum OptionState
    {
        SOME,
        NONE
        ;
    }

    private Class<? extends T> tClass;
    private OptionState state = OptionState.NONE;
    private T contained = null;

    public Option(Class<? extends T> tClass)
    {
        this.tClass = tClass;
    }

    public Class<? extends T> getTClass()
    {
        return this.tClass;
    }

    public void some(T contained)
    {
        if (contained == null)
            return;

        this.state = OptionState.SOME;
        this.contained = contained;
    }

    public OptionState getState()
    {
        return this.state;
    }

    public T unwrap()
    {
        if (state.equals(OptionState.NONE))
            return null;
        if (state.equals(OptionState.SOME))
            return contained;
        return null; // This can't ever fire
    }
}
