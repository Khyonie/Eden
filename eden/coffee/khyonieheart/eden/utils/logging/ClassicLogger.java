package coffee.khyonieheart.eden.utils.logging;

import org.apache.commons.lang.StringUtils;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.utils.PrintUtils;

/**
 * Logger with the classic < 1.7.3 style.
 */
public class ClassicLogger implements Logger
{
    /**
     * Formats a logging message.
     * @param input A given message.
     * @return A log message with Eden's formatting.
     */
    @Override
    public String format(String message, InfoType type) 
    {
        String buffer = message;
        for (String regex : PrintUtils.getLogColors().keySet())
            buffer = buffer.replaceAll(regex, PrintUtils.getLogColors().get(regex));

        return "§8[" + PrintUtils.getInfoColors().get(type) + "e§8]§8 " + StringUtils.normalizeSpace(buffer).replaceAll("\\\\(?=[\\[\\]\\(\\)\\{\\}<>])", "");
    }

    @Override
    public String log(String message, InfoType type) 
    {
        String logMessage = format(message, type);
        Eden.getInstance().getServer().getConsoleSender().sendMessage(logMessage);

        return logMessage;
    }

    @Override
    public String formatVerbose(String message, InfoType type) 
    {
        return "§8[§ae§8]§7 " + PrintUtils.getInfoColors().get(type)  + StringUtils.normalizeSpace(message);
    }

    @Override
    public String logVerbose(String message, InfoType type) 
    {
        String logMessage = formatVerbose(message, type);
        Eden.getInstance().getServer().getConsoleSender().sendMessage(logMessage);
        return logMessage;
    }
}
