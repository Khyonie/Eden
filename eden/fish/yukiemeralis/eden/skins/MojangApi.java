package fish.yukiemeralis.eden.skins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonSyntaxException;
import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.utils.PrintUtils;

import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;
import fish.yukiemeralis.eden.utils.option.Option;
import fish.yukiemeralis.eden.utils.result.Result;

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
    public static <T> Result performRequest(MojangRequest<T> request)
    {
        try {
            if (requests >= 600)
            {
                PrintUtils.log("Rate-limit for Mojang API has been hit. Delaying execution.", InfoType.WARN);
                PrintUtils.logVerbose("Request type: " + request.getClass().getName(), InfoType.WARN);
                queuedRequests.add(new QueuedRequest<>(request));
                return Result.err(MojangApiStatus.TOO_MANY_REQUESTS);  
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

            return Result.ok(val);
        } catch (JsonSyntaxException e) {
            return Result.err(MojangApiStatus.CORRUPT_RESPONSE);
        } catch (IOException e) {
            return Result.err(MojangApiStatus.CONNECTION_FAILED);
        }
    } 
}