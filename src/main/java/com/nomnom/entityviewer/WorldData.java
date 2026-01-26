package com.nomnom.entityviewer;

import com.hypixel.hytale.component.metric.ArchetypeChunkData;
import com.hypixel.hytale.server.core.universe.world.World;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;

public class WorldData {
    public final World World;
    public final String Name;

    public volatile ArchetypeChunkData[] Chunks;
    public volatile List<ArchetypeData> Archetypes;
    public volatile Map<Integer, EntityData> Entities;

    public boolean WantsPageRebuild;
    public boolean WantsFullRebuild;
    public double ValidateTimer;

    public WorldData(World world) {
        World = world;
        Name = world.getName();

        Chunks = new ArchetypeChunkData[0];
        Archetypes = new ObjectArrayList<>(256);
        Entities = new HashMap<>(256);

        ValidateTimer = Math.random();
    }
}
