package com.yukiemeralis.blogspot.zenith.module.java.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.yukiemeralis.blogspot.zenith.module.java.enums.BranchType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Branch 
{
    BranchType value() default BranchType.RELEASE;
}
