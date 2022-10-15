package fish.yukiemeralis.flock.gui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.InventoryView;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.surface2.special.PagedSurfaceGui;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.result.Result;
import fish.yukiemeralis.flock.DownloadUtils;
import fish.yukiemeralis.flock.Flock;
import fish.yukiemeralis.flock.enums.JsonDownloadStatus;
import fish.yukiemeralis.flock.repository.ModuleRepository;

public class GlobalRepositoryGui extends PagedSurfaceGui 
{
    private static GuiItemStack QUIT_BUTTON = SimpleComponentBuilder.build(Material.BARRIER, "§r§c§lClose", (event) -> event.getWhoClicked().closeInventory(), "§7§oExits this menu.");
    private static GuiItemStack ADD_NEW_BUTTON = SimpleComponentBuilder.build(Material.EMERALD, "§r§a§lSync new repository", (event) -> 
        {
            event.getWhoClicked().closeInventory();
            PrintUtils.sendMessage(event.getWhoClicked(), "Enter repository URL, or enter \"cancel\" to exit text-entry mode.");

            ChatUtils.expectChat(event.getWhoClicked(), () -> {
                String output = ChatUtils.receiveResult(event.getWhoClicked());

                if (output == null)
                    return;

                if (output.toLowerCase().equals("cancel"))
                {
                    new GlobalRepositoryGui(event.getWhoClicked()).display(event.getWhoClicked());
                    return;
                }

                Result result = DownloadUtils.downloadJson(output, ModuleRepository.class);

                if (result.isErr())
                {
                    switch (result.unwrapErr(JsonDownloadStatus.class))
                    {
                        case CONNECTION_FAILED:
                            PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to connect to resource, please check the URL or try again later.");
                            break;
                        case CORRUPT_REPOSITORY:
                            PrintUtils.sendMessage(event.getWhoClicked(), "§cRepository is corrupt! Cannot synchronize.");
                            break;
                        case MALFORMED_URL:
                            PrintUtils.sendMessage(event.getWhoClicked(), "§cInvalid URL. Please check the URL.");
                            break;
                    }
                    return;
                }

                ModuleRepository repo = result.unwrapOk(ModuleRepository.class);
                if (Flock.hasRepository(repo.getName()))
                {
                    PrintUtils.sendMessage(event.getWhoClicked(), "§cA repository with that name is already synchronized.");
                    return;
                }

                PrintUtils.sendMessage(event.getWhoClicked(), "§aSuccessfully synchronized new repository \"" + repo.getName() + "\"!");
                Flock.addRepository(repo);

                Bukkit.getScheduler().runTask(Eden.getInstance(), () -> new GlobalRepositoryGui(event.getWhoClicked()).display(event.getWhoClicked()));
            });
        },
        "§7§oSynchronizes a new repository."
    );

    private static GuiItemStack CREATE_NEW_BUTTON = SimpleComponentBuilder.build(Material.CRAFTING_TABLE, "§r§a§lCreate new repository", (event) -> 
        {
            event.getWhoClicked().closeInventory();
            ModuleRepositoryBuilder builder = new ModuleRepositoryBuilder();

            PrintUtils.sendMessage(event.getWhoClicked(), "Enter repository name:");

            // Get name
            ChatUtils.expectChat(event.getWhoClicked(), () -> {
                String name = ChatUtils.receiveResult(event.getWhoClicked());
                ChatUtils.deleteResult(event.getWhoClicked());

                builder.setName(name);

                if (Flock.hasRepository(name))
                {
                    PrintUtils.sendMessage(event.getWhoClicked(), "§cA repository named \"" + name + "\" already exists locally. Remove the repository, or choose a different name.");
                    return;
                }

                // Get URL
                PrintUtils.sendMessage(event.getWhoClicked(), "Enter repository URL:");
                ChatUtils.expectChat(event.getWhoClicked(), () -> {
                    try {
                        String result = ChatUtils.receiveResult(event.getWhoClicked());
                        new URL(result); // This will throw an error if the URL is bad

                        // URL is likely valid at this point, if the data it points to is bad, that's up to the creator to figure out 
                        builder.setUrl(result);

                        ModuleRepository repo = builder.build();
                        Flock.addRepository(repo);
                        PrintUtils.sendMessage(event.getWhoClicked(), "§aSuccessfully created a new repository named \"§b" + repo.getName() + "§a\"!");

                        // Open GUI in sync
                        Bukkit.getScheduler().runTask(Eden.getInstance(), () -> new RepositoryEditGui(repo, event.getWhoClicked()).display(event.getWhoClicked()));
                    } catch (MalformedURLException e) {
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cInvalid URL. Please check the URL.");
                        return;
                    } finally {
                        ChatUtils.deleteResult(event.getWhoClicked());
                    }
                });
            });
        },
        "§7§oCreates a new local repository and opens",
        "§7§oit in edit mode."
    );

    public GlobalRepositoryGui(HumanEntity target) 
    {
        super(36, "Module Repositories", target, 0, Flock.getRepositories(), List.of(QUIT_BUTTON, ADD_NEW_BUTTON, CREATE_NEW_BUTTON), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    }   

    private int prefetched = 0;
    @Override
    public InventoryView display(HumanEntity e)
    {
        // Wait for all the repos to prefetch
        for (ModuleRepository repo : Flock.getRepositories())
            repo.prefetch(() -> prefetched++);

        Runnable prefetchChecker = () -> {
            long currentTime = System.currentTimeMillis(); // We want to wait 60 seconds/6000ms
            while (prefetched < Flock.getRepositories().size())
            {
                if (System.currentTimeMillis() - currentTime > 6000) // Timeout timer
                {
                    break;
                }
            }

            Bukkit.getScheduler().runTask(Eden.getInstance(), () -> {
                super.display(e);
                Flock.getRepositories().forEach(repo -> repo.cleanupPrefetch());
            }); // If all the repos were prefetched or the timeout threshold was reached, open the global GUI
        };
        new Thread(prefetchChecker).start();
        
        return new SnakeLoadingGui().display(e);
    }

    public static class ModuleRepositoryBuilder
    {
        private String name, url;

        public void setName(String name)
        {
            this.name = name;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        public ModuleRepository build()
        {
            if (name == null)
                throw new IllegalStateException("Cannot build module repository with null name");
            if (url == null)
                throw new IllegalStateException("Cannot build module repository with null URL");
            return new ModuleRepository(name, url);
        }
    }
}
