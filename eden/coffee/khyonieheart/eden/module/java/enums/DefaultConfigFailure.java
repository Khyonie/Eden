package coffee.khyonieheart.eden.module.java.enums;

/**
 * Enum that describes why a default config failed to load 
 */
public enum DefaultConfigFailure 
{
    /** No default configuration map found */
    NO_DEFAULT_CONFIG_FOUND,
    /** Default configuration map contains no entries */
    EMPTY_DEFAULT_CONFIG,
    /** Default configuration map is null, or not a map of Strings and Objects */
    INVALID_DEFAULT_CONFIG,
    /** An unknown error has occurred */
    UNKNOWN_ERROR
    ;
}
