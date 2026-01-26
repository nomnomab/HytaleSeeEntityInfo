package com.nomnom.entityviewer.ui;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.entityviewer.*;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.*;

public class EntityViewerSystem extends TickingSystem<EntityStore> {
    public EntityViewerSystem() {}

    @Override
    public void tick(float dt, int index, @NonNullDecl Store<EntityStore> store) {
        updateDatabase(dt, store);
    }

    void updateDatabase(float dt, @NonNullDecl Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var worldData = EntityViewer.getInstance().getWorldData(world);
        if (worldData == null) return;

        if (!worldData.World.isAlive() || worldData.World.isPaused()) {
            return;
        }

        if (worldData.ValidateTimer >= 5.0) {
            // check for invalid entities, such as non-valid or missing
            worldData.ValidateTimer = 0;
            checkForInvalidEntities(worldData, store);
        } else if (worldData.WantsFullRebuild) {
            // clear the world database and remake it from scratch
            worldData.WantsFullRebuild = false;
            fullRebuild(worldData, store);
        } else if (worldData.WantsPageRebuild) {
            // redraw all player pages in this world
            worldData.WantsPageRebuild = false;
            repaintPages(worldData, store);
        } else {
            worldData.ValidateTimer += dt;
        }
    }

    private static void fullRebuild(@NonNullDecl WorldData worldData, @NonNullDecl Store<EntityStore> store) {
        worldData.Chunks = store.collectArchetypeChunkData();
        worldData.Archetypes.clear();
        worldData.Entities.clear();

        // remake the database
        store.forEachChunk((chunk, cmd) -> {
            var archetype = chunk.getArchetype();
            var archetypeData = new ArchetypeData();

            for (int i = 0; i < chunk.size(); i++) {
                var entityRef = chunk.getReferenceTo(i);
                var entityId = entityRef.getIndex();

                archetypeData.Entities.add(entityId);

                var entityData = new EntityData(entityId);
                entityData.WorldName = worldData.Name;

                entityData.Components = new ArrayList<>();
                for (int j = archetype.getMinIndex(); j < archetype.length(); j++) {
                    var compType = archetype.get(j);
                    if (compType != null) {
                        var componentName = TypeNameUtil.getSimpleName(compType.getTypeClass().getName());
                        entityData.Components.add(componentName);

                        // if this is a DisplayNameComponent, push into the field
                        if (componentName.equals("DisplayNameComponent")) {
                            var displayName = store.getComponent(entityRef, DisplayNameComponent.getComponentType());
                            if (displayName != null && displayName.getDisplayName() != null) {
                                entityData.DisplayName = displayName.getDisplayName().getRawText();
                            }
                        } else if (componentName.equals("UUIDComponent")) {
                            var uuid = store.getComponent(entityRef, UUIDComponent.getComponentType());
                            if (uuid != null) {
                                entityData.UniqueId = uuid.getUuid();
                                entityData.UniqueIdString = uuid.toString();
                            }
                        } else if (componentName.equals("ModelComponent")) {
                            var model = store.getComponent(entityRef, ModelComponent.getComponentType());
                            if (model != null) {
                                var rawModel = model.getModel();
                                entityData.Properties.put("model", rawModel.getModel());

                                entityData.ModelAssetId = rawModel.getModelAssetId();
                                entityData.Properties.put("model_asset_id", entityData.ModelAssetId);
                            }
                        } else if (componentName.equals("Nameplate")) {
                            var nameplate = store.getComponent(entityRef, Nameplate.getComponentType());
                            if (nameplate != null) {
                                entityData.Properties.put("nameplate", nameplate.getText());
                            }
                        } else if (componentName.equals("TransformComponent")) {
                            var transform = store.getComponent(entityRef, TransformComponent.getComponentType());
                            if (transform != null) {
                                entityData.Properties.put("position", transform.getPosition().toString());
                                entityData.Properties.put("rotation", transform.getRotation().toString());
                            }
                        }
                    }
                }

                worldData.Entities.put(entityId, entityData);
            }

            worldData.Archetypes.add(archetypeData);
        });

        // when a full rebuild happens, reset the player page since it may be too large now
        for (var player : EntityViewer.getInstance().Players.values()) {
            player.PageIndex = 0;
        }

        worldData.WantsPageRebuild = true;

        EntityViewer.log("Rebuild for world " + worldData.World.getName() + " resulted in " + worldData.Entities.size() + " entities");
    }

    private static final List<Integer> _entitiesToRemove = new ArrayList<>();
    public static void checkForInvalidEntities(@NonNullDecl WorldData worldData, @NonNullDecl Store<EntityStore> store) {
//        EntityViewer.log("[" + worldData.Name + "] Checking for invalid entities...");

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

        var hadOne = !_entitiesToRemove.isEmpty();
        for (var remove : _entitiesToRemove) {
            EntityViewer.log("[" + worldData.Name + "] Removing invalid entity: " + worldData.Entities.get(remove).UniqueId);
            worldData.Entities.remove(remove);
        }

        _entitiesToRemove.clear();

//        EntityViewer.log("[" + worldData.Name + "] Done checking for invalid entities!");
        if (hadOne) {
            worldData.WantsPageRebuild = true;
        }
    }

    private static void repaintPages(WorldData worldData, @NonNullDecl Store<EntityStore> store) {
        EntityViewer.log("[" + worldData.Name + "] Repainting pages for world: " + store.getExternalData().getWorld().getName());

        for (var playerData : EntityViewer.getInstance().Players.values()) {
            var world = playerData.getWorld();
            var playerRef = playerData.PlayerRef;
            if (playerRef == null || !playerRef.isValid()) {
                EntityViewer.warn("[" + world.getName() + "] PlayerRef " + playerRef + " is invalid");
                continue;
            }

            world.execute(() -> {
                var worldStore = world.getEntityStore().getStore();
                var player = worldStore.getComponent(Objects.requireNonNull(playerRef.getReference()), Player.getComponentType());
                if ((player == null) || !(player.getReference() != null && player.getReference().isValid())) {
                    EntityViewer.warn("[" + world.getName() + "] Player " + playerRef + " is invalid");
                    return;
                }

                var page = player.getPageManager().getCustomPage();
                if (page instanceof EntityViewerUi viewerUi) {
                    EntityViewer.log("[" + world.getName() + "] Repainting page for " + player.getDisplayName());
                    viewerUi.buildContent();
                } else {
                    EntityViewer.log("[" + world.getName() + "] page wasn't entity viewer");
                }
            });
        }

        EntityViewer.log("[" + worldData.Name + "] Done repainting pages!");
    }
}
