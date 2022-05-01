package fish.yukiemeralis.eden.surface2.test;

import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

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

        paint(FISH_BUTTON);
    }
}
