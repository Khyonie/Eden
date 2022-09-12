package fish.yukiemeralis.eden.module.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for modules that have an @EdenConfig annotation. Provides the keys and values for a default config. Default field mapping is <code>EDEN_DEFAULT_CONFIG</code>.
 * @author Yuki_emeralis
 * @deprecated {@link EdenConfig} annotation now handles this annotation's function.
 * @see {@link EdenConfig}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface DefaultConfig
{
	/** Field name for a Map of keys and values for a default configuration. */
	String value() default "EDEN_DEFAULT_CONFIG";
}
