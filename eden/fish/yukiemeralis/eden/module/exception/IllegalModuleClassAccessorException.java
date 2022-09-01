package fish.yukiemeralis.eden.module.exception;

import fish.yukiemeralis.eden.module.EdenModule;

public class IllegalModuleClassAccessorException extends RuntimeException
{
    public IllegalModuleClassAccessorException(EdenModule accessor, Class<?> clazz)
    {
        super("Module " + accessor + " is not permitted to access module-private class " + clazz.getName());       
    }
}
