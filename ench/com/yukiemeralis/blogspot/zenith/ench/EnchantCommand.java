package com.yukiemeralis.blogspot.zenith.ench;

import com.yukiemeralis.blogspot.modules.zenithgui.base.DynamicGui;
import com.yukiemeralis.blogspot.zenith.command.ZenithCommand;
import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant;
import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant.CompatibleItem;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.Result;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantCommand extends ZenithCommand
{
    public EnchantCommand(ZenithModule parent_module)
    {
        super("zench", parent_module);

        this.addBranch("gui", "apply");
        this.getBranch("apply").addBranch("<enchant>").addBranch("<level>");
    }

    @ZenCommandHandler(usage = "zench gui", description = "Opens the enchant GUI", argsCount = 1)
    public void zcommand_gui(CommandSender sender, String commandLabel, String[] args)
    {
        DynamicGui gui = new EnchantGui((Player) sender);
        gui.init();
        gui.display((Player) sender);
    }
    
    @ZenCommandHandler(usage = "zench apply <enchant> <level>", description = "Apply an enchantment to an item.", argsCount = 3)
    public void zcommand_apply(CommandSender sender, String commandLabel, String[] args)
    {
        if (!(sender instanceof Player))
        {
            PrintUtils.sendMessage(sender, "Only players can apply enchantments.");
            return;
        }

        Result<ZenithEnchant, String> result = ZenithEnchant.getEnchantByClassname(args[1]);
        ZenithEnchant enchant;

        switch (result.getState())
        {
            case OK:
                enchant = (ZenithEnchant) result.unwrap();
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
            PrintUtils.sendMessage(sender, "Invalid enchantment level. Usage: /zench apply " + args[1] + " <level>");
            return;
        }

        Result<Boolean, String> enchResult = ZenithEnchant.apply(held, enchant, level);
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
