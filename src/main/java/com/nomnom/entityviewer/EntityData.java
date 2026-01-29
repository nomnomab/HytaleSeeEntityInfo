package com.nomnom.entityviewer;

import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityData {
    public final int Id;

    public String DisplayName;
    public UUID UniqueId;
    public String UniqueIdString;
    public String ElementId;

    public String ModelAssetId;
    public String WorldName;

    public Map<String, String> Properties;
    public List<String> Components;

    public EntityData(int id) {
        Id = id;
        ElementId = "#Entity" + id;
        Properties = new HashMap<>();
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
}