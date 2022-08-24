package fish.yukiemeralis.flock;

public class TextUtils 
{
    /**
     * Reduces a string's length to a specified length. If the string's length is equal to or less than the specified length, the input string itself is returned.
     * @param input Input string
     * @param suffix String to be appended to the shortened input
     * @param maxLength Maximum String length before pruning, in characters
     */
    public static String pruneStringLength(String input, String suffix, int maxLength)
    {
        if (input.length() <= maxLength)
            return input;

        return new StringBuilder(input.substring(0, maxLength)).append(suffix).toString();      
    }    
}
