package fish.yukiemeralis.eden.surface2.special;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;

import fish.yukiemeralis.eden.surface2.GuiUtils;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.component.GuiTab;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.utils.DataUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;

public class TabbedSurfaceGui extends SurfaceGui
{
    private int tabRow;

    @SuppressWarnings("unused")
    private int dataPage = 0; // TODO Decide an implementation for this

    private int tabPage = 0;
    private List<GuiTab> tabData;

    private static final GuiItemStack NEXT_TAB_SECTION = SimpleComponentBuilder.build(Material.PAPER, "§r§f§lNext page", (e) -> {
            TabbedSurfaceGui gui = (TabbedSurfaceGui) SurfaceGui.getOpenGui(e.getWhoClicked()).unwrap();
            gui.updateTabLine(e.getWhoClicked(), gui.getTabPage() + 1); 
        }, 
        "§7§oNavigates to next tabpage."
    );

    private static final GuiItemStack PREVIOUS_TAB_SECTION = SimpleComponentBuilder.build(Material.MAP, "§r§f§lPrevious page", (e) -> {
            TabbedSurfaceGui gui = (TabbedSurfaceGui) SurfaceGui.getOpenGui(e.getWhoClicked()).unwrap();
            gui.updateTabLine(e.getWhoClicked(), gui.getTabPage() - 1); 
        }, 
        "§7§oNavigates to previous tabpage."
    );

    /**
     * A special GUI type that seperates lists of components into tabs.<p>
     * Note: This GUI is designed so that one instance can be used per player. More than one instance may allow both players to interact with the same GUI at the same time.
     * @param size
     * @param title
     * @param tabRow
     * @param tabData
     * @param defaultAction
     * @param allowedClickActions
     */
    public TabbedSurfaceGui(int size, String title, HumanEntity target, int tabRow, List<GuiTab> tabData, DefaultClickAction defaultAction, InventoryAction... allowedClickActions) 
    {
        super(size, title, defaultAction, allowedClickActions);
        this.tabRow = tabRow;
        this.tabData = tabData;

        paintBlack();
        if (tabData.size() == 0)
        {
            PrintUtils.log("Attempted to initialized tabbed GUI with an empty dataset! This may be a bug, please open an issue, especcially if it looks like an Eden process is involved.", InfoType.WARN);
            PrintUtils.log("Call stack snippet: " + DataUtils.getPreviousCaller(Thread.currentThread()), InfoType.WARN);
            return;
        }

        updateTabLine(target, 0);
        changeTab(target, tabData.get(0));
    }

    public int getTabPage()
    {
        return this.tabPage;
    }

    public int getTabRow()
    {
        return this.tabRow;
    }

    public void setTabRow(int row) throws IllegalArgumentException
    {
        if (row >= this.getSize() / 9)
            throw new IllegalArgumentException("Row must fit inside the inventory. (Min: 0, max: " + ((this.getSize() / 9) - 1) + ", given: " + row + ")");

        this.tabRow = row;
    }

    public void updateTabLine(HumanEntity e, int page)
    {
        this.tabPage = page;
        // Repaint tab line
        for (int i = 0; i < 9; i++)
        {
            updateSingleItem(e, (tabRow * 9) + i, GuiUtils.GREY_PANE_GUI, false);
        }

        if (page == 0)
        {
            if (tabData.size() > 8)
                updateSingleComponent(e, (tabRow * 9) + 8, NEXT_TAB_SECTION);

            for (int i = 0; i < 8; i++)
            {
                if (i == tabData.size())
                    break;
                        
                updateSingleComponent(e, (tabRow * 9) + i, tabData.get(i));
            }

            return;
        }

        updateSingleComponent(e, tabRow * 9, PREVIOUS_TAB_SECTION);
        int seenIndex = 8 + ((page - 1) * 7);
        for (int i = 0; i < 7; i++)
        {
            if (tabData.size() == seenIndex + i)
                break;
            
            updateSingleComponent(e, (tabRow * 9) + i + 1, tabData.get(seenIndex + i));
        }

        if (tabData.size() - seenIndex > 7)
            updateSingleComponent(e, (tabRow * 9) + 8, NEXT_TAB_SECTION);
    }
    
    public void changeTab(HumanEntity e, GuiTab tab)
    {
        // Repaint
        int index = 0;
        for (int i = 0; i < getSize(); i++)
        {
            if (i / 9 == tabRow)
                continue;

            if (index < tab.getData().size())
            {
                updateSingleComponent(e, i, tab.getData().get(index));
            } else {
                updateSingleItem(e, i, GuiUtils.BLACK_PANE_GUI, false);
            }
            index++;
        }
    }
}
