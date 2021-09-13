package com.yukiemeralis.blogspot.zenith.module.java.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for modules that have an @ZenConfig annotation. Provides the keys and values for a default config.
 * @Author Yuki_emeralis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultConfig
{
	/** */
	String[] keys();
	/** */
	String[] values();
}
