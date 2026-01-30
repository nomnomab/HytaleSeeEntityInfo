package com.nomnom.entityviewer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.metric.ArchetypeChunkData;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class WorldData {
    public final UUID UUID;
    public final String Name;

    public volatile ArchetypeChunkData[] Chunks;
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
        Entities = new HashMap<>(512);

        EntityChanges = new ArrayList<>(256);

        ValidateTimer = Math.random();
    }

    public World getWorld() {
        return Universe.get().getWorld(UUID);
    }

    public void clear() {
        Entities.clear();
    }

    public void resetDrawFlags() {
        DrawAll = false;
        DrawRealtimeElements = false;
        DrawPlayerList = false;
        DrawTeleportersList = false;
        DrawEntitiesList = false;
    }

    public EntityData getEntityFromUUID(UUID uuid) {
        for (var entity : Entities.values()) {
            if (entity.UniqueId.equals(uuid)) {
                return entity;
            }
        }

        return null;
    }

    public EntityData addEntity(Ref<EntityStore> entityRef, Store<EntityStore> store) {
        var entityId = entityRef.getIndex();

        var entityData = Entities.get(entityId);
        if (entityData == null) {
            entityData = new EntityData(entityId);
        }
        entityData.WorldName = Name;

        // collect components
        if (entityData.Components != null) {
            entityData.Components.clear();
        } else {
            entityData.Components = new ArrayList<>();
        }

        var archetype = entityRef.getStore().getArchetype(entityRef);
        for (int j = archetype.getMinIndex(); j < archetype.length(); j++) {
            var compType = archetype.get(j);
            if (compType != null) {
                var componentName = TypeNameUtil.getSimpleName(compType.getTypeClass().getName());
                entityData.Components.add(componentName);

                // can check componentName.equals then store.getComponent
                // if this is a DisplayNameComponent, push into the field
                switch (componentName) {
                    case "DisplayNameComponent" -> {
                        var displayName = store.getComponent(entityRef, DisplayNameComponent.getComponentType());
                        if (displayName != null && displayName.getDisplayName() != null) {
                            entityData.DisplayName = displayName.getDisplayName().getRawText();
                        }
                    }
                    case "ModelComponent" -> {
                        var model = store.getComponent(entityRef, ModelComponent.getComponentType());
                        if (model != null) {
                            var rawModel = model.getModel();
                            entityData.StaticProperties.put("model", rawModel.getModel());

                            entityData.ModelAssetId = rawModel.getModelAssetId();
                            entityData.StaticProperties.put("model_asset_id", entityData.ModelAssetId);
                        }
                    }
                    case "Nameplate" -> {
                        var nameplate = store.getComponent(entityRef, Nameplate.getComponentType());
                        if (nameplate != null) {
                            entityData.StaticProperties.put("nameplate", nameplate.getText());
                        }
                    }
                }
            }
        }

        // get uuid if possible
        if (entityData.UniqueIdString == null) {
            var uuid = store.getComponent(entityRef, UUIDComponent.getComponentType());
            if (uuid != null) {
                entityData.UniqueId = uuid.getUuid();
                entityData.UniqueIdString = entityData.UniqueId.toString();
            }
        }

        // show the display name if possible
        if (entityData.DisplayName == null || entityData.DisplayName.isEmpty()) {
            if (entityData.ModelAssetId != null) {
                entityData.DisplayName = entityData.ModelAssetId;
            } else {
                entityData.DisplayName = entityData.UniqueIdString;
            }
        }

        Entities.put(entityId, entityData);
        return entityData;
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