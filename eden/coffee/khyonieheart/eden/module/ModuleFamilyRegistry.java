package coffee.khyonieheart.eden.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import coffee.khyonieheart.eden.module.annotation.ModuleFamily;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.logging.Logger.InfoType;

public class ModuleFamilyRegistry 
{
    private static final Map<String, ModuleFamilyEntry> data = new HashMap<>();
    
    public static void register(String name, Material mat)
    {
        if (!data.containsKey(name))
        {
            PrintUtils.logVerbose("Registered new family \"" + name + "\" with icon " + mat.name() + ".", InfoType.INFO);
            data.put(name, new ModuleFamilyEntry(name, mat, new ArrayList<>()));
        }
    }

    public static void register(EdenModule m)
    {
        if (!m.getClass().isAnnotationPresent(ModuleFamily.class))
            throw new IllegalArgumentException("Eden module \"" + m.getName() + "\" does not contain the @ModuleFamily annotation.");

        register(m.getClass().getAnnotation(ModuleFamily.class));
        data.get(m.getClass().getAnnotation(ModuleFamily.class).name()).addEntry(m);
    }

    public static void register(ModuleFamily family)
    {
        register(family.name(), family.icon());
    }

    public static ModuleFamilyEntry getFamily(String name)
    {
        return data.get(name);
    }

    public static ModuleFamilyEntry getFamily(EdenModule m) throws IllegalArgumentException
    {
        if (!m.getClass().isAnnotationPresent(ModuleFamily.class))
            throw new IllegalArgumentException("Eden module \"" + m.getName() + "\" does not contain the @ModuleFamily annotation.");

        return data.get(m.getClass().getAnnotation(ModuleFamily.class).name());
    }

    public static void unregister(EdenModule m) throws IllegalArgumentException
    {
        if (!m.getClass().isAnnotationPresent(ModuleFamily.class))
            throw new IllegalArgumentException("Eden module \"" + m.getName() + "\" does not contain the @ModuleFamily annotation.");

        data.get(m.getClass().getAnnotation(ModuleFamily.class).name()).removeEntry(m);
    }

    public static List<ModuleFamilyEntry> getAllFamilies()
    {
        return Collections.unmodifiableList(new ArrayList<>(data.values()));
    }

    public static class ModuleFamilyEntry
    {
        private final String name;
        private final Material mat;
        private final List<EdenModule> data;

        ModuleFamilyEntry(String name, Material mat, List<EdenModule> data)
        {
            this.name = name;
            this.mat = mat;
            this.data = data;
        }

        public Material getMaterial()
        {
            return this.mat;
        }

        public List<EdenModule> getData()
        {
            return this.data;
        }

        public String getName()
        {
            return this.name;
        }

        public void addEntry(EdenModule m)
        {
            data.removeIf((mod) -> {
                if (mod.getName().equals(m.getName()))
                {
                    PrintUtils.logVerbose("Removed previous entry of \"" + mod.getName() + "\" in family " + name, InfoType.INFO);
                    return true;
                }
                return false;
            });

            PrintUtils.logVerbose("Successfully registered " + m.getName() + " to family " + name, InfoType.INFO);
            data.add(m);
        }

        public void removeEntry(EdenModule m)
        {
            data.removeIf((mod) -> {
                if (mod.getName().equals(m.getName()))
                {
                    PrintUtils.logVerbose("Removed entry of " + m.getName() + " from family " + name , InfoType.INFO);
                    return true;
                }
                return false;
            });
        }
    }
}
