package com.nomnom.entityviewer.systems;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.entityviewer.*;
import com.nomnom.entityviewer.ui.EntityViewerPage;
import com.nomnom.entityviewer.ui.PageSignals;
import com.nomnom.entityviewer.ui.ValueToString;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.text.NumberFormat;
import java.util.*;

public class EntityViewerSystem extends TickingSystem<EntityStore> {
    private static final NumberFormat _numberFormat = NumberFormat.getInstance();

    private static double _updateRealtimeElementsTimer;
    private final List<EntityViewerPage> _pageCache = new ArrayList<>(16);

    public EntityViewerSystem() {
        _numberFormat.setMaximumFractionDigits(2);
    }

    @Override
    public void tick(float dt, int index, @NonNullDecl Store<EntityStore> store) {
        updateWorld(dt, store);
    }

    void updateWorld(float dt, @NonNullDecl Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var worldData = EntityViewer.getWorldData(world);
        if (worldData == null) return;

        updateRealtimeElements(dt, worldData, store);

        worldData.ValidateTimer += dt;

        // clear the world database and remake it from scratch
        if (worldData.RebuildEntityLookup) {
            worldData.RebuildEntityLookup = false;
            worldData.DrawAll = true;
            fullRebuild(worldData, store);
            return;
        }

        updateDraw(worldData, store);
    }

    void updateDraw(WorldData worldData, @NonNullDecl Store<EntityStore> store) {
        if (worldData.DrawAll) {
            worldData.resetDrawFlags();
            PageSignals.rebuildPages(worldData);
            return;
        }

        if (worldData.DrawPlayerList) {
            worldData.DrawPlayerList = false;
            // todo
        } else if (worldData.DrawTeleportersList) {
            worldData.DrawTeleportersList = false;
            // todo
        } else if (worldData.DrawEntitiesList) {
            worldData.DrawEntitiesList = false;
            PageSignals.buildEntitiesLists(worldData);
        } else if (!worldData.EntityChanges.isEmpty()) {
            EntityViewer.log("world " + worldData.Name + " has " + worldData.EntityChanges.size() + " changes.");

            _pageCache.clear();
            for (var playerRef : worldData.getWorld().getPlayerRefs()) {
                var playerData = EntityViewer.getPlayerData(playerRef);
                if (playerData == null) continue;
                if (playerData.Page == null) continue;

                _pageCache.add(playerData.Page);
            }

            for (var page : _pageCache) {
                var uiCommandBuilder = new UICommandBuilder();
                var uiEventBuilder = new UIEventBuilder();

                for (var change : worldData.EntityChanges) {
                    var entityData = change.EntityData;
                    if (entityData == null) {
                        entityData = worldData.Entities.get(change.Id);
                    }
                    if (entityData == null) continue;

                    switch (change.ChangeType) {
                        case ADD -> page.addEntity(entityData, uiCommandBuilder, uiEventBuilder);
                        case REMOVE -> page.removeEntity(entityData, uiCommandBuilder, uiEventBuilder);
                    }
                }

                page.update(uiCommandBuilder, uiEventBuilder);
            }

            worldData.EntityChanges.clear();
            _pageCache.clear();
        }
    }

    void updateRealtimeElements(float dt, WorldData worldData, @NonNullDecl Store<EntityStore> store) {
        // todo: wait for Hytale to support this without eating inputs
        return;

//        _updateRealtimeElementsTimer += dt;
//        if (!worldData.DrawRealtimeElements && !(_updateRealtimeElementsTimer >= 0.1)) {
//            return;
//        }
//        _updateRealtimeElementsTimer = 0.0;
//        worldData.DrawRealtimeElements = false;
//
//        var world = store.getExternalData().getWorld();
//        for (var player : world.getPlayerRefs()) {
//            var playerData = EntityViewer.getPlayerData(player);
//            if (playerData == null) continue;
//            if (playerData.Page == null) continue;
////            if (!playerData.Page.canUpdate()) continue;
//
//            updateRealtimeEntityData(playerData, store);
//            playerData.Page.buildRealtimeElements();
//        }
    }

    private static void fullRebuild(@NonNullDecl WorldData worldData, @NonNullDecl Store<EntityStore> store) {
        worldData.clear();
        worldData.Chunks = store.collectArchetypeChunkData();

        var world = worldData.getWorld();
        // remake the database
        store.forEachChunk((chunk, cmd) -> {
            for (int i = 0; i < chunk.size(); i++) {
                var entityRef = chunk.getReferenceTo(i);
                worldData.addEntity(entityRef, store);
            }
        });

        worldData.DrawAll = true;

        EntityViewer.log("Rebuild for world " + world.getName() + " resulted in " + worldData.Entities.size() + " entities");
    }

    public static void updateRealtimeEntityData(PlayerData playerData, @NonNullDecl Store<EntityStore> store) {
        if (playerData.SelectedEntityId == -1) return;
        if (playerData.Page == null) return;

        var entityData = playerData.getSelectedEntityData();
        if (entityData == null) return;

        var entityRef = entityData.getRef(store.getExternalData().getWorld());
        entityData.Properties.clear();

        if (entityRef == null) return;

        for (var component : entityData.Components) {
            // if this is a DisplayNameComponent, push into the field
            switch (component) {
                case "TransformComponent" -> {
                    var transform = store.getComponent(entityRef, TransformComponent.getComponentType());
                    if (transform != null) {
                        var pos = transform.getPosition();
                        entityData.Properties.put("position", ValueToString.Vector3d(pos));

                        var rot = transform.getRotation();
                        entityData.Properties.put("rotation", ValueToString.Vector3f(rot));
                    }
                }
            }
        }
    }

    private static final List<Integer> _entitiesToRemove = new ArrayList<>();
    public static void checkForInvalidEntities(@NonNullDecl WorldData worldData, @NonNullDecl Store<EntityStore> store) {
        _entitiesToRemove.clear();

        for (var entity : worldData.Entities.values()) {
            try {
                var uuid = entity.UniqueId;
                var world = store.getExternalData().getWorld();
                var entityRef = world.getEntityRef(uuid);
                if  (entityRef == null || !entityRef.isValid()) {
                    _entitiesToRemove.add(entity.Id);
                }
            } catch (Exception _) {
                _entitiesToRemove.add(entity.Id);
            }
        }

//        var hadOne = !_entitiesToRemove.isEmpty();
        for (var remove : _entitiesToRemove) {
            var entity = worldData.Entities.get(remove);
            EntityViewer.log("[" + worldData.Name + "] Removing invalid entity: " + entity.UniqueId);

            worldData.Entities.remove(remove);
            PageSignals.onDestroyEntity(worldData, entity);
        }

        _entitiesToRemove.clear();

//        if (hadOne) {
//            worldData.DrawAll = true;
//        }
    }
}
