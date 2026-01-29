package com.nomnom.entityviewer;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.event.IBaseEvent;
import com.hypixel.hytale.protocol.packets.interface_.AddToServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.RemoveFromServerPlayerList;
import com.hypixel.hytale.registry.Registration;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.entityviewer.commands.ShowEntityViewerCommand;
import com.nomnom.entityviewer.commands.ShowTestUiCommand;
import com.nomnom.entityviewer.systems.EntityViewerSystem;
import com.nomnom.entityviewer.systems.ListenSystem;
import com.nomnom.entityviewer.ui.PageSignals;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.logging.Level;

public class EntityViewer extends JavaPlugin {
    public static volatile Map<UUID, PlayerData> Players;
    public static volatile Map<String, WorldData> Worlds;

    public static final int MAX_ENTRIES_PER_PAGE = 50;

    private static EntityViewer instance;

    private final List<CommandRegistration> _commands = new ArrayList<>(16);
    private final List<Registration> _events = new ArrayList<>(16);

    public EntityViewer(@Nonnull JavaPluginInit init) {
        super(init);

        instance = this;

        Players = new HashMap<>(32);
        Worlds = new HashMap<>(32);

        getLogger().at(Level.INFO).log("[EntityViewer] Plugin loaded!");
    }

    public static EntityViewer getInstance() {
        return instance;
    }

    public static void log(String message) {
        getInstance().getLogger().at(Level.INFO).log("[EntityViewer] " + message);
    }

    public static void warn(String message) {
        getInstance().getLogger().at(Level.WARNING).log("[EntityViewer] " + message);
    }

    public static void err(String message) {
        getInstance().getLogger().at(Level.SEVERE).log("[EntityViewer] " + message);
    }

    @Override
    protected void setup() {
        log("Plugin setup!");

        registerEvents();
        registerCommands();

        _commands.add(this.getCommandRegistry().registerCommand(new ShowEntityViewerCommand("entityviewer", "Shows the Entity Viewer")));
        _commands.add(this.getCommandRegistry().registerCommand(new ShowTestUiCommand("testui", "Shows the Entity Viewer")));
        this.getEntityStoreRegistry().registerSystem(new EntityViewerSystem());
        this.getEntityStoreRegistry().registerSystem(new ListenSystem());
    }

    @Override
    protected void start() {
        log("Plugin enabled!");
    }

    @Override
    public void shutdown() {
        log("Plugin disabled!");

        for (var cmd : _commands) {
            cmd.unregister();
        }
        _commands.clear();

        EntityStore.REGISTRY.unregisterSystem(EntityViewerSystem.class);
        EntityStore.REGISTRY.unregisterSystem(ListenSystem.class);

        for (var e : _events) {
            e.unregister();
        }
        _events.clear();
    }

    private void registerEvents() {
        _events.add(this.getEventRegistry().register(LoadedAssetsEvent.class, ModelAsset.class, EntityViewer::onModelAssetLoad));
        _events.add(this.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, EntityViewer::onWorldsLoaded));
        _events.add(this.getEventRegistry().registerGlobal(AddWorldEvent.class, EntityViewer::onWorldAdded));
        _events.add(this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, EntityViewer::onWorldRemoved));
//        _events.add(this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, EntityViewer::onPlayerJoined));
        _events.add(this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, EntityViewer::onPlayerReady));
        _events.add(this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, EntityViewer::onPlayerLeft));

        PacketAdapters.registerOutbound(ShowTestUiCommand::reopenWindow);
    }

    private void registerCommands() {}

    public static Map<String, ModelAsset> MODELS = new HashMap<>();
    private static void onModelAssetLoad(LoadedAssetsEvent<String, ModelAsset, DefaultAssetMap<String, ModelAsset>> event) {
        MODELS = event.getAssetMap().getAssetMap();
    }

    private static void onWorldsLoaded(AllWorldsLoadedEvent event) {
        var worlds = Universe.get().getWorlds();
        log("worlds loaded: " + worlds.size());

        for (var world : worlds.values()) {
            registerWorld(world);
        }
    }

    private static void onWorldAdded(AddWorldEvent addWorldEvent) {
        var world = addWorldEvent.getWorld();
        log("World added: " + world.getName());

        registerWorld(world);
    }

    private static void onWorldRemoved(RemoveWorldEvent removeWorldEvent) {
        var world = removeWorldEvent.getWorld();
        log("World removed: " + world.getName());

        unregisterWorld(world);
    }

//    private static void onPlayerJoined(PlayerConnectEvent playerConnectEvent) {
//        registerPlayer(playerConnectEvent.getPlayerRef());
//        PageSignals.drawAllPlayerLists();
//    }

    private static void onPlayerReady(PlayerReadyEvent playerReadyEvent) {
        var ref = playerReadyEvent.getPlayerRef();
        var uuid = ref.getStore().getComponent(ref, UUIDComponent.getComponentType());
        assert uuid != null;

        registerPlayer(uuid.getUuid());
        PageSignals.drawAllPlayerLists();
    }

    private static void onPlayerLeft(PlayerDisconnectEvent playerDisconnectEvent) {
        unregisterPlayer(playerDisconnectEvent.getPlayerRef());
        PageSignals.drawAllPlayerLists();
    }

    public static void registerPlayer(UUID uuid) {
        if (!Players.containsKey(uuid)) {
            log("Registering player " + uuid);
            Players.put(uuid, new PlayerData(uuid));

            PageSignals.drawAllPlayerLists();
        }
    }

    public static void registerPlayer(PlayerRef playerRef) {
        var uuid = playerRef.getUuid();
        registerPlayer(uuid);
    }

    public static void unregisterPlayer(PlayerRef playerRef) {
        var uuid = playerRef.getUuid();
        if (!Players.containsKey(uuid)) return;

        log("Unregistering player " + playerRef);
        Players.remove(uuid);

        PageSignals.drawAllPlayerLists();
    }

    public static PlayerData getPlayerData(PlayerRef playerRef) {
        var uuid = playerRef.getUuid();
        return getPlayerData(uuid);
    }

    public static PlayerData getPlayerData(UUID uuid) {
        return Players.get(uuid);
    }

    public static WorldData getWorldData(World world) {
        return getWorldData(world.getName());
    }

    public static WorldData getWorldData(String name) {
        return Worlds.get(name);
    }

    public static void registerWorld(World world) {
        var name = world.getName();
        if (!Worlds.containsKey(name)) {
            log("Registering world " + world.getName());
            Worlds.put(name, new WorldData(world));
        }
    }

    public static void unregisterWorld(World world) {
        log("Unregistering world " + world.getName());

        var name = world.getName();
        Worlds.remove(name);
    }
}
