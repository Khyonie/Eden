package fish.yukiemeralis.eden.skins;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;

import fish.yukiemeralis.eden.skins.MojangApi.MojangApiStatus;
import fish.yukiemeralis.eden.utils.result.Result;

public class QueuedRequest<T> extends Thread
{
    final MojangRequest<T> request;
    private Result result;

    public QueuedRequest(MojangRequest<T> request)
    {
        this.request = request;
    }

    @Override
    public void run()
    {
        try {
            result = Result.ok(request.performRequest());
        } catch (JsonSyntaxException e) {
            result = Result.err(MojangApiStatus.CORRUPT_RESPONSE);
        } catch (IOException e) {
            result = Result.err(MojangApiStatus.CONNECTION_FAILED);;
        }
    }

    public Result getResult()
    {
        return result;
    }
}
