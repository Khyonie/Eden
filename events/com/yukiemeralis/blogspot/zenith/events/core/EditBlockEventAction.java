package com.yukiemeralis.blogspot.zenith.events.core;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.zenith.events.Cleanable;
import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;
import com.yukiemeralis.blogspot.zenith.utils.ChatUtils;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EditBlockEventAction extends ZenithEvent implements Cleanable
{
    private Material newMaterial, oldMaterial;
    private Block block;

    public EditBlockEventAction(Block target, Material newMaterial)
    {
        super("Edit block", "Edits a block in the world.", Material.GRASS_BLOCK);

        this.newMaterial = newMaterial;
        if (target != null)
            this.oldMaterial = target.getType();
        this.block = target;
    }

    @Override
    public GuiItemStack toIcon()
    {
        GuiItemStack item = new GuiItemStack(this.getMaterial())
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                
            }
        };

        ItemUtils.applyName(item, "§r§e§l" + this.getName());

        String[] data = {"Not set. Left click to set block.", "Not set. Right click to set material."};
        if (block != null)
        {
            data[0] = block.getType().name() + " at (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")";
            data[1] = newMaterial.name();
        }

        ItemUtils.applyLore(item, 
            "§r§7§o" + this.getDescription(),
            "",
            "§r" + ChatUtils.of("CCCCCC") + "Block:",
            "§r§7§o" + data[0],
            "§r" + ChatUtils.of("CCCCCC") + "Material:",
            "§r§7§o" + data[1]
        );

        return item;
    }

    @Override
    public void trigger(Entity... targets)
    {
        block.setType(newMaterial);   
    }

    @Override
    public void clean()
    {
        block.setType(oldMaterial);
    }
}
