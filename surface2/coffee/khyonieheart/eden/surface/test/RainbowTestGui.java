package coffee.khyonieheart.eden.surface.test;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.surface.SurfaceGui;
import coffee.khyonieheart.eden.surface.enums.DefaultClickAction;

public class RainbowTestGui extends SurfaceGui
{
    public RainbowTestGui() 
    {
        super(54, "Rainbow GUI", DefaultClickAction.CANCEL);
    } 

    @Override
    public void init(HumanEntity e, InventoryView view) { }

    private static ItemStack[] COLORS = {
        new ItemStack(Material.RED_STAINED_GLASS_PANE),
        new ItemStack(Material.ORANGE_STAINED_GLASS_PANE),
        new ItemStack(Material.YELLOW_STAINED_GLASS_PANE),
        new ItemStack(Material.LIME_STAINED_GLASS_PANE),
        new ItemStack(Material.GREEN_STAINED_GLASS_PANE),
        new ItemStack(Material.CYAN_STAINED_GLASS_PANE),
        new ItemStack(Material.BLUE_STAINED_GLASS_PANE),
        new ItemStack(Material.PURPLE_STAINED_GLASS_PANE),
        new ItemStack(Material.PINK_STAINED_GLASS_PANE)
    };

    int timeDelta = 0;
    BukkitTask runnable;
    @Override
    public void onGuiOpen(HumanEntity target, InventoryView view)
    {
        runnable = new BukkitRunnable() {
            @Override
            public void run() 
            {           
                for (int i = 0; i < 54; i++)
                {
                    int id = (i % 9 + (i / 9) + timeDelta) % 9;
                    view(target).setItem(i, COLORS[id]);  
                }

                timeDelta++;
            }
        }.runTaskTimer(Eden.getInstance(), 1L, 2L);
    }

    @Override
    public void onGuiClose(HumanEntity e, InventoryView view) 
    {
        runnable.cancel();
    }
}
