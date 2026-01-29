package com.nomnom.entityviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EntityViewerBook {
    public List<EntityData> Entities;

    public EntityViewerBook() {
        Entities = new ArrayList<>(512);
    }

    public void clear() {
        Entities.clear();
    }

    public void remove(int entityId) {
        Entities.removeIf(e -> e.Id == entityId);
    }

    public void filter(String filter, WorldData worldData) {
        var entities = worldData.Entities;
        Entities.clear();

        if (filter == null || filter.isEmpty()) {
            for (var entity : entities.values()) {
                if (entity.WorldName.equals(worldData.Name)) {
                    Entities.add(entity);
                }
            }
            return;
        }

        // no prefix = name
        // c         = component
        // uuid      = uuid
        var prefix = filter.contains(":") ? filter.substring(0, filter.indexOf(":")) : null;
        var value = prefix == null ? filter : filter.substring(filter.indexOf(":") + 1);
        for (var entity : entities.values()) {
            if (prefix == null) {
                if (entity.DisplayName != null && entity.DisplayName.contains(value)) {
                    Entities.add(entity);
                }

                continue;
            }

            switch (prefix) {
                case "c": {
                    for (var component : entity.Components) {
                        if (component.contains(value)) {
                            Entities.add(entity);
                            break;
                        }
                    }
                    break;
                }

                case "uuid": {
                    if (entity.UniqueIdString != null && entity.UniqueIdString.contains(value)) {
                        Entities.add(entity);
                    }
                    break;
                }
            }
        }
    }
}