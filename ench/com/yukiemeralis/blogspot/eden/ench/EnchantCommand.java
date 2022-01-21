package com.yukiemeralis.blogspot.eden.ench;

import com.yukiemeralis.blogspot.eden.command.EdenCommand;
import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant;
import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant.CompatibleItem;
import com.yukiemeralis.blogspot.eden.gui.base.DynamicGui;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.Result;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantCommand extends EdenCommand
{
    public EnchantCommand(EdenModule parent_module)
    {
        super("edenench", parent_module);

        this.addBranch("gui", "apply");
        this.getBranch("apply").addBranch("<enchant>").addBranch("<level>");
    }

    @EdenCommandHandler(usage = "edenench gui", description = "Opens the enchant GUI", argsCount = 1)
    public void edencommand_gui(CommandSender sender, String commandLabel, String[] args)
    {
        DynamicGui gui = new EnchantGui((Player) sender); // TODO Audit this
        gui.init();
        gui.display((Player) sender);
    }
    
    @EdenCommandHandler(usage = "edenench apply <enchant> <level>", description = "Apply an enchantment to an item.", argsCount = 3)
    public void edencommand_apply(CommandSender sender, String commandLabel, String[] args)
    {
        if (!(sender instanceof Player))
        {
            PrintUtils.sendMessage(sender, "Only players can apply enchantments.");
            return;
        }

        Result<EdenEnchant, String> result = EdenEnchant.getEnchantByClassname(args[1]);
        EdenEnchant enchant;

        switch (result.getState())
        {
            case OK:
                enchant = (EdenEnchant) result.unwrap();
                break;
            case ERR:
                PrintUtils.sendMessage(sender, "Failed to obtain enchantment. Reason: \"" + (String) result.unwrap() + "\"");
                return;
            default: return;
        }

        ItemStack held = ((Player) sender).getInventory().getItemInMainHand();
        
        if (held.getType().isAir())
        {
            PrintUtils.sendMessage(sender, "An item must be held in your main hand to apply an enchantment.");
            return;
        }

        boolean valid = false;
        for (CompatibleItem i : enchant.getCompatibleItems())
            if (i.isCompatible(held.getType()))
                valid = true;
        if (!valid)
        {
            PrintUtils.sendMessage(sender, "Itemstack type is not applicable to this enchantment.");
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            PrintUtils.sendMessage(sender, "Invalid enchantment level. Usage: /edenench apply " + args[1] + " <level>");
            return;
        }

        Result<Boolean, String> enchResult = EdenEnchant.apply(held, enchant, level);
        switch (enchResult.getState())
        {
            case OK:
                PrintUtils.sendMessage(sender, "Assignment successful.");
                break;
            case ERR:
                PrintUtils.sendMessage(sender, "Assignment failed. Reason: \"" + enchResult.unwrap() + "\".");
                break;
        }
    }
}
