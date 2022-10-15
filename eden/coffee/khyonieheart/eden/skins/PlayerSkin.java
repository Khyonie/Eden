package fish.yukiemeralis.eden.skins;

import java.util.Base64;

public class PlayerSkin 
{
    private final String signature, value;

    public PlayerSkin(String signature, String value)
    {
        this.signature = signature;
        this.value = value;
    }

    public String getSignature()
    {
        return this.signature;
    }

    public String getValue()
    {
        return this.value;
    }

    public static String decodeBase64(String input)
    {
        return new String(Base64.getDecoder().decode(input));
    }
}
