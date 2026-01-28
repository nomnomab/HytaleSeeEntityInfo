package com.nomnom.entityviewer;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interface_.Notification;
import com.hypixel.hytale.registry.Registration;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.nomnom.entityviewer.commands.ShowEntityViewerCommand;
import com.nomnom.entityviewer.commands.ShowTestUiCommand;
import com.nomnom.entityviewer.ui.EntityViewerSystem;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.logging.Level;

public class EntityViewer extends JavaPlugin {
    public volatile Map<UUID, PlayerData> Players;
    public volatile Map<String, WorldData> Worlds;

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
            getInstance().registerWorld(world);
        }
    }

    private static void onWorldAdded(AddWorldEvent addWorldEvent) {
        var world = addWorldEvent.getWorld();
        log("World added: " + world.getName());

        getInstance().registerWorld(world);
    }

    private static void onWorldRemoved(RemoveWorldEvent removeWorldEvent) {
        var world = removeWorldEvent.getWorld();
        log("World removed: " + world.getName());

        getInstance().unregisterWorld(world);
    }

    public void registerPlayer(PlayerRef playerRef) {
        var uuid = playerRef.getUuid();
        if (!Players.containsKey(uuid)) {
            log("Registering player " + playerRef);
            Players.put(uuid, new PlayerData(uuid, playerRef));
            log("Player " + playerRef + " has been registered");
        }
    }

    public void unregisterPlayer(PlayerRef playerRef) {
        var uuid = playerRef.getUuid();
        if (!Players.containsKey(uuid)) return;

        log("Unregistering player " + playerRef);
        Players.remove(uuid);
        log("Player " + playerRef + " has been unregistered");
    }

    public WorldData getWorldData(World world) {
        return getWorldData(world.getName());
    }

    public WorldData getWorldData(String name) {
        return Worlds.get(name);
    }

    public void registerWorld(World world) {
        var name = world.getName();
        if (!Worlds.containsKey(name)) {
            log("Registering world " + world.getName());
            Worlds.put(name, new WorldData(world));
            log("World " + name + " has been registered");
        }
    }

    public void unregisterWorld(World world) {
        log("Unregistering world " + world.getName());

        var name = world.getName();
        Worlds.remove(name);

        log("World " + name + " has been unregistered");
    }
}
