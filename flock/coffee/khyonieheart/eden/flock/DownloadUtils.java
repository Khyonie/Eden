package coffee.khyonieheart.eden.flock;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import coffee.khyonieheart.eden.flock.enums.JsonDownloadStatus;
import coffee.khyonieheart.eden.utils.JsonUtils;
import coffee.khyonieheart.eden.utils.result.Result;

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

    /**
     * Downloads a file from a URL. Threaded to prevent blocking. 
     * @param urlString
     * @param fileTarget
     * @param downloadFinishThread
     * @throws MalformedURLException
     */
    public static void downloadFile(String urlString, String fileTarget, DownloadFinishedThread downloadFinishThread) throws MalformedURLException
    {
        URL url = new URL(urlString);

        Runnable thread = () -> {
            try (BufferedInputStream stream = new BufferedInputStream(url.openStream()))
            {
                FileOutputStream output = new FileOutputStream(new File(fileTarget));

                byte[] buffer = new byte[1024];
                int byteCount = 0;

                while ((byteCount = stream.read(buffer, 0, 1024)) != -1) 
                    output.write(buffer, 0, byteCount);

                stream.close();
                output.close();

                downloadFinishThread.run(true);
            } catch (IOException e) {
                downloadFinishThread.setFailure(e);
                downloadFinishThread.run(false);
            }   
        };

        new Thread(thread).start();
    }
}
