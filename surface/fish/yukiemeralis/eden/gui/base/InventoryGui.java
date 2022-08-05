package fish.yukiemeralis.eden.gui.base;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fish.yukiemeralis.eden.module.annotation.HideFromCollector;
import fish.yukiemeralis.eden.utils.ItemUtils;

@Deprecated
@HideFromCollector
public abstract class InventoryGui implements Listener
{
    private final int invSize;
    private final String invName;

    private Inventory inv;

    private static final ItemStack black_glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE) {{
        ItemUtils.applyName(this, " ");
    }};

    public InventoryGui(int invSize, String invName)
    {
        this.invName = invName;
        this.invSize = invSize;

        inv = Bukkit.createInventory(null, invSize, invName);
    }

    public abstract void init();
    public abstract void onInteract(InventoryClickEvent event);

    public void display(HumanEntity target)
    {
        target.openInventory(inv);
    }

    public void paint()
    {
        for (int i = 0; i < this.inv.getSize(); i++)
            inv.setItem(i, getBlankIcon());
    }

    public Inventory getInventory()
    {
        return this.inv;
    }

    public int getInventorySize()
    {
        return this.invSize;
    }

    public String getInventoryName()
    {
        return this.invName;
    }

    public static ItemStack getBlankIcon()
    {
        return black_glass;
    }
}
