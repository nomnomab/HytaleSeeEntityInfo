package com.nomnom.entityviewer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityData {
    public final int Id;
    public final UUID UUID;
    public final String UUIDString;
    public final String ElementId;

    public String DisplayName;

    public String ModelAssetId;
    public String WorldName;

    public Map<String, String> StaticProperties;
    public Map<String, String> Properties;
    public List<String> Components;

    public EntityData(int id, UUID uuid) {
        Id = id;
        UUID = uuid;
        UUIDString = UUID.toString();
        ElementId = "#Entity" + UUIDString.replace("-", "");
        StaticProperties = new HashMap<>(8);
        Properties = new HashMap<>(8);
    }

    public String getIconPath() {
        if (ModelAssetId != null && !ModelAssetId.isEmpty()) {
            var model = EntityViewer.MODELS.get(ModelAssetId);
            var iconPath = "Pages/Memories/npcs/" + model.getId() + ".png";
            var fullIconPath = "UI/Custom/" + iconPath;
            if (CommonAssetRegistry.hasCommonAsset(fullIconPath)) {
                return iconPath;
            }
        }

        return null;
    }

    public Ref<EntityStore> getRef(World world) {
        if (UUIDString.isEmpty()) {
            return null;
        }

        var store = world.getEntityStore();
        return store.getRefFromUUID(UUID);
    }
}