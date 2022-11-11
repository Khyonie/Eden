package coffee.khyonieheart.eden.module.exception;

import coffee.khyonieheart.eden.module.EdenModule;

/**
 * Exception that is thrown when a class inside a module that is not permitted to access a resource attempts to access
 * such a resource.
 * @since 1.7.3
 * @author Khyonie
 */
public class IllegalModuleClassAccessorException extends RuntimeException
{
    /**
     * Constructor that takes the module that attempted to access a class, and the class itself
     * @param accessor Attempted class accessor
     * @param clazz The accessed class
     */
    public IllegalModuleClassAccessorException(EdenModule accessor, Class<?> clazz)
    {
        super("Module " + accessor + " is not permitted to access module-private class " + clazz.getName());       
    }
}
