package fish.yukiemeralis.eden.module.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Annotation to notify Eden that this module has a configuration file.
 * @author Yuki_emeralis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EdenConfig 
{
	/** Field name for a Map of keys and values for a default configuration. */
    String value() default "EDEN_DEFAULT_CONFIG";   

	/**
	 * JSON-serializable type that wraps around a supplied Map of Strings and Objects
	 */
    public static class DefaultConfigWrapper
	{
		private final Map<String, Object> data;

		/**
		 * DefaultConfigWrapper constructor
		 * @param data Map data
		 */
		public DefaultConfigWrapper(Map<String, Object> data)
		{
			this.data = data;
		}

		/**
		 * Obtains the contained Map data
		 * @return The contained Map data
		 */
		public Map<String, Object> getData()
		{
			return this.data;
		}
	}
}
