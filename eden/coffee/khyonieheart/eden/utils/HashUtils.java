package coffee.khyonieheart.eden.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


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
     * Converts a String into a salted hash array of bytes using the SHA-256 hashing algorithm.
     * @param input Unhashed input
     * @return Salted hashed input
     */
    public static byte[] hashStringSHA256(String input, String salt)
    {
        return hashString(HashAlgorithm.SHA_256, input, salt);
    }

    /**
     * Converts a String into a salted hash array of bytes using a supported hashing algorithm
     * @param input Unhashed input
     * @return Salted hashed input
     */
    public static byte[] hashString(HashAlgorithm algorithm, String input, String salt)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getLabel());
            digest.update((input + salt).getBytes("UTF-8"));

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

    public static String genererateSalt(int length)
    {
        StringBuilder builder = new StringBuilder();
        Random random = new Random(System.currentTimeMillis());

        for (int i = 0; i < length; i++)
            builder.append((char) random.nextInt(0x20, 0x7F));

        random = null;
        return builder.toString();
    }
}