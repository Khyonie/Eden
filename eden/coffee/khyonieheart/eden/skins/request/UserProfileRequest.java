package coffee.khyonieheart.eden.skins.request;

import java.io.IOException;
import java.net.MalformedURLException;

import com.google.gson.JsonSyntaxException;

import coffee.khyonieheart.eden.skins.MojangApi;
import coffee.khyonieheart.eden.skins.MojangRequest;
import coffee.khyonieheart.eden.skins.result.UserProfileResult;
import coffee.khyonieheart.eden.utils.JsonUtils;

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
