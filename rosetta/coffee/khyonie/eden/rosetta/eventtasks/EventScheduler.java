package coffee.khyonie.eden.rosetta.eventtasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import coffee.khyonieheart.eden.module.annotation.HideFromCollector;
import coffee.khyonieheart.eden.module.annotation.Unimplemented;

@HideFromCollector
@Unimplemented("Need to determine how to make this work")
@SuppressWarnings("unused")
public class EventScheduler implements Listener
{
    private static final Map<Class<? extends Event>, List<EventScheduledTask<? extends Event>>> SCHEDULED_EVENTS = new HashMap<>();

    private static final String[] ENTITY_EVENTS = {
        "AreaEffectCloudApplyEvent", "ArrowBodyCountChangeEvent", "BatToggleSleepEvent", 
        "CreeperPowerEvent", "EnderDragonChangePhaseEvent", "EntityAirChangeEvent", 
        "EntityBreedEvent", "EntityChangeBlockEvent", "EntityCombustEvent", 
        "EntityCreatePortalEvent", "EntityDamageEvent", "EntityDeathEvent", 
        "EntityDismountEvent", "EntityDropItemEvent", "EntityEnterBlockEvent", 
        "EntityEnterLoveModeEvent", "EntityExhaustionEvent", "EntityExplodeEvent", 
        "EntityInteractEvent", "EntityMountEvent", "EntityPickupItemEvent", 
        "EntityPlaceEvent", "EntityPortalEnterEvent", "EntityPoseChangeEvent", 
        "EntityPotionEffectEvent", "EntityRegainHealthEvent", "EntityResurrectEvent", 
        "EntityShootBowEvent", "EntitySpawnEvent", "EntitySpellCastEvent", 
        "EntityTameEvent", "EntityTargetEvent", "EntityTeleportEvent", 
        "EntityToggleGlideEvent", "EntityToggleSwimEvent", "EntityTransformEvent", 
        "EntityUnleashEvent", "ExplosionPrimeEvent", "FireworkExplodeEvent", 
        "FoodLevelChangeEvent", "HorseJumpEvent", "ItemDespawnEvent", 
        "ItemMergeEvent", "PiglinBarterEvent", "PigZombieAngerEvent", 
        "ProjectileHitEvent", "SheepDyeWoolEvent", "SheepRegrowWoolEvent", 
        "SlimeSplitEvent", "StriderTemperatureChangeEvent", "VillagerAcquireTradeEvent", 
        "VillagerCareerChangeEvent", "VillagerReplenishTradeEvent", "PlayerLeashEntityEvent"
    };

    private static final String[] PLAYER_EVENTS = {
        "AsyncPlayerPreLoginEvent","AsyncPlayerChatEvent", "PlayerAdvancementDoneEvent", 
        "PlayerBedEnterEvent", "PlayerBedLeaveEvent", "PlayerBucketEntityEvent", 
        "PlayerBucketEvent", "PlayerChangedMainHandEvent", "PlayerChangedWorldEvent", 
        "PlayerChannelEvent", "PlayerChatEvent", "PlayerChatTabCompleteEvent", 
        "PlayerCommandPreprocessEvent", "PlayerCommandSendEvent", "PlayerDropItemEvent", 
        "PlayerEditBookEvent", "PlayerEggThrowEvent", "PlayerExpChangeEvent", 
        "PlayerFishEvent", "PlayerGameModeChangeEvent", "PlayerHarvestBlockEvent", 
        "PlayerInteractEntityEvent", "PlayerInteractEvent", "PlayerItemBreakEvent", 
        "PlayerItemConsumeEvent", "PlayerItemDamageEvent", "PlayerItemHeldEvent", 
        "PlayerItemMendEvent", "PlayerJoinEvent", "PlayerKickEvent", 
        "PlayerLevelChangeEvent", "PlayerLocaleChangeEvent", "PlayerLoginEvent", 
        "PlayerMoveEvent", "PlayerPickupItemEvent", "PlayerQuitEvent", 
        "PlayerRecipeDiscoverEvent", "PlayerResourcePackStatusEvent", "PlayerRespawnEvent", 
        "PlayerRiptideEvent", "PlayerShearEntityEvent", "PlayerSpawnLocationEvent", 
        "PlayerStatisticIncrementEvent", "PlayerSwapHandItemsEvent", "PlayerTakeLecternBookEvent", 
        "PlayerToggleFlightEvent", "PlayerToggleSneakEvent", "PlayerToggleSprintEvent", 
        "PlayerVelocityEvent", "PlayerAnimationEvent"
    };

    private static String[] BLOCK_EVENTS = {
        "BlockBurnEvent", "BlockCanBuildEvent", "BlockCookEvent", 
        "BlockDamageEvent", "BlockDispenseEvent", "BlockDropItemEvent", 
        "BlockExpEvent", "BlockExplodeEvent", "BlockFadeEvent", 
        "BlockFertilizeEvent", "BlockFromToEvent", "BlockGrowEvent", 
        "BlockIgniteEvent", "BlockPhysicsEvent", "BlockPistonEvent", 
        "BlockPlaceEvent", "BlockReceiveGameEvent", "BlockRedstoneEvent", 
        "BlockShearEntityEvent", "BrewEvent", "BrewingStandFuelEvent", 
        "CauldronLevelChangeEvent", "FluidLevelChangeEvent", "FurnaceBurnEvent", 
        "FurnaceStartSmeltEvent", "LeavesDecayEvent", "MoistureChangeEvent", 
        "NotePlayEvent", "SignChangeEvent", "SpongeAbsorbEvent"
    };

    private static String[] WORLD_EVENTS = {
        "ChunkEvent", "GenericGameEvent", "LootGenerateEvent", 
        "PortalCreateEvent", "RaidEvent", "SpawnChangeEvent", 
        "StructureGrowEvent", "TimeSkipEvent", "WorldInitEvent", 
        "WorldLoadEvent", "WorldSaveEvent", "WorldUnloadEvent"
    };

    private static final String[] INVENTORY_EVENTS = {
        "EnchantItemEvent", "InventoryCloseEvent", "InventoryInteractEvent", 
        "InventoryOpenEvent", "PrepareAnvilEvent", "PrepareItemCraftEvent", 
        "PrepareItemEnchantEvent", "PrepareSmithingEvent", "InventoryMoveItemEvent",
        "InventoryPickupItemEvent"
    };

    private static final String[] VEHICLE_EVENTS = {
        "VehicleCollisionEvent", "VehicleCreateEvent", "VehicleDamageEvent", 
        "VehicleDestroyEvent", "VehicleEnterEvent", "VehicleExitEvent", 
        "VehicleMoveEvent", "VehicleUpdateEvent"
    };

    private static final String[] SERVER_EVENTS = {
        "BroadcastMessageEvent", "MapInitializeEvent", "PluginEvent", 
        "ServerCommandEvent", "ServerListPingEvent", "ServerLoadEvent", 
        "ServiceEvent", "TabCompleteEvent"
    };

    private static final String[] WEATHER_EVENTS = {
        "LightningStrikeEvent", "ThunderChangeEvent", "WeatherChangeEvent"
    };

    private static final String[] PAINTING_EVENTS = {
        "HangingBreakEvent", "HangingPlaceEvent"
    };

    private static final Map<String, String[]> EVENTS = new HashMap<>() {{
        put("org.bukktit.event.entity", ENTITY_EVENTS);
        put("org.bukktit.event.player", PLAYER_EVENTS);
        put("org.bukktit.event.block", BLOCK_EVENTS);
        put("org.bukktit.event.world", WORLD_EVENTS);
        put("org.bukktit.event.inventory", INVENTORY_EVENTS);
        put("org.bukktit.event.vehicle", VEHICLE_EVENTS);
        put("org.bukktit.event.server", SERVER_EVENTS);
        put("org.bukktit.event.weather", WEATHER_EVENTS);
        put("org.bukkit.event.hanging", PAINTING_EVENTS);
    }};

    static {
        init();
    }

    private static void init()
    {
        
    }

    synchronized static void schedule(EventScheduledTask<? extends Event> task, Class<? extends Event> event)
    {
        if (!SCHEDULED_EVENTS.containsKey(event))
            SCHEDULED_EVENTS.put(event, new ArrayList<>());

        SCHEDULED_EVENTS.get(event).add(task);
    }
    
    protected static void generalHandler(Event event)
    {
        if (!SCHEDULED_EVENTS.containsKey(event.getClass()))
            return;

            SCHEDULED_EVENTS.get(event.getClass()).removeIf(task -> {
                if (!task.isApplicable(event))
                    return false;
    
                task.setEvent(event);
                task.run();
                return true;
            });

        SCHEDULED_EVENTS.get(event.getClass()).clear();
    }

    @HideFromCollector
    static class GenericEventHandler<T extends Event>
    {
        @EventHandler
        public void handler(T event)
        {
            generalHandler(event);
        }
    } 
}
