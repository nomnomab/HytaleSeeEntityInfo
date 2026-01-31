package com.nomnom.seeentityinfo;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interface_.Notification;
import com.hypixel.hytale.registry.Registration;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.seeentityinfo.commands.ShowEntityViewerCommand;
import com.nomnom.seeentityinfo.items.DebugStickInteraction;
import com.nomnom.seeentityinfo.items.DebugStickSneakInteraction;
import com.nomnom.seeentityinfo.systems.UpdateEntityDataSystem;
import com.nomnom.seeentityinfo.systems.ListenSystem;
import com.nomnom.seeentityinfo.ui.PageSignals;
import com.nomnom.seeentityinfo.ui.ValueToString;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.logging.Level;

public class SeeEntityInfo extends JavaPlugin {
    private static final boolean ShowLogs = false;

    public static volatile Map<UUID, PlayerData> Players;
    public static volatile Map<String, WorldData> Worlds;

    private static SeeEntityInfo instance;
    private static ValueToString _valueToString;

    private final List<CommandRegistration> _commands = new ArrayList<>(16);
    private final List<Registration> _events = new ArrayList<>(16);

    public SeeEntityInfo(@Nonnull JavaPluginInit init) {
        super(init);

        instance = this;
        _valueToString = new ValueToString();

        Players = new HashMap<>(32);
        Worlds = new HashMap<>(32);

        log("Plugin loaded!");
    }

    public static SeeEntityInfo getInstance() {
        return instance;
    }

    public static void log(String message) {
        getInstance().getLogger().at(Level.INFO).log(message);
    }

    public static void logDebug(String message) {
        if (!ShowLogs) return;
        log(message);
    }

    public static void warn(String message) {
        getInstance().getLogger().at(Level.WARNING).log(message);
    }

    public static void err(String message) {
        getInstance().getLogger().at(Level.SEVERE).log(message);
    }

    @Override
    protected void setup() {
        log("Plugin setup!");

        registerEvents();
        registerCommands();

        _commands.add(this.getCommandRegistry().registerCommand(new ShowEntityViewerCommand("entityinfo", "Shows the Entity Viewer")));
        this.getEntityStoreRegistry().registerSystem(new UpdateEntityDataSystem());
        this.getEntityStoreRegistry().registerSystem(new ListenSystem());
        this.getCodecRegistry(Interaction.CODEC).register("SeeEntityInfo_DebugStick", DebugStickInteraction.class, DebugStickInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("SeeEntityInfo_DebugStickSneak", DebugStickSneakInteraction.class, DebugStickSneakInteraction.CODEC);
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

        EntityStore.REGISTRY.unregisterSystem(UpdateEntityDataSystem.class);
        EntityStore.REGISTRY.unregisterSystem(ListenSystem.class);

        for (var e : _events) {
            e.unregister();
        }
        _events.clear();
    }

    private void registerEvents() {
        _events.add(this.getEventRegistry().register(LoadedAssetsEvent.class, ModelAsset.class, SeeEntityInfo::onModelAssetLoad));
        _events.add(this.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, SeeEntityInfo::onWorldsLoaded));
        _events.add(this.getEventRegistry().registerGlobal(AddWorldEvent.class, SeeEntityInfo::onWorldAdded));
        _events.add(this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, SeeEntityInfo::onWorldRemoved));
//        _events.add(this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, EntityViewer::onPlayerJoined));
        _events.add(this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, SeeEntityInfo::onPlayerReady));
        _events.add(this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, SeeEntityInfo::onPlayerLeft));

        PacketAdapters.registerOutbound(SeeEntityInfo::reopenPage);
    }

    private void registerCommands() {}

    public static Map<String, ModelAsset> MODELS = new HashMap<>();
    private static void onModelAssetLoad(LoadedAssetsEvent<String, ModelAsset, DefaultAssetMap<String, ModelAsset>> event) {
        MODELS = event.getAssetMap().getAssetMap();
    }

    private static void onWorldsLoaded(AllWorldsLoadedEvent event) {
        var worlds = Universe.get().getWorlds();
        logDebug("worlds loaded: " + worlds.size());

        for (var world : worlds.values()) {
            registerWorld(world);
        }
    }

    private static void onWorldAdded(AddWorldEvent addWorldEvent) {
        var world = addWorldEvent.getWorld();
        logDebug("World added: " + world.getName());

        registerWorld(world);
    }

    private static void onWorldRemoved(RemoveWorldEvent removeWorldEvent) {
        var world = removeWorldEvent.getWorld();
        logDebug("World removed: " + world.getName());

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

    // reloads my page when the asset changes automatically
    private static boolean reopenPage(PlayerRef playerRef, Packet packet) {
        if (!(packet instanceof Notification notification)) {
            return false;
        }

        // bad message content
        if (notification.secondaryMessage == null) return false;
        var rawText = notification.secondaryMessage.rawText;
        if (rawText == null) return false;

        // not from me
        if (!notification.secondaryMessage.rawText.startsWith("com.nomnom:Entity Viewer:UI/Custom/Pages")) {
            return false;
        }

        // get store
        var ref = playerRef.getReference();
        var store = ref != null ? ref.getStore() : null;
        if (store == null) return false;

        // manage page
        var player = store.getComponent(ref, Player.getComponentType());
        var pageManager = player != null ? player.getPageManager() : null;
        if (pageManager == null)  return false;

        var page = pageManager.getCustomPage();
        if  (page == null) return false;

        var pageClass = page.getClass();
        logDebug("package: " + pageClass.getPackageName());

        if (pageClass.getPackageName().startsWith("com.nomnom.seeentityinfo")) {
            pageManager.openCustomPage(ref, store, page);
        }

        return false;
    }

    public static void registerPlayer(UUID uuid) {
        if (!Players.containsKey(uuid)) {
            logDebug("Registering player " + uuid);
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

        logDebug("Unregistering player " + playerRef);
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
            logDebug("Registering world " + world.getName());
            Worlds.put(name, new WorldData(world));
        }
    }

    public static void unregisterWorld(World world) {
        logDebug("Unregistering world " + world.getName());

        var name = world.getName();
        Worlds.remove(name);
    }
}
