package fish.yukiemeralis.eden.skins;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonSyntaxException;

public abstract class MojangRequest<T> 
{
    private final HttpsURLConnection connection;

    public MojangRequest(HttpsURLConnection connection)
    {
        this.connection = connection;
    }

    public abstract T performRequest() throws IOException, JsonSyntaxException;

    protected HttpsURLConnection getConnection()
    {
        return this.connection;
    }
}
