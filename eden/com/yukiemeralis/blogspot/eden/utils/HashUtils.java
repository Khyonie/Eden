package com.yukiemeralis.blogspot.eden.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.yukiemeralis.blogspot.eden.Eden;

public class HashUtils 
{
    public static enum HashAlgorithm
    {
        SHA_1       ("SHA-1"),
        SHA_224     ("SHA-224"),
        SHA_256     ("SHA-256"),
        SHA_384     ("SHA-384"),
        SHA_512_256 ("SHA-512/224"),
        SHA_512_224 ("SHA-512/256"),
        SHA3_224    ("SHA3-224"),
        SHA3_256    ("SHA3-256"),
        SHA3_384    ("SHA3-384"),
        SHA3_512    ("SHA3-512"),
        MD5         ("MD5"),
        MD2         ("MD2")
        ;

        final String label;

        HashAlgorithm(String label)
        {
            this.label = label;
        }

        public String getLabel()
        {
            return this.label;
        }
    }

    /**
     * Converts a String into a salted hash array of bytes using the SHA-256 hashing algorithm. Salt is provided by an Eden main config value.
     * @param input Unhashed input
     * @return Salted hashed input
     */
    public static byte[] hashStringSHA256(String input)
    {
        return hashString(HashAlgorithm.SHA_256, input);
    }

    /**
     * Converts a String into a salted hash array of bytes using a supported hashing algorithm. Salt is provided by an Eden main config value.
     * @param input Unhashed input
     * @return Salted hashed input
     */
    public static byte[] hashString(HashAlgorithm algorithm, String input)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getLabel());
            digest.update((input + Eden.getEdenConfig().get("hashSalt")).getBytes("UTF-8"));

            return digest.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            PrintUtils.printPrettyStacktrace(e);
            return null;
        }
    }

    /**
     * Converts an array of bytes to an accurate String representation.
     * @param input Salted hashed array of bytes
     * @return String salted hash
     */
    public static String hexToString(byte[] input)
    {
        StringBuilder builder = new StringBuilder();
        for (byte b : input)
            builder.append(Integer.toString((b & 0xFF) + 0x100, 16));

        return builder.toString();
    }
}