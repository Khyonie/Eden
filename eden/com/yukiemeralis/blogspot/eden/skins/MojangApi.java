package com.yukiemeralis.blogspot.eden.skins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonSyntaxException;
import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.utils.Option;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.Result;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("unused")
public class MojangApi 
{
    private static int requests = 0; // Up to 600 every 10 minutes
    private static List<QueuedRequest<?>> queuedRequests = new ArrayList<>();

    public enum MojangApiStatus 
    {
        GOOD,
        TOO_MANY_REQUESTS,
        CORRUPT_RESPONSE,
        CONNECTION_FAILED
        ;
    }

    public static HttpsURLConnection createConnection(String urlString) throws MalformedURLException, IOException
    {
        URL url = new URL(urlString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        return connection;
    } 
   
    /**
     * Performs a request for the mojang API to handle. Requests are rate-limited to 600 every 10 minutes. 
     * This method causes blocking, and as such should not be run on the main thread. Always defer execution to another thread.
     * 
     * If the rate limit is hit, execution will be delayed until a request slot is opened.
     * @param <T>
     * @param request
     * @param classOfT
     * @return
     */
    public static <T> Result<T, MojangApiStatus> performRequest(MojangRequest<T> request, Class<T> classOfT)
    {
        Result<T, MojangApiStatus> option = new Result<>(classOfT, MojangApiStatus.class);

        try {
            if (requests >= 600)
            {
                PrintUtils.log("Rate-limit for Mojang API has been hit. Delaying execution.", InfoType.WARN);
                PrintUtils.logVerbose("Request type: " + request.getClass().getName(), InfoType.WARN);
                queuedRequests.add(new QueuedRequest<>(request, classOfT));
                option.err(MojangApiStatus.TOO_MANY_REQUESTS);  

                return option;
            }

            T val = request.performRequest();  
            
            requests++;
            new BukkitRunnable()
            {
                @Override
                public void run() 
                {
                    requests--;   
                }
            }.runTaskLater(Eden.getInstance(), 600*20);

            return option;
        } catch (IOException e) {
            return null;
        }
    } 
}