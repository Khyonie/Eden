package fish.yukiemeralis.eden.surface2.special;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.InventoryView;

import fish.yukiemeralis.eden.surface2.GuiUtils;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.utils.PrintUtils;

public class PagedSurfaceGui extends SurfaceGui 
{
    private int page = 0;
    private List<? extends GuiComponent> components;
    private List<? extends GuiComponent> topBarComponents;
    private int row = 0; // Row where the top bar will generate

    private static final GuiItemStack NEXT_PAGE_ITEM = SimpleComponentBuilder.build(Material.PAPER, "§r§f§lNext page", (e) -> {
            PagedSurfaceGui gui = (PagedSurfaceGui) SurfaceGui.getOpenGui(e.getWhoClicked()).unwrap(SurfaceGui.class);    
            gui.goToPage(e.getWhoClicked(), gui.getPage() + 1);
        },
        "§7§oOpens the next page." 
    );

    private static final GuiItemStack BACK_PAGE_ITEM = SimpleComponentBuilder.build(Material.MAP, "§r§f§lPrevious page", (e) -> {
            PagedSurfaceGui gui = (PagedSurfaceGui) SurfaceGui.getOpenGui(e.getWhoClicked()).unwrap(SurfaceGui.class);    
            gui.goToPage(e.getWhoClicked(), gui.getPage() - 1);
        },
        "§7§oOpens the previous page."
    );

    public PagedSurfaceGui(int size, String title, HumanEntity target, int topBarRow, List<? extends GuiComponent> components, DefaultClickAction defaultAction, InventoryAction... allowedClickActions) 
    {
        this(size, title, target, topBarRow, components, new ArrayList<>(), defaultAction, allowedClickActions);
    }

    public PagedSurfaceGui(int size, String title, HumanEntity target, int topBarRow, List<? extends GuiComponent> components, List<? extends GuiComponent> topBarComponents, DefaultClickAction defaultAction, InventoryAction... allowedClickActions) 
    {
        super(size, title, defaultAction, allowedClickActions);
        this.topBarComponents = topBarComponents;
        this.components = components;
        this.row = topBarRow;
    }

    @Override
    public void init(HumanEntity e, InventoryView view) 
    {
        generatePage(e);
    }

    public int getPage()
    {
        return this.page;
    }

    private void generatePage(HumanEntity e)
    {
        // Clean target area
        paintBlack();

        // Embed top bar
        for (int i = 0; i < 9; i++)
            updateSingleItem(e, (row * 9) + i, GuiUtils.GREY_PANE_GUI, false);

        for (int i = 0; i < 7; i++)
        {
            if (i == topBarComponents.size())
                break;

            updateSingleComponent(e, (row * 9) + i, topBarComponents.get(i));
        }

        if (page != 0)
            updateSingleComponent(e, (row * 9) + 7, BACK_PAGE_ITEM);

        PrintUtils.log("Page " + page + " * \\(" + this.getSize() + " - 9\\) = " + (page * (this.getSize() - 9)) + " | " + components.size());
        if (page * (this.getSize() - 9) < components.size())
        {
            updateSingleComponent(e, (row * 9) + 8, NEXT_PAGE_ITEM);
        }

        generateListData(e);
    }

    public void generateListData(HumanEntity e)
    {
        int index = page * (this.getSize() - 9);
        for (int i = 0; i < this.getSize(); i++)
        {
            if (i / 9 == row)
                continue;

            if (index == components.size())
                break;    
            
            updateSingleComponent(e, i, components.get(index));
            index++;
        }
    }

    private void goToPage(HumanEntity e, int page)
    {
        this.page = page;
        generatePage(e);
    } 
}
