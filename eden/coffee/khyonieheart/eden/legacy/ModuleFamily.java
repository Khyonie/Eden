package coffee.khyonieheart.eden.legacy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

/**
 * A simple module family registry, each with an icon and description.
 * @author Yuki_emeralis
 * @deprecated Module families are now registered automagically with hints given via annotation on a per-module basis.
 */
@Deprecated
public class ModuleFamily 
{
    private static final Map<String, Material> familyIcons = new HashMap<>();
    private static final Map<String, String> familyDescs = new HashMap<>();

    /**
     * Registers a module family.
     * @param name Family name
     * @param material Family material icon
     * @param description Family description
     */
    public static void registerFamily(String name, Material material, String description)
    {
        familyIcons.put(name, material);
        familyDescs.put(name, description);
    }

    /**
     * Obtains a material that represents a family.
     * @param familyName The family to get an icon from.
     * @return A material for the given family.
     */
    public static Material getIcon(String familyName)
    {
        return familyIcons.get(familyName);
    }

    /**
     * Obtains a description for a family.
     * @param familyName The family to get a description from.
     * @return A description of the given family.
     */
    public static String getDescription(String familyName)
    {
        return familyDescs.get(familyName);
    }

    /**
     * Obtains a set of all registered family's names.
     * @return A set of all registered family names.
     */
    public static Set<String> getAllFamilies()
    {
        return familyDescs.keySet();
    }
}
