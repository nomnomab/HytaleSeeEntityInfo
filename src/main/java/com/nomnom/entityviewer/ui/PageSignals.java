package com.nomnom.entityviewer.ui;

import com.hypixel.hytale.server.core.universe.world.World;
import com.nomnom.entityviewer.EntityData;
import com.nomnom.entityviewer.EntityViewer;
import com.nomnom.entityviewer.WorldData;

/// Holds useful functions to make updating the EntityViewer page
/// easier!
public class PageSignals {
    /// Rebuilds all entity lookups across all worlds.
    public static void rebuildAllEntityLookups() {
        for (var world : EntityViewer.Worlds.values()) {
            world.RebuildEntityLookup = true;
        }
    }

    /// Rebuilds the entity lookup for one world.
    public static void rebuildEntityLookup(World world) {
        var worldData = EntityViewer.getWorldData(world);
        if (worldData == null) return;

        worldData.RebuildEntityLookup = true;
    }

    public static void onCreateEntity(WorldData worldData, EntityData entityData) {
        EntityViewer.log("onCreateEntity: " + entityData.Id + ", uuid: " + entityData.UniqueId);

        worldData.EntityChanges.add(new WorldData.EntityChange(WorldData.EntityChangeType.ADD, entityData.Id, entityData));
    }

    public static void onDestroyEntity(WorldData worldData, EntityData entityData) {
        EntityViewer.log("onDestroyEntity: " + entityData.Id + ", uuid: " + entityData.UniqueId);

        worldData.EntityChanges.add(new WorldData.EntityChange(WorldData.EntityChangeType.REMOVE, entityData.Id, entityData));
    }

    /// Rebuild all pages.
    public static void rebuildPages() {
        for (var player : EntityViewer.Players.values()) {
            player.rebuildPage();
        }
    }

    /// Rebuild all pages for a specific world.
    public static void rebuildPages(WorldData worldData) {
        for (var playerRef : worldData.getWorld().getPlayerRefs()) {
            var playerData = EntityViewer.getPlayerData(playerRef);
            if (playerData == null) continue;

            playerData.rebuildPage();
        }
    }

    /// Update all page realtime elements.
    public static void drawRealtimeElements() {
        for (var player : EntityViewer.Players.values()) {
            player.buildRealtimeElements();
        }
    }

    /// Enqueues the draw of the player lists.
    public static void drawAllPlayerLists() {
        for (var world : EntityViewer.Worlds.values()) {
            world.DrawPlayerList = true;
        }
    }

    /// Enqueues the draw of the teleporter lists.
    public static void drawAllTeleporterLists() {
        for (var world : EntityViewer.Worlds.values()) {
            world.DrawTeleportersList = true;
        }
    }

    /// Enqueues the draw of the teleporter lists.
    public static void drawAllEntitiesLists() {
        for (var world : EntityViewer.Worlds.values()) {
            world.DrawEntitiesList = true;
        }
    }

    /// Enqueues the draw of the teleporter lists.
    public static void buildEntitiesLists(WorldData worldData) {
        for (var playerRef : worldData.getWorld().getPlayerRefs()) {
            var playerData = EntityViewer.getPlayerData(playerRef);
            if (playerData == null) continue;

            playerData.buildEntitiesList();
        }
    }
}
