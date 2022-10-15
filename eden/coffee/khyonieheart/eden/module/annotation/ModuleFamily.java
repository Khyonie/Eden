package coffee.khyonieheart.eden.module.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.bukkit.Material;

import coffee.khyonieheart.eden.module.EdenModule.ModInfo;

/** 
 * Interface that designates a module as being part of a family 
 * @see {@link ModInfo}
*/
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleFamily 
{
    /** Module family name. */
    String name() default "Unknown";
    /** Module family icon, for use in GUIs. */
    Material icon() default Material.WHITE_CONCRETE; 
}
