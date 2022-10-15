package coffee.khyonieheart.eden.utils.exception;

import coffee.khyonieheart.eden.Eden;

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
