package com.yukiemeralis.blogspot.eden.skins;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;
import com.yukiemeralis.blogspot.eden.skins.MojangApi.MojangApiStatus;
import com.yukiemeralis.blogspot.eden.utils.Result;

public class QueuedRequest<T> extends Thread
{
    final MojangRequest<T> request;
    final Class<T> classOfT;
    private Result<T, MojangApiStatus> result;

    public QueuedRequest(MojangRequest<T> request, Class<T> classOfT)
    {
        this.request = request;
        this.classOfT = classOfT;
    }

    @Override
    public void run()
    {
        result = new Result<>(classOfT, MojangApiStatus.class);

        try {
            T val = request.performRequest();

            result.ok(val);
        } catch (JsonSyntaxException e) {
            result.err(MojangApiStatus.CORRUPT_RESPONSE);
        } catch (IOException e) {
            result.err(MojangApiStatus.CONNECTION_FAILED);;
        }
    }

    public Result<T, MojangApiStatus> getResult()
    {
        return result;
    }
}
