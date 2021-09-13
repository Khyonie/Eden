package com.yukiemeralis.blogspot.zenith.events;

public class WrappedType<T>
{
    private final Class<T> type;
    private final T obj;

    public WrappedType(Class<T> type, T obj)
    { 
        this.type = type;
        this.obj = obj;
    }

    public Class<T> getType()
    {
        return this.type;
    }

    public T getWrappedObject()
    {
        return this.obj;
    }
}
