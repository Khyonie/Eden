package fish.yukiemeralis.eden.module.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fish.yukiemeralis.eden.module.exception.InvalidStaticInitException;

/**
 * Annotation to denote that this class's static fields should be initialized on load according to sections 12.2, 12.3, and 12.4 of <i>The Java Language Specification.</i>
 * Eden classloader will attempt to call a static <code>void initStatic()</code> on all classes containing this annotation.<p>
 * 
 * A string can optionally be supplied to call a different static method instead.<p>
 * 
 * If no such static method can be found or invokation fails, a {@link InvalidStaticInitException} will be thrown, containing details of the error.<p>
 * 
 *<pre><code>
&#64;StaticInitialize
public class ClassExample
{
    static int value = 0;

    private static void initStatic()
    {
        // Static variable "value" will be initialized at this point
    }
}
 *</code></pre><p>
 *
 * @param value An optional String supplying the name of the static method to call. Defaults to "initStatic".
 * @see https://docs.oracle.com/javase/specs/jls/se18/html/jls-12.html
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StaticInitialize 
{
    String value() default "initStatic";
}
