package fish.yukiemeralis.eden.gui.base;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fish.yukiemeralis.eden.gui.GuiItemStack;
import fish.yukiemeralis.eden.module.java.annotations.HideFromCollector;
import fish.yukiemeralis.eden.utils.ItemUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@HideFromCollector
@Deprecated
public abstract class StaticGui extends InventoryGui
{
    protected Map<Integer, ItemStack> invElements;
    private final boolean paintBlack;
    private final List<InventoryAction> allowedActions;

    public StaticGui(int invSize, String invName, Map<Integer, ItemStack> invElements, boolean paintBlack, InventoryAction... allowedActions)
    {
        super(invSize, invName);
        this.invElements = invElements;
        this.paintBlack = paintBlack;

        this.allowedActions = Arrays.asList(allowedActions);

        init();
    }

    @Override
    public void init()
    {
        // Paint GUI black
        if (paintBlack)
            for (int i = 0; i < this.getInventorySize(); i++)
                this.getInventory().setItem(i, getBlankIcon());

        // Enter the GUI elements into the inventory
        invElements.forEach((slot, icon) -> {
            this.getInventory().setItem(slot, icon);
        });
    }

    @Override
    @EventHandler
    public void onInteract(InventoryClickEvent event)
    {
        if (event.getClickedInventory() == null)
            return;

        if (!event.getView().getTitle().equals(this.getInventoryName()))
            return;

        event.setCancelled(true);

        if (!this.getAllowedActions().contains(event.getAction()))
            return;

        if (this.invElements.containsKey(event.getRawSlot()))
            if (ItemUtils.hasNamespacedKey(event.getInventory().getItem(event.getRawSlot()), "isGuiItemstack"))
                ((GuiItemStack) invElements.get(event.getRawSlot())).onIconInteract(event);

    }

    public Map<Integer, ItemStack> getInventoryElements()
    {
        return this.invElements;
    }

    public List<InventoryAction> getAllowedActions()
    {
        return this.allowedActions;
    }
}
