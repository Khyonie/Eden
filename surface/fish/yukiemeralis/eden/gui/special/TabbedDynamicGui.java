package fish.yukiemeralis.eden.gui.special;

import java.util.Map;

import fish.yukiemeralis.eden.gui.GuiComponent;
import fish.yukiemeralis.eden.gui.base.DynamicGui;
import fish.yukiemeralis.eden.module.annotation.HideFromCollector;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @deprecated Deprecated in favor of Surface2's TabbedSurfaceGui implementation.
 */
@HideFromCollector
@Deprecated
public class TabbedDynamicGui extends DynamicGui
{
    private final String currentTab;
    private final Map<String, GuiTab> tabData;
    private final int rowCount;
    private final int tabPage;

    public TabbedDynamicGui(int rowCount, int tabPage, String invName, Player player, Map<String, GuiTab> tabData, String currentTab, InventoryAction... allowedActions) 
    {
        super(9 + (rowCount * 9), invName, player, allowedActions);

        this.tabData = tabData;
        this.currentTab = currentTab;
        this.rowCount = rowCount;
        this.tabPage = tabPage;

        init();
    }

    public TabbedDynamicGui(int rowCount, String invName, Player player, Map<String, GuiTab> tabData, String currentTab, InventoryAction... allowedActions)
    {
        super(9 + (rowCount * 9), invName, player, allowedActions);

        this.tabData = tabData;
        this.currentTab = currentTab;
        this.rowCount = rowCount;
        this.tabPage = 0;

        init();
    }

    @Override
    public void init() 
    {
        paint();

        // OLDTODO Add "tab" paging for GUIs with more than 9 tabs

        // Set tabs
        int index = this.getInventorySize() - 9;
        for (GuiTab tab : tabData.values())
        {
            if (index >= this.getInventorySize())
                break;
            addComponent(index, tab);
            index++;
        }

        GuiTab tab = tabData.get(currentTab);

        if (tab == null)
            return;

        index = 0;
        for (GuiComponent component : tab.getComponents())
        {
            if (index > this.getInventorySize())
                break;
            this.addComponent(index, component);
            index++;
        }

        paintComponents();
    }

    @Override
    @EventHandler
    public void onInteract(InventoryClickEvent event) 
    {
        if (!isEventApplicable(event, true))
            return;

        TabbedDynamicGui gui = (TabbedDynamicGui) DynamicGui.getOpenedGuis().get((Player) event.getWhoClicked());

        // In a GUI like this, we only care about components
        if (gui.getComponents().containsKey(event.getRawSlot()))
            gui.getComponents().get(event.getRawSlot()).onIconInteract(event);
    }

    @Override
    protected boolean isEventApplicable(InventoryClickEvent event, boolean cancel)
    {
        if (event.getClickedInventory() == null)
            return false;

        if (!DynamicGui.getOpenedGuis().containsKey((Player) event.getWhoClicked()))
            return false;

        DynamicGui gui = DynamicGui.getOpenedGuis().get((Player) event.getWhoClicked());

        if (!gui.getInventoryName().equals(event.getView().getTitle()))
            return false;

        if (!(gui instanceof TabbedDynamicGui))
            return false;

        if (cancel)
            event.setCancelled(true);

        if (!gui.getAllowedActions().contains(event.getAction()))
            return false;

        return true;
    }

    public int getRowCount()
    {
        return this.rowCount;
    }

    public String getCurrentTab()
    {
        return this.currentTab;
    }

    public Map<String, GuiTab> getTabData()
    {
        return this.tabData;
    }

    public int getTabPage()
    {
        return this.tabPage;
    }
}
