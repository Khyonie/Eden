package coffee.khyonieheart.eden.skins.request;

import java.io.IOException;
import java.net.MalformedURLException;

import com.google.gson.JsonSyntaxException;

import coffee.khyonieheart.eden.skins.MojangApi;
import coffee.khyonieheart.eden.skins.MojangRequest;
import coffee.khyonieheart.eden.skins.result.UuidProfileResult;
import coffee.khyonieheart.eden.utils.JsonUtils;

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
