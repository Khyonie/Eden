package com.yukiemeralis.blogspot.zenith.events.triggers;

import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockInteractTrigger extends EventTrigger<PlayerInteractEvent>
{
    private Block block;

    public BlockInteractTrigger(boolean synchronizeAttendees, int step, Block block, ZenithEvent... events)
    {
        super(synchronizeAttendees, step, events);
        this.block = block;
    }

    @Override
    public boolean shouldTrigger(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null)
            return false;

        return event.getClickedBlock().equals(block);
    }

    @Override
    public void runEvent(PlayerInteractEvent event)
    {
        if (willSynchronizeAttendees())
        {
            this.getAssociatedScript().synchronizeProgress(this.getStep());
            for (ZenithEvent e : this.getAssociatedEvents())
                e.trigger(this.getAssociatedScript().getAttendees().toArray(new Player[this.getAssociatedScript().getAttendees().size()])); 
            for (Player p : this.getAssociatedScript().getAttendees())
                this.getAssociatedScript().step(p);
            return;
        }

        for (ZenithEvent e : this.getAssociatedEvents())
            e.trigger(event.getPlayer());
        this.getAssociatedScript().step(event.getPlayer());
    }

    @Override
    public void clean()
    {
        
    }
}
