package coffee.khyonieheart.eden.skins;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;

import coffee.khyonieheart.eden.skins.MojangApi.MojangApiStatus;
import coffee.khyonieheart.eden.utils.result.Result;

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
