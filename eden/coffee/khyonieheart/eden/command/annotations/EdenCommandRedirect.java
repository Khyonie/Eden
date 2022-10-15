package coffee.khyonieheart.eden.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to list commands that can be redirected. Used for command aliasing
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EdenCommandRedirect
{
    /** List of subcommands that redirect to this command */
    String[] labels();
    /** Command to be executed instead. Replaces "args" with the player specified arguments */
    String command();
}