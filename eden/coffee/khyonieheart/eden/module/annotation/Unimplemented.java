package coffee.khyonieheart.eden.module.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a class to be ignored by the class collection process, but will be implemented later.<br>
 * Optional string value that describes why this class is unimplemented and when it may be implemented.
 * @author Yuki_emeralis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Unimplemented 
{
	/** */
    String value() default "This class is not finished.";
}
