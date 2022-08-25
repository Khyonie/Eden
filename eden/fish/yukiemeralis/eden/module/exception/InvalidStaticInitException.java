package fish.yukiemeralis.eden.module.exception;

public class InvalidStaticInitException extends RuntimeException
{
    public InvalidStaticInitException(String message, Throwable causedBy)
    {
        super(message, causedBy);
    }
}