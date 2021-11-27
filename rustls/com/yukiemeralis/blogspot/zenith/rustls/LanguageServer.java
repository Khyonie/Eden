package com.yukiemeralis.blogspot.zenith.rustls;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

public class LanguageServer implements Runnable
{
    private int port = 40302;
    private List<RLSClient> connectedClients = new ArrayList<>();
    private boolean running = false;

    private static Random random = new Random();

    public LanguageServer() {}

    public LanguageServer(int port)
    {
        this.port = port;
    }

    @Override
    public void run()
    {
        PrintUtils.sendMessage("[RustLS] Attempting to start Rust language server...");

        running = true;
    }

    public void stop()
    {
        PrintUtils.sendMessage("[RustLS] Attempting to stop Rust language server...");

        running = false;
        PrintUtils.sendMessage("[RustLS] Stopped Rust language server.");
    }

    public int getPort()
    {
        return this.port;
    }

    public boolean isRunning()
    {
        return this.running;
    }

    public RLSClient createClient()
    {
        int id;

        loop: while (true)
        {
            id = 1000000 + random.nextInt(9000000);

            for (RLSClient c : connectedClients)
                if (c.getID() == id)
                    continue loop;

            break loop;
        }

        return new RLSClient(id);
    }
}
