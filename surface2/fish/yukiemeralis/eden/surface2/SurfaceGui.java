package fish.yukiemeralis.eden.surface2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.utils.Option;

public class SurfaceGui implements ISurfaceGui
{
    private static Map<HumanEntity, SurfaceGui> OPEN_GUIS = new HashMap<>();
 
    private Inventory host;
    private Map<HumanEntity, Map<Integer, GuiComponent>> data = new HashMap<>();

    private final int size;
    private final String title;
    private final DefaultClickAction defaultAction;
    private final List<InventoryAction> allowedClickActions;

    public SurfaceGui(int size, String title, DefaultClickAction defaultAction, InventoryAction... allowedClickActions)
    {
        host = Bukkit.createInventory(null, size, title);
        this.size = size;
        this.title = title;
        this.defaultAction = defaultAction;
        this.allowedClickActions = Arrays.asList(allowedClickActions);
    }

    public void paintBlack()
    {
        paint(GuiUtils.BLACK_PANE);
    }

    /**
     * Paints the base inventory with an itemstack. This change affects all current and future inventoryviews derived from this one.
     * <p>Analagous to <code>updateAlldataItem(GuiUtils.generateBaseGui(size, item), true);</code>
     * @param i Itemstack to paint with.
     */
    public void paint(ItemStack i)
    {
        updateAllDataItem(GuiUtils.generateBaseGui(size, i), true);
    }

    //
    // Data setters
    // Component - Per-user items that contain a related action, which is executed upon interaction
    // Item - Item with no java-related interaction
    //

    /**
     * Embeds a single item, with no defined action, into a GUI viewer's view. If the slot being embedded into
     * previously contained a GuiComponent, that component will be erased.<p>
     * If persistent, the host inventory will be modified along with the target's view, thus affecting all new GUIs
     * derived from this one.
     * @param e The target GUI viewer.
     * @param slot The slot to embed an item into.
     * @param item The item to be embedded.
     * @param persistent Whether or not to modify the host inventory.
     */
    public void updateSingleItem(HumanEntity e, int slot, ItemStack item, boolean persistent)
    {
        if (e != null)
        {
            view(e).setItem(slot, item);
            data.get(e).remove(slot);
        }

        if (persistent)
            embdedInHost(slot, item);
    }

    /**
     * Embeds a GuiComponent with a defined action into a GUI viewer's view.
     * @param e The target GUI viewer.
     * @param slot The slot in which to embed a component into.
     * @param component The component to be embedded.
     */
    public void updateSingleComponent(HumanEntity e, int slot, GuiComponent component)
    {
        view(e).setItem(slot, component.generate());
        data.get(e).put(slot, component);
    }

    /**
     * Embeds a single item, with no defined action, into all currently opened surface2 GUIs of this type. If the slot being embedded
     * into previously contained a GuiComponent, that component will be erased on a per-player basis.
     * If persistent, the host inventory will be modified along with the target's view, thus affecting all new GUIs
     * derived from this one.
     * @param slot The slot to embed an item into.
     * @param item The item to be embedded.
     * @param persistent Whether or not to modify the host inventory.
     */
    public void updateAllItem(int slot, ItemStack item, boolean persistent)
    {
        getSurfaceViewers().forEach(e -> updateSingleItem(e, slot, item, persistent));
    }

    /**
     * Embeds a GuiComponent with a defined action into all GUI viewer's views of this GUI type.
     * @param slot The slot in which to embed a component into.
     * @param component The component to be embedded.
     */
    public void updateAllComponent(int slot, GuiComponent component)
    {
        getSurfaceViewers().forEach(e -> updateSingleComponent(e, slot, component));
    }

    /**
     * Embeds a map of slots and items, with no defined actions, into a GUI viewer's view. For each slot, if the slot being embedded
     * into previously contained a GuiComponent, that component will be erased.
     * If persistent, the host inventory will be modified along with the target's view, thus affecting all new GUIs
     * derived from this one.
     * @param e The target GUI viewer.
     * @param data A map of slots and items to embed.
     * @param persistent Whether or not to modify the host inventory.
     */
    public void updateSingleDataItem(HumanEntity e, Map<Integer, ItemStack> data, boolean persistent)
    {
        data.forEach((slot, item) -> updateSingleItem(e, slot, item, persistent));
    }

    /**
     * Embeds a map of slots and components with defined actions, into a GUI viewer's view.
     * @param e The target GUI viewer.
     * @param data
     */
    public void updateSingleDataComponent(HumanEntity e, Map<Integer, GuiComponent> data)
    {
        data.forEach((slot, component) -> updateSingleComponent(e, slot, component));
    }


    /**
     * Embeds a map of slots and items, with no defined actions, into all currently opened surface2 GUIs of this type. For each slot, if the slot being embedded
     * into previously contained a GuiComponent, that component will be erased.
     * If persistent, the host inventory will be modified along with the target's view, thus affecting all new GUIs
     * derived from this one.
     * @param data
     * @param persistent
     */
    public void updateAllDataItem(Map<Integer, ItemStack> data, boolean persistent)
    {
        this.data.forEach((e, map) -> updateSingleDataItem(e, data, persistent));
    }

    /**
     * Embeds a map of slots and components with defined actions, into all currently opened surface2 GUIs of this type.
     * @param data
     */
    public void updateAllDataComponent(Map<Integer, GuiComponent> data)
    {
        this.data.forEach((e, map) -> updateSingleDataComponent(e, data));
    }

    /**
     * Tiny method to embed an itemstack into a host inventory.
     * @param slot
     * @param item
     */
    private void embdedInHost(int slot, ItemStack item)
    {
        host.setItem(slot, item);
    }

    /**
     * Obtains the InventoryView in use by the target.
     * @param e The human target.
     * @return The InventoryView in use by the target.
     */
    protected InventoryView view(HumanEntity e)
    {
        return e.getOpenInventory();
    }

    /**
     * Obtains a list of all InventoryViews <i>pertaining to this exact instance of a Surface2 GUI</i>.
     * @return A list of all InventoryViews derived from this GUI's host.
     */
    protected List<InventoryView> allViews()
    {
        List<InventoryView> invData = new ArrayList<>();

        for (HumanEntity e : data.keySet())
            invData.add(e.getOpenInventory());

        return invData;
    }

    /**
     * Obtains a list of all players currently viewing this GUI.
     * @return A list of all players currently viewing this GUI.
     */
    protected List<HumanEntity> getSurfaceViewers()
    {
        return new ArrayList<>(data.keySet());
    }

    public int getSize()
    {
        return this.size;
    }

    public String getTitle()
    {
        return this.title;
    }

    public DefaultClickAction getDefaultClickAction()
    {
        return this.defaultAction;
    }

    public List<InventoryAction> getAllowedClickActions()
    {
        return this.allowedClickActions;
    }

    public Map<HumanEntity, Map<Integer, GuiComponent>> getData()
    {
        return this.data;
    }

    public ItemStack getViewItemAt(HumanEntity e, int slot)
    {
        return view(e).getItem(slot);
    }

    public Map<Integer, GuiComponent> getData(HumanEntity e)
    {
        return OPEN_GUIS.get(e).getData().get(e);
    }

    public static void setClosed(HumanEntity e)
    {
        OPEN_GUIS.remove(e);
    }

    public static Option<SurfaceGui> getOpenGui(HumanEntity e)
    {
        Option<SurfaceGui> option = new Option<>(SurfaceGui.class);

        return OPEN_GUIS.containsKey(e) ? option.some(OPEN_GUIS.get(e)) : option.none();
    }

    /**
     * Opens an inventory to the player, returning a handle to the inventoryview.<p>
     * Note: If you override this method but fail to place the target and GUI inside the <code>OPEN_GUIS</code> map, your GUI may be unresponsive.
     * @param target The HumanEntity to display this GUI to.
     * @return A handle to the opened inventory view.
     */
    public InventoryView display(HumanEntity target)
    {
        InventoryView view = target.openInventory(host);
        
        OPEN_GUIS.put(target, this);
        data.put(target, new HashMap<>());
        onGuiOpen(target, view);

        return view;
    }
}