package com.yukiemeralis.blogspot.zenith.skins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MojangApi 
{
    private static int requests = 0;

    public HttpsURLConnection createConnection(String urlString, boolean rateLimit) throws MalformedURLException, IOException
    {
        URL url = new URL(urlString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        return connection;
    }  
}
