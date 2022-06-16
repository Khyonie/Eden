package fish.yukiemeralis.eden.utils.exception;

import fish.yukiemeralis.eden.Eden;

/**
 * Exception for NMS-based classes.
 */
public class VersionNotHandledException extends RuntimeException
{
    public VersionNotHandledException()
    {
        super("API version " + Eden.getNMSVersion() + " is not handled");
    }
}
