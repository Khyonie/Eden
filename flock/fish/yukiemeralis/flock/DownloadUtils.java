package fish.yukiemeralis.flock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.result.Result;
import fish.yukiemeralis.flock.enums.JsonDownloadStatus;

public class DownloadUtils 
{
    public static Result downloadJson(String urlString, Class<?> type)
    {
        Object obj;
        try {
            URL url = new URL(urlString);

            try (InputStream in = url.openStream())
            {
                obj = JsonUtils.getGson().fromJson(new InputStreamReader(in), type);
            } catch (JsonIOException | JsonSyntaxException e) {
                return Result.err(JsonDownloadStatus.CORRUPT_REPOSITORY);
            } catch (IOException e) {
                return Result.err(JsonDownloadStatus.CONNECTION_FAILED);
            }
        } catch (MalformedURLException e) {
            return Result.err(JsonDownloadStatus.MALFORMED_URL);
        }

        return Result.ok(obj);
    }
}
