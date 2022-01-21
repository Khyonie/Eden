package com.yukiemeralis.blogspot.eden.skins.request;

import java.io.IOException;
import java.net.MalformedURLException;

import com.google.gson.JsonSyntaxException;
import com.yukiemeralis.blogspot.eden.skins.MojangApi;
import com.yukiemeralis.blogspot.eden.skins.MojangRequest;
import com.yukiemeralis.blogspot.eden.skins.result.UuidProfileResult;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;

public class UuidRequest extends MojangRequest<UuidProfileResult>
{
    public UuidRequest(String username) throws MalformedURLException, IOException 
    {
        super(MojangApi.createConnection("https://api.mojang.com/users/profiles/minecraft/" + username));
    }

    @Override
    public UuidProfileResult performRequest() throws JsonSyntaxException, IOException
    {
        return JsonUtils.getGson().fromJson(this.getConnection().getResponseMessage(), UuidProfileResult.class); 
    }
}
