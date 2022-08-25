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
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.utils.option.Option;

public abstract class SurfaceGui implements ISurfaceGui
{
    private static Map<HumanEntity, SurfaceGui> OPEN_GUIS = new HashMap<>();
 
    private Inventory host;
    private Map<HumanEntity, Map<Integer, GuiComponent>> data = new HashMap<>();

    private final int size;
    private String title;
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
     * @param i Itemstack to paint with.
     */
    public void paint(ItemStack i)
    {
        embedDataInHost(GuiUtils.generateBaseGui(size, i));
    }

    public void rename(String name)
    {
        // Regenerate inventory
        Inventory backup = host;
        this.title = name;
        host = Bukkit.createInventory(null, size, title);

        // Copy server-side
        host.addItem(backup.getContents());
        
        // Copy client-side
        List<HumanEntity> viewers = new ArrayList<>(data.keySet());

        // Items
        Map<HumanEntity, Map<Integer, ItemStack>> backupItem = new HashMap<>();
        for (HumanEntity e : viewers)
        {
            backupItem.put(e, new HashMap<>());
            for (int i = 0; i < size; i++)
                backupItem.get(e).put(i, e.getOpenInventory().getItem(i));
        }

        // Components
        Map<HumanEntity, Map<Integer, GuiComponent>> backupData = new HashMap<>(data);        

        // Clean component data...
        data.clear();

        // ... And reapply it.
        for (HumanEntity e : viewers)
        {
            display(e, false);
            updateSingleDataItem(e, backupItem.get(e), false);
            updateSingleDataComponent(e, backupData.get(e));
        }
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
        if (persistent)
        {
            embedInHost(slot, item);
        }

        if (e != null)
        {
            initData(e);
            view(e).setItem(slot, item);

            data.get(e).remove(slot);
        }
    }

    /**
     * Embeds a GuiComponent with a defined action into a GUI viewer's view.
     * @param e The target GUI viewer.
     * @param slot The slot in which to embed a component into.
     * @param component The component to be embedded.
     */
    public void updateSingleComponent(HumanEntity e, int slot, GuiComponent component)
    {
        GuiItemStack comp = component.generate();
        view(e).setItem(slot, comp);

        initData(e);
        data.get(e).put(slot, comp);
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
        data.forEach((slot, item) -> {
            updateSingleItem(e, slot, item, persistent);
        });
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
        this.data.forEach((e, map) -> {
            updateSingleDataItem(e, data, persistent);
        });
    }

    /**
     * Embeds a map of slots and components with defined actions, into all currently opened surface2 GUIs of this type.
     * @param data
     */
    public void updateAllDataComponent(Map<Integer, GuiComponent> data)
    {
        this.data.forEach((e, map) -> updateSingleDataComponent(e, data));
    }

    private void embedDataInHost(Map<Integer, ItemStack> data)
    {
        data.forEach((slot, item) -> embedInHost(slot, item));
    }

    /**
     * Tiny method to safely embed an itemstack into a host inventory.
     * @param slot
     * @param item
     */
    private void embedInHost(int slot, ItemStack item)
    {
        if (host.getItem(slot) != null)
        {
            if (host.getItem(slot).equals(item))
                return;
        }
        
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
     * Obtains the InventoryView in use by a player in a Surface2 GUI.
     * Returns null if the player is not in a Surface2 GUI. 
     * @param e The human target.
     * @return The InventoryView in use by the target.
     */
    protected InventoryView surfaceView(HumanEntity e)
    {
        if (OPEN_GUIS.containsKey(e))
            return view(e);
        return null;
    }

    public boolean isInSurfaceGui(HumanEntity e)
    {
        return OPEN_GUIS.containsKey(e);
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

    public static Option getOpenGui(HumanEntity e)
    {
        return OPEN_GUIS.containsKey(e) ? Option.some(OPEN_GUIS.get(e)) : Option.none();
    }

    public InventoryView display(HumanEntity target)
    {
        return display(target, true);
    }

    /**
     * Opens an inventory to the player, returning a handle to the inventoryview.<p>
     * @param target The HumanEntity to display this GUI to.
     * @return A handle to the opened inventory view.
     */
    public InventoryView display(HumanEntity target, boolean init)
    {
        // Clean up a little
        if (isInSurfaceGui(target))
        {
            getOpenGui(target).unwrap(SurfaceGui.class).onGuiClose(target, view(target));
            OPEN_GUIS.remove(target);
        }

        // Generate a new view
        InventoryView view = target.openInventory(host);

        // Make the GUI actionable
        OPEN_GUIS.put(target, this);

        // Perform abstract initialization
        if (init)
        {
            init(target, view);
            onGuiOpen(target, view);
        }
        

        return view;
    }

    private void initData(HumanEntity target)
    {
        if (data.containsKey(target))
            return;

        data.put(target, new HashMap<>());
    }
}