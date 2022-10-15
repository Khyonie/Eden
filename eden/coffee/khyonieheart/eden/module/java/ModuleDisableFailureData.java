package fish.yukiemeralis.eden.module.java;

import java.util.Collections;
import java.util.List;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.java.enums.ModuleDisableFailure;

/**
 * Collection class containing an unmodifiable list of EdenModules and an {@link ModuleDisableFailure} enum.
 */
public class ModuleDisableFailureData 
{
    private final List<EdenModule> list;
    private final ModuleDisableFailure reason;

    /**
     * @param list
     * @param reason
     */
    public ModuleDisableFailureData(List<EdenModule> list, ModuleDisableFailure reason)
    {
        this.list = Collections.unmodifiableList(list);
        this.reason = reason;
    }

    /**
     * Obtains an unmodifiable list of modules considered "downstream" by a given module
     * @return List of downstream modules
     */
    public List<EdenModule> getDownstreamModules()
    {
        return this.list;
    }

    /**
     * Attempts to perform a disable failure rollback by enabling the successfully disabled downstream modules.
     * @return Whether or not the rollback was successful
     */
    public boolean performRollback()
    {
        try {
            for (EdenModule m : list)
            {
                Eden.getModuleManager().enableModule(m);
                m.setEnabled();
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Obtains the contained {@link ModuleDisableFailure} enum.
     * @return Module disable failure enum
     */
    public ModuleDisableFailure getReason()
    {
        return this.reason;
    }
}
