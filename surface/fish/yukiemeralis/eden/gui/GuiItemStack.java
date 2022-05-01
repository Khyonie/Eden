package fish.yukiemeralis.eden.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fish.yukiemeralis.eden.utils.ItemUtils;

/**
 * @deprecated Deprecated in favor of Surface2 implementation
 */
@Deprecated
public abstract class GuiItemStack extends ItemStack implements GuiComponent
{
    public GuiItemStack(Material material)
    {
        super(material);
        ItemUtils.saveToNamespacedKey(this, "isGuiItemstack", "true");
    }

    public GuiItemStack(ItemStack itemstack)
    {
        super(itemstack);
        ItemUtils.saveToNamespacedKey(this, "isGuiItemstack", "true");
    }

    public GuiItemStack(Material material, int amount)
    {
        super(material, amount);
        ItemUtils.saveToNamespacedKey(this, "isGuiItemstack", "true");
    }

    @Override
    public abstract void onIconInteract(InventoryClickEvent event);

    @Override
    public GuiItemStack toIcon()
    {
        return this;
    }
}
