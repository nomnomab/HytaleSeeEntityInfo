package com.nomnom.entityviewer;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.nomnom.entityviewer.ui.EntityViewerPage;

import java.util.UUID;

public class PlayerData {
    public final UUID UUID;

    public String Filter;
    public String SelectedWorldName;
    public int SelectedEntityId = -1;

    public EntityViewerPage Page;
    public EntityViewerBook Book;

    public PlayerData(UUID uuid) {
        this.UUID = uuid;
        this.Filter = "";
        this.SelectedWorldName = getWorld().getName();
        this.Book = new EntityViewerBook();
    }

    public void reset() {
        this.Filter = "";
        this.SelectedWorldName = getWorld().getName();
        this.Book.clear();
    }

    public PlayerRef getPlayerRef() {
        return Universe.get().getPlayer(UUID);
    }

    public Player getPlayer() {
        var playerRef = getPlayerRef();
        if (playerRef == null) return null;

        var ref = playerRef.getReference();
        if (ref == null) return null;

        return ref.getStore().getComponent(ref, Player.getComponentType());
    }

    public World getWorld() {
        var playerRef = getPlayerRef();
        if (playerRef == null) return null;

        var ref = playerRef.getReference();
        if (ref == null) return null;

        return ref.getStore().getExternalData().getWorld();
    }

    public WorldData getWorldData() {
        var world = getWorld();
        if (world == null) return null;

        return EntityViewer.getWorldData(world);
    }

    public World getSelectedWorld() {
        return Universe.get().getWorld(SelectedWorldName);
    }

    public WorldData getSelectedWorldData() {
        var world = getSelectedWorld();
        if (world == null) return null;

        return EntityViewer.getWorldData(world);
    }

    public EntityData getSelectedEntityData() {
        if (SelectedEntityId == -1) return null;

        var worldData = getSelectedWorldData();
        if (worldData == null) return null;
        return worldData.Entities.get(SelectedEntityId);
    }

    public void rebuildPage() {
        if (Page == null) return;
        Page.fullRebuild();
    }

    public void buildRealtimeElements() {
        if (Page == null) return;
        Page.buildRealtimeElements();
    }

    public void buildEntitiesList() {
        if (Page == null) return;
        Page.buildEntitiesList();
    }
}
