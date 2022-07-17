package fish.yukiemeralis.eden.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * Hide a command from /eden helpall, found inside the Rosetta module.
 */
public @interface HideFromEdenHelpall {}
