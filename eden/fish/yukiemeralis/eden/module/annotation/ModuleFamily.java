package fish.yukiemeralis.eden.module.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.bukkit.Material;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleFamily 
{
    String name() default "Unknown";
    Material icon() default Material.WHITE_CONCRETE; 
}
