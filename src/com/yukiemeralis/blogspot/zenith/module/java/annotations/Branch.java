package com.yukiemeralis.blogspot.zenith.module.java.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.yukiemeralis.blogspot.zenith.module.java.enums.BranchType;

@Retention(RetentionPolicy.RUNTIME)
public @interface Branch 
{
    BranchType value() default BranchType.RELEASE;
}
