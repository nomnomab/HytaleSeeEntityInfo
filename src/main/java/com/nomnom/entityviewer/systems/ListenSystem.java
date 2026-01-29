package com.nomnom.entityviewer.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.entityviewer.EntityViewer;
import com.nomnom.entityviewer.ui.PageSignals;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class ListenSystem extends RefSystem<EntityStore> {
    @Override
    public void onEntityAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        EntityViewer.log("entity added");
//        PageSignals.rebuildAllEntityLookups();

        var index = ref.getIndex();
        var world = store.getExternalData().getWorld();
        var worldData = EntityViewer.getWorldData(world);
        var entity = worldData.Entities.get(index);
        if (entity == null) {
//            EntityViewer.warn("Failed to get entity at index " + index);
            return;
        }

        var uuidFrom = store.getComponent(ref, UUIDComponent.getComponentType()).getUuid();
        var uuidTo = entity.UniqueId;

        if (!uuidFrom.equals(uuidTo)) {
            EntityViewer.warn("UUIDs don't match! " + uuidFrom + " != " + uuidTo);
            return;
        }

        PageSignals.onCreateEntity(worldData, entity);
    }

    @Override
    public void onEntityRemove(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return UUIDComponent.getComponentType();
    }
}