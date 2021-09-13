package com.yukiemeralis.blogspot.zenith.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils 
{
    public static byte[] hashStringSHA256(String input)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes("UTF-8"));

            return digest.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            PrintUtils.printPrettyStacktrace(e);
            return null;
        }
    }

    public static String hexToString(byte[] input)
    {
        StringBuilder builder = new StringBuilder();
        for (byte b : input)
            builder.append(Integer.toString((b & 0xFF) + 0x100, 16));

        return builder.toString();
    }
}
