package fish.yukiemeralis.eden.module.java.enums;

import fish.yukiemeralis.eden.module.java.ModuleDisableFailureData;

/**
 * Enum that describes why a module failed to be disabled
 */
public enum ModuleDisableFailure 
{
    /** Module is null */
    NULL_MODULE,
    /** Caller does not have authorization to disable module */
    UNAUTHORIZED_CALLERTOKEN,
    /** Dependent module failed to be disabled @see {@link ModuleDisableFailureData#getDownstreamModules()} */
    DOWNSTREAM_DISABLE_FAILURE,
    /** A java exception was thrown when disable was attempted */
    JAVA_ERROR,
    /** An unknown error has occurred */
    UNKNOWN_ERROR
    ;    
}
