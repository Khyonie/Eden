package fish.yukiemeralis.eden.surface2.component;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class GuiItemStack extends ItemStack implements GuiComponent 
{
    public GuiItemStack(ItemStack stack)
    {
        super(stack); 
    }

    public GuiItemStack(Material type)
    {
        super(type);   
    }

    public GuiItemStack(Material type, int amount)
    {
        super(type, amount);   
    }

    @Override
    public GuiItemStack generate()
    {
        return this;
    }
}
