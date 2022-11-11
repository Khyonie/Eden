package coffee.khyonieheart.eden.utils.logging;

public interface Logger 
{
    public String format(String message, InfoType type);
    public String log(String message, InfoType type);

    public String formatVerbose(String message, InfoType type);
    public String logVerbose(String message, InfoType type);

    /**
     * Category of information, used for logging.
     * @author Khyonie
     */
    public static enum InfoType
    {
        INFO,
        WARN,
        ERROR,
        FATAL 
        ;   
    }
}
