package com.yukiemeralis.blogspot.zenith.skins;

import java.util.Base64;

public class PlayerSkin 
{
    

    public static String decodeBase64(String input)
    {
        return new String(Base64.getDecoder().decode(input));
    }
}
