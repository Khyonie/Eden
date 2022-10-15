package fish.yukiemeralis.eden.surface2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GuiUtils 
{
    public static ItemStack 
        BLACK_PANE = null,
        GREY_PANE = null; 
    public static GuiItemStack 
        BLACK_PANE_GUI = null,
        GREY_PANE_GUI = null; 

    private static Map<GuiComponent, GuiItemStack> ITEM_CACHE = new HashMap<>();

    public static boolean hasCachedComponent(GuiComponent component)
    {
        return ITEM_CACHE.containsKey(component);
    }

    public static GuiItemStack getCachedComponent(GuiComponent component)
    {
        return ITEM_CACHE.get(component);
    }

    public static List<? extends GuiComponent> of(GuiComponent... components)
    {
        return Arrays.asList(components);
    }

    public static List<? extends GuiComponent> of(Collection<? extends GuiComponent> components)
    {
        return new ArrayList<>(components);
    }

    public static Map<Integer, ItemStack> generateBaseGui(int size, ItemStack component) throws IllegalArgumentException
    {
        if (size <= 0 || size % 9 != 0)
            throw new IllegalArgumentException("Inventory size cannot less than or equal to 0, and must be a multiple of 9.");

        Map<Integer, ItemStack> data = new HashMap<>();

        for (int i = 0; i < size; i++)
            data.put(i, component.clone());

        return data;
    }

    public static Map<Integer, ItemStack> generateItemRectangle(int x1, int y1, int x2, int y2, ItemStack component)
    {
        Map<Integer, ItemStack> data = new HashMap<>();
        
        for (int y = y1; y < y2; y++)
            for (int x = x1; x < x2; x++)
                data.put((y * 9) + x, component.clone());
        
        return data;
    }

    public static Map<Integer, GuiComponent> generateComponentRectangle(int x1, int y1, int x2, int y2, GuiComponent component)
    {
        Map<Integer, GuiComponent> data = new HashMap<>();

        for (int y = y1; y < y2; y++)
            for (int x = x1; x < x2; x++)
                data.put((y * 9) + x, component);

        return data;
    }

    static {
        BLACK_PANE = ItemUtils.build(Material.BLACK_STAINED_GLASS_PANE, "§r");
        BLACK_PANE_GUI = new GuiItemStack(BLACK_PANE) {};

        GREY_PANE = ItemUtils.build(Material.GRAY_STAINED_GLASS_PANE, "§r");
        GREY_PANE_GUI = new GuiItemStack(GREY_PANE) {};
    }
}
