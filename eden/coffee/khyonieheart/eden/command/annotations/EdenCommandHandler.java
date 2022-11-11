package coffee.khyonieheart.eden.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method named "edencommand_name" as a valid command method
 * @since 1.0
 * @author Khyonie
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EdenCommandHandler 
{
    /** This command's usage */
    String usage();
    /** This command's description */
    String description();
    /** How many arguments this command takes. "<code>edencommand_nosubcmd</code>" would take 0, a subcommand would take at least 1, and so on */
    int argsCount();
}