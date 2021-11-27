package com.yukiemeralis.blogspot.zenith.rustls;

public class RLSClient implements Runnable
{
    private int id;

    public RLSClient(int id)
    {
        this.id = id;
    }

    public int getID()
    {
        return this.id;
    }

    @Override
    public void run()
    {
        
    }
}
