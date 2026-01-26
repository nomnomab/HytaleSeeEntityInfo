package com.nomnom.entityviewer;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.Objects;
import java.util.UUID;

public class PlayerData {
    public final UUID UUID;
    public final PlayerRef PlayerRef;

    public int PageIndex;
    public String Filter;
    public String SelectedWorldName;

    public EntityViewerBook Book;

    public PlayerData(UUID uuid, PlayerRef playerRef) {
        this.UUID = uuid;
        this.PlayerRef = playerRef;
        this.PageIndex = 0;
        this.Book = new EntityViewerBook();
        this.Filter = "";
        this.SelectedWorldName = getWorld().getName();
    }

    public void reset() {
        this.PageIndex = 0;
        this.Filter = "";
        this.Book.clear();
        this.SelectedWorldName = getWorld().getName();
    }

    public Player getPlayer() {
        var world = getWorld();
        var ref = PlayerRef.getReference();
        if (ref == null) return null;
        return world.getEntityStore().getStore().getComponent(ref, Player.getComponentType());
    }

    public World getWorld() {
        var store = Objects.requireNonNull(PlayerRef.getReference()).getStore();
        return store.getExternalData().getWorld();
    }

    public WorldData getWorldData() {
        return EntityViewer.getInstance().getWorldData(getWorld());
    }

    public World getSelectedWorld() {
        return Universe.get().getWorld(SelectedWorldName);
    }

    public WorldData getSelectedWorldData() {
        return EntityViewer.getInstance().getWorldData(getSelectedWorld());
    }
}
