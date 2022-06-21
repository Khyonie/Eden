package fish.yukiemeralis.eden.core.old.modgui;

import fish.yukiemeralis.eden.module.java.annotations.Unimplemented;

/**
 * @deprecated Deprecated in favor of Surface2-based implementation.
 */
@Deprecated
@Unimplemented("Class has been retired.")
public class ModuleSubGui
{
    // EdenModule module;

    // private static final GuiItemStack enable_button, disable_button, unload_button;
    // static {
    //     enable_button = new GuiItemStack(
    //         ItemUtils.build(Material.LIME_CONCRETE, "§r§a§lEnable module", "§r§2Enable a disabled module.")
    //     ) {
    //         @Override
    //         public void onIconInteract(InventoryClickEvent event) 
    //         {
    //             EdenModule mod = ((ModuleSubGui) DynamicGui.getOpenedGuis().get(event.getWhoClicked())).getModule();

    //             Eden.getModuleManager().enableModule(mod);
    //             mod.setEnabled();

    //             ModuleTracker.update();
    //             ModuleGui gui = new ModuleGui();
    //             gui.display((Player) event.getWhoClicked());
    //         }
    //     };

    //     disable_button = new GuiItemStack(
    //         ItemUtils.build(
    //             Material.RED_CONCRETE, 
    //             "§r§c§lDisable module", 
    //             "§r§4Disable an enabled module.", 
    //             "§r§4This unloads all commands and events", 
    //             "§r§4tied to the module.",
    //             "",
    //             "§r§4All listed reliant modules will",
    //             "§r§4also be disabled. "
    //         )
    //     ) {
    //         @Override
    //         public void onIconInteract(InventoryClickEvent event) 
    //         {
    //             EdenModule mod = ((ModuleSubGui) DynamicGui.getOpenedGuis().get(event.getWhoClicked())).getModule();

    //             if (mod.getClass().isAnnotationPresent(PreventUnload.class))
    //             {
    //                 PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module cannot be disabled from here!");
    //                 return;
    //             }

    //             Option<ModuleDisableFailureData> option = Eden.getModuleManager().disableModule(mod.getName(), CallerToken.PLAYER);
                
    //             switch (option.getState())
    //             {
    //                 case NONE:
    //                     PrintUtils.sendMessage(event.getWhoClicked(), "Successfully disabled module.");
    //                     break;
    //                 case SOME:
    //                     PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to disable module! Performing rollback...");

    //                     if (option.unwrap().performRollback())
    //                     {
    //                         PrintUtils.sendMessage(event.getWhoClicked(), "§cRollback complete.");
    //                         return;
    //                     }

    //                     PrintUtils.sendMessage(event.getWhoClicked(), "§cRollback failed.");
    //                     return;
    //             }

    //             mod.setDisabled();

    //             ModuleTracker.update();
    //             ModuleGui gui = new ModuleGui();
    //             gui.display((Player) event.getWhoClicked());
    //         }
    //     };

    //     unload_button = new GuiItemStack(
    //         ItemUtils.build(Material.YELLOW_CONCRETE, "§r§e§lUnload module", "§r§6Unload a disabled module from", "§r§6memory.", "§r§6If the module is enabled, it will", "§r§6be disabled first.")
    //     ) {
    //         @Override
    //         public void onIconInteract(InventoryClickEvent event) 
    //         {
    //             EdenModule mod = ((ModuleSubGui) DynamicGui.getOpenedGuis().get(event.getWhoClicked())).getModule();

    //             if (mod.getClass().isAnnotationPresent(PreventUnload.class))
    //             {
    //                 PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module cannot be disabled from here!");
    //                 return;
    //             }

    //             if (mod.getIsEnabled()) // Disable
    //             {
    //                 Option<ModuleDisableFailureData> option = Eden.getModuleManager().disableModule(mod.getName(), CallerToken.PLAYER);

    //                 switch (option.getState())
    //                 {
    //                     case NONE:
    //                         mod.setDisabled();
    //                         break;
    //                     case SOME:
    //                         PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to disable module! Performing rollback...");

    //                         if (option.unwrap().performRollback())
    //                         {
    //                             PrintUtils.sendMessage(event.getWhoClicked(), "§cRollback complete.");
    //                             return;
    //                         }

    //                         PrintUtils.sendMessage(event.getWhoClicked(), "§cRollback failed.");
    //                         return;
    //                 }
    //             }

    //             Eden.getModuleManager().removeModuleFromMemory(mod.getName(), CallerToken.PLAYER);

    //             ModuleTracker.update();
    //             ModuleGui gui = new ModuleGui();
    //             gui.display((Player) event.getWhoClicked());
    //         }
    //     };
    // }

    // public ModuleSubGui()
    // {
    //     super(9, "Module options", null, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    //     this.module = null;
    // }

    // public ModuleSubGui(EdenModule module) 
    // {
    //     super(9, "Module options", null, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    //     this.module = module;
    // }

    // @Override
    // public void init() 
    // {
    //     paint();

    //     addComponent(3, enable_button);
    //     if (module.getIsEnabled())
    //         addComponent(3, disable_button);

    //     getInventory().setItem(4, module.toIcon());
    //     getInventory().setItem(8, reliantModules());
    //     addComponent(5, unload_button);

    //     paintComponents();
    // }

    // private ItemStack reliantModules()
    // {
    //     if (module.getReliantModules().size() == 0)
    //         return getBlankIcon().clone();

    //     List<String> lore = new ArrayList<>();
    //     module.getReliantModules().forEach(mod -> lore.add("§r" + ChatUtils.of("9CE8A7") + mod.getName()));
    //     return ItemUtils.build(Material.OAK_SAPLING, "§r" + ChatUtils.of("DD73CE") + "§lReliant modules", lore.toArray(new String[lore.size()]));
    // }

    // @Override
    // @EventHandler
    // public void onInteract(InventoryClickEvent event) 
    // {
    //     if (!isEventApplicable(event, true))
    //         return;

    //     DynamicGui gui = DynamicGui.getOpenedGuis().get(event.getWhoClicked());
    //     if (gui.getComponents().containsKey(event.getRawSlot()))
    //     {
    //         gui.getComponents().get(event.getRawSlot()).onIconInteract(event);
    //         return;
    //     }
            
    //     ModuleTracker.update();
    //     ModuleGui maingui = new ModuleGui();
    //     maingui.display(event.getWhoClicked());
    // }
    
    // public EdenModule getModule()
    // {
    //     return this.module;
    // }
}
