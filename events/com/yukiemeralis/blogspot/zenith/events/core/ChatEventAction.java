package com.yukiemeralis.blogspot.zenith.events.core;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;
import com.yukiemeralis.blogspot.zenith.utils.ChatUtils;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ChatEventAction extends ZenithEvent
{
    private final String message;

    public ChatEventAction(String message)
    {
        super("Chat message", "Sends a chat message to applicable entitites.", Material.FILLED_MAP);
        this.message = message;
    }

    @Override
    public void trigger(Entity... targets)
    {
        for (Entity e : targets)
            PrintUtils.sendMessage(e, message);
    }

    @Override
    public GuiItemStack toIcon()
    {
        GuiItemStack item = new GuiItemStack(this.getMaterial())
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                return;
            }
        };

        ItemUtils.applyName(item, "§r§e§l" + this.getName());

        String loreMessage = "Message not set. Click to set message.";
        if (message != null)
            loreMessage = "\"" + message + "\"";
        ItemUtils.applyLore(item, 
            "§r§7§o" + this.getDescription(), 
            "", 
            "§r" + ChatUtils.of("CCCCCC") + "Message:",
            "§r§7§o" + loreMessage
        );

        return item;
    }
}