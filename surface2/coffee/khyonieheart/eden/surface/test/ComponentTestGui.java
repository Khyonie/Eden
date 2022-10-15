package coffee.khyonieheart.eden.surface.test;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import coffee.khyonieheart.eden.surface.SimpleComponentBuilder;
import coffee.khyonieheart.eden.surface.SurfaceGui;
import coffee.khyonieheart.eden.surface.component.GuiItemStack;
import coffee.khyonieheart.eden.surface.enums.DefaultClickAction;

public class ComponentTestGui extends SurfaceGui 
{
    static GuiItemStack FISH_BUTTON = SimpleComponentBuilder.build(Material.SALMON, "§r§c§lA Salmon", (e) -> {
            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_SALMON_FLOP, 1.0f, 1.0f);
        },
        "§7§oYou've been hit by",
        "§7§oYou've been struck by"
    );

    public ComponentTestGui() 
    {
        super(54, "Component GUI", DefaultClickAction.CANCEL);
    }

    @Override
    public void init(HumanEntity e, InventoryView view)
    {
        paint(FISH_BUTTON);
    }
}
