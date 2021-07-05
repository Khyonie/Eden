package com.yukiemeralis.blogspot.zenith.module.java.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Unimplemented 
{
    String value() default "This class is not finished.";
}
