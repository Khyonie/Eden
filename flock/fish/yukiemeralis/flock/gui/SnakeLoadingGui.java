package fish.yukiemeralis.flock.gui;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.surface2.GuiUtils;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.utils.ItemUtils;
import fish.yukiemeralis.flock.gui.snake.SnakeBearing;

/**
 * A silly GUI that randomly plays Snake. Used as an entertaining alternative to a generic loading screen when updating the repository list.
 */
public class SnakeLoadingGui extends SurfaceGui
{
    private ItemStack snakeBodyItem = ItemUtils.build(Material.LIME_STAINED_GLASS_PANE, "§a§lFrank", "§7§oFrank is helping update your", "§7§orepositories. If Frank is taking a", "§7§owhile, feel free to exit and", "§7§otry again later.");
    private ItemStack snakeHeadItem = ItemUtils.build(Material.GREEN_STAINED_GLASS_PANE, "§a§lFrank", "§7§oFrank is helping update your", "§7§orepositories. If Frank is taking a", "§7§owhile, feel free to exit and", "§7§otry again later.");
    private static ItemStack fruitItem = ItemUtils.build(Material.APPLE, "§r");
    
    private static final Random random = new Random();

    public SnakeLoadingGui() 
    {
        super(54, "Updating repositories...", DefaultClickAction.CANCEL);
        paintBlack();
    }

    private int 
        snakeX = random.nextInt(9), 
        snakeY = random.nextInt(6),
        snakeLength = 5,
        targetX = random.nextInt(9),
        targetY = random.nextInt(6);

    private SnakeBearing bearing = SnakeBearing.WEST;

    private int[][] snakeBody = new int[54][2];
    private int[] previousPosition = new int[2];

    @Override
    public void init(HumanEntity e, InventoryView view) {}

    private BukkitTask runnable;
    @Override
    public void onGuiOpen(HumanEntity e, InventoryView view)
    {
        runnable = new BukkitRunnable() {
            @Override
            public void run() 
            {
                update();
                render(e, view);   
            }
        }.runTaskTimer(Eden.getInstance(), 0L, 2L);
    }

    @Override
    public void onGuiClose(HumanEntity e, InventoryView view)
    {
        runnable.cancel();
    }

    private void render(HumanEntity e, InventoryView view)
    {
        // Render fruit
        updateSingleItem(e, (targetY * 9) + targetX, fruitItem, false);

        // Render snake        
        for (int i = 0; i < snakeLength; i++) // Body
            updateSingleItem(e, (snakeBody[i][1] * 9) + snakeBody[i][0], snakeBodyItem, false);
        updateSingleItem(e, (snakeY * 9) + snakeX, snakeHeadItem, false); // Head

        // Derender previous position
        updateSingleItem(e, (previousPosition[1] * 9) + previousPosition[0], GuiUtils.BLACK_PANE, false);
    }
    
    private void update()
    {
        // Move snake body
        previousPosition = snakeBody[snakeLength - 1];
        int[][] newPositions = new int[54][2];

        for (int i = 0; i < 53; i++)
            newPositions[i + 1] = snakeBody[i];
        snakeBody = newPositions;

        snakeBody[0] = new int[] {snakeX, snakeY};
        
        // Decide if we should update bearing
        if (random.nextInt(5) == 0)
            bearing = SnakeBearing.getRandomValidBearing(bearing);

        // Update position
        if (bearing.isHorizontal())
        {
            snakeX += bearing.getModifier();
        } else {
            snakeY += bearing.getModifier();
        }

        // Wrap
        if (snakeX == 9)
            snakeX = 0;
        if (snakeX == -1)
            snakeX = 8;
        if (snakeY == 6)
            snakeY = 0;
        if (snakeY == -1)
            snakeY = 5;

        // Check if we're overlapping target
        if (snakeX == targetX && snakeY == targetY)
        {
            snakeLength++;
            targetX = random.nextInt(9);
            targetY = random.nextInt(6);
        }
    }
}
