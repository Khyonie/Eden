package com.yukiemeralis.blogspot.eden.module.java.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a class to be ignored by the class collection process.
 * @author Yuki_emeralis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HideFromCollector {}