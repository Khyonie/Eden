package coffee.khyonieheart.eden.recipe;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;

import coffee.khyonieheart.eden.utils.DataUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.DataUtils.KeyValuePair;
import coffee.khyonieheart.eden.utils.logging.Logger.InfoType;

public class RecipeManager 
{
    private static Map<NamespacedKey, Recipe> registeredRecipes = new HashMap<>(); 

    public static void addRecipe(NamespacedKey key, Recipe recipe, boolean register)
    {
        registeredRecipes.put(key, recipe);

        if (register)
            Bukkit.addRecipe(recipe);
    }

    public static void removeRecipe(NamespacedKey key)
    {
        if (registeredRecipes.containsKey(key))
            registeredRecipes.remove(key);

        if (Bukkit.getRecipe(key) != null)
        {
            Bukkit.removeRecipe(key);
            return;
        }

        PrintUtils.log("Attempted to remove unknown recipe \"" + key.getKey() + "\".", InfoType.WARN);
    }

    public static int registerAllRecipes()
    {
        int registered = 0;
        
        for (KeyValuePair<NamespacedKey, Recipe> pair : DataUtils.toKeyValuePairSet(registeredRecipes))
        {
            if (Bukkit.getRecipe(pair.getKey()) != null)
                continue;

            Bukkit.addRecipe(pair.getValue());
            registered++;
        }

        return registered;
    }

    public static void unregisterAllRecipes()
    {
        for (NamespacedKey key : registeredRecipes.keySet())
            Bukkit.removeRecipe(key);

        registeredRecipes.clear();
    }
}
