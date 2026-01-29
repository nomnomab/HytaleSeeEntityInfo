package com.nomnom.entityviewer;

import com.hypixel.hytale.component.metric.ArchetypeChunkData;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;

public class WorldData {
    public final UUID UUID;
    public final String Name;

    public volatile ArchetypeChunkData[] Chunks;
    public volatile List<ArchetypeData> Archetypes;
    public volatile Map<Integer, EntityData> Entities;

    public double ValidateTimer;

    // build flags
    public boolean RebuildEntityLookup = true;

    // draw flags
    public boolean DrawAll;
    public boolean DrawRealtimeElements;
    public boolean DrawPlayerList;
    public boolean DrawTeleportersList;
    public boolean DrawEntitiesList;
    public List<EntityChange> EntityChanges;

    public WorldData(World world) {
        UUID = world.getWorldConfig().getUuid();
        Name = world.getName();

        Chunks = new ArchetypeChunkData[0];
        Archetypes = new ObjectArrayList<>(512);
        Entities = new HashMap<>(512);

        EntityChanges = new ArrayList<>(256);

        ValidateTimer = Math.random();
    }

    public World getWorld() {
        return Universe.get().getWorld(UUID);
    }

    public void clear() {
        Archetypes.clear();
        Entities.clear();
    }

    public void resetDrawFlags() {
        DrawAll = false;
        DrawRealtimeElements = false;
        DrawPlayerList = false;
        DrawTeleportersList = false;
        DrawEntitiesList = false;
    }

    public void removeEntity(int entityId) {
        Entities.remove(entityId);

        var world = getWorld();
        var worldData = EntityViewer.getWorldData(world);
        worldData.DrawEntitiesList = true;

        for (var playerRef : world.getPlayerRefs()) {
            var playerData = EntityViewer.getPlayerData(playerRef);
            if (playerData != null) {
                playerData.Book.remove(entityId);
            }
        }

        // todo: remove from the page

        // remove the entity
//        var playerData = getPlayerData();
//        var worldData = playerData.getWorldData();
//        var entityId = Integer.parseInt(data.entityId);
//
//        worldData.Entities.remove(entityId);
//        playerData.Book.remove(entityId);

//        buildEntitiesList(playerData, worldData, commandBuilder, eventBuilder, store);
    }

    public static class EntityChange {
        public EntityChangeType ChangeType;
        public Integer Id;
        public EntityData EntityData;

        public EntityChange (EntityChangeType changeType, Integer id, EntityData entityData) {
            ChangeType = changeType;
            Id = id;
            EntityData = entityData;
        }
    }

    public enum EntityChangeType {
        ADD,
        REMOVE
    }
}