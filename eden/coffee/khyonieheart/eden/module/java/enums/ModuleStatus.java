package fish.yukiemeralis.eden.module.java.enums;

/**
 * Enum describing all the possible states a module can be in
 */
public enum ModuleStatus 
{
    /** Module is loaded and enabled */
    ENABLED,
    /** Module is loaded and disabled */
    DISABLED,
    /** Module was previously loaded but not in memory anymore */
    UNLOADED,
    /** Module was never loaded. <b>This state is considered invalid.</b> */
    NOT_PRESENT
    ;    
}
