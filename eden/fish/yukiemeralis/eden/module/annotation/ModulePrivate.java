package fish.yukiemeralis.eden.module.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fish.yukiemeralis.eden.module.EdenModule;

/**
 * Protects a class from access unless the module is specified.
 * TODO This
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModulePrivate 
{
    Class<? extends EdenModule>[] accessors();
}
