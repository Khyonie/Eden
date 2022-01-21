package com.yukiemeralis.blogspot.eden.skins.request;

import java.io.IOException;
import java.net.MalformedURLException;

import com.google.gson.JsonSyntaxException;
import com.yukiemeralis.blogspot.eden.skins.MojangApi;
import com.yukiemeralis.blogspot.eden.skins.MojangRequest;
import com.yukiemeralis.blogspot.eden.skins.result.UserProfileResult;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;

public class UserProfileRequest extends MojangRequest<UserProfileResult>
{
    public UserProfileRequest(String uuid) throws MalformedURLException, IOException 
    {
        super(MojangApi.createConnection("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid));
    }

    @Override
    public UserProfileResult performRequest() throws IOException, JsonSyntaxException 
    {
        return JsonUtils.getGson().fromJson(this.getConnection().getResponseMessage(), UserProfileResult.class);
    }
    
}
