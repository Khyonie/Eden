package fish.yukiemeralis.eden.module.java.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Annotation for modules that have an @EdenConfig annotation. Provides the keys and values for a default config. Default field mapping is <code>EDEN_DEFAULT_CONFIG</code>.
 * @Author Yuki_emeralis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultConfig
{
	String value() default "EDEN_DEFAULT_CONFIG";

	public static class DefaultConfigWrapper
	{
		private final Map<String, Object> data;

		public DefaultConfigWrapper(Map<String, Object> data)
		{
			this.data = data;
		}

		public Map<String, Object> getData()
		{
			return this.data;
		}
	}
}
