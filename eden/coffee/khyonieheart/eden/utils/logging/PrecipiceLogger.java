package coffee.khyonieheart.eden.utils.logging;

import org.apache.commons.lang.StringUtils;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.utils.PrintUtils;

public class PrecipiceLogger implements Logger
{
    @Override
    public String format(String message, InfoType type) 
    {
        String buffer = message;
        for (String regex : PrintUtils.getLogColors().keySet())
            buffer = buffer.replaceAll(regex, PrintUtils.getLogColors().get(regex));
        return "§bEDEN §3> §7LOGGING " + PrintUtils.getInfoColors().get(type) + type.name() + " §3> §8" + StringUtils.normalizeSpace(buffer).replaceAll("\\\\(?=[\\[\\]\\(\\)\\{\\}<>])", "");
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
        return "§bEDEN §3> §6VERBOSE " + PrintUtils.getInfoColors().get(type) + type.name() + " §3> §e" + StringUtils.normalizeSpace(message);
    }

    @Override
    public String logVerbose(String message, InfoType type) 
    {
        String logMessage = formatVerbose(message, type);
        Eden.getInstance().getServer().getConsoleSender().sendMessage(logMessage);

        return logMessage;
    }
}
