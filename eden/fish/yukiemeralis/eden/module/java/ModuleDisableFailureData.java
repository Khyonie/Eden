package fish.yukiemeralis.eden.module.java;

import java.util.List;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.java.enums.ModuleDisableFailure;

public class ModuleDisableFailureData 
{
    private final List<EdenModule> list;
    private final ModuleDisableFailure reason;

    public ModuleDisableFailureData(List<EdenModule> list, ModuleDisableFailure reason)
    {
        this.list = list;
        this.reason = reason;
    }

    public List<EdenModule> getDownstreamModules()
    {
        return this.list;
    }

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

    public ModuleDisableFailure getReason()
    {
        return this.reason;
    }
}
