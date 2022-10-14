package fish.yukiemeralis.eden.marker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for methods that are not allowed to return null, as well as required method parameters.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface NotNull 
{
    String value() default "This class is not allowed to return null.";    
}
