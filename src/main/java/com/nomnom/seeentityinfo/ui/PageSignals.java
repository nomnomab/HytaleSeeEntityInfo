package com.nomnom.seeentityinfo.ui;

import com.hypixel.hytale.server.core.universe.world.World;
import com.nomnom.seeentityinfo.EntityData;
import com.nomnom.seeentityinfo.SeeEntityInfo;
import com.nomnom.seeentityinfo.WorldData;

/// Holds useful functions to make updating the EntityViewer page
/// easier!
public class PageSignals {
    /// Rebuilds all entity lookups across all worlds.
    public static void rebuildAllEntityLookups() {
        for (var world : SeeEntityInfo.Worlds.values()) {
            world.RebuildEntityLookup = true;
        }
    }

    /// Rebuilds the entity lookup for one world.
    public static void rebuildEntityLookup(World world) {
        var worldData = SeeEntityInfo.getWorldData(world);
        if (worldData == null) return;

        worldData.RebuildEntityLookup = true;
    }

    public static void onCreateEntity(WorldData worldData, EntityData entityData) {
        SeeEntityInfo.logDebug("onCreateEntity: uuid: " + entityData.UUID);

        worldData.EntityChanges.add(new WorldData.EntityChange(WorldData.EntityChangeType.ADD, entityData.UUID, entityData));
    }

    public static void onDestroyEntity(WorldData worldData, EntityData entityData) {
        SeeEntityInfo.logDebug("onDestroyEntity: uuid: " + entityData.UUID);

        worldData.EntityChanges.add(new WorldData.EntityChange(WorldData.EntityChangeType.REMOVE, entityData.UUID, entityData));
    }

    /// Rebuild all pages.
    public static void rebuildPages() {
        for (var player : SeeEntityInfo.Players.values()) {
            player.rebuildPage();
        }
    }

    /// Rebuild all pages for a specific world.
    public static void rebuildPages(WorldData worldData) {
        SeeEntityInfo.logDebug("rebuildPages: " + worldData.EntityChanges.size());
        for (var playerRef : worldData.getWorld().getPlayerRefs()) {
            var playerData = SeeEntityInfo.getPlayerData(playerRef);
            if (playerData == null) continue;

            SeeEntityInfo.logDebug("rebuilding for player " + playerData.UUID.toString());
            playerData.rebuildPage();
        }
    }

    /// Update all page realtime elements.
    public static void drawRealtimeElements() {
        for (var player : SeeEntityInfo.Players.values()) {
            player.buildRealtimeElements();
        }
    }

    /// Enqueues the draw of the player lists.
    public static void drawAllPlayerLists() {
        for (var world : SeeEntityInfo.Worlds.values()) {
            world.DrawPlayerList = true;
        }
    }

    /// Enqueues the draw of the teleporter lists.
    public static void drawAllTeleporterLists() {
        for (var world : SeeEntityInfo.Worlds.values()) {
            world.DrawTeleportersList = true;
        }
    }

    /// Enqueues the draw of the teleporter lists.
    public static void drawAllEntitiesLists() {
        for (var world : SeeEntityInfo.Worlds.values()) {
            world.DrawEntitiesList = true;
        }
    }

    /// Enqueues the draw of the teleporter lists.
    public static void buildEntitiesLists(WorldData worldData) {
        for (var playerRef : worldData.getWorld().getPlayerRefs()) {
            var playerData = SeeEntityInfo.getPlayerData(playerRef);
            if (playerData == null) continue;

            playerData.buildEntitiesList();
        }
    }
}
