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

        // for now just filter by any of the strings
        for (var entity : entities.values()) {
            if (!entity.WorldName.equals(worldData.Name)) {
                continue;
            }

            if (entity.DisplayName != null && entity.DisplayName.contains(filter)) {
                Entities.add(entity);
                continue;
            }

            if (entity.UniqueIdString != null && entity.UniqueIdString.contains(filter)) {
                Entities.add(entity);
                continue;
            }

            var found = false;
            for (var value : entity.Properties.values()) {
                if (value.contains(filter)) {
                    Entities.add(entity);
                    found = true;
                    break;
                }
            }

            if (found) continue;

            for (var component : entity.Components) {
                if (component.contains(filter)) {
                    Entities.add(entity);
                    found = true;
                    break;
                }
            }

            if (found) continue;

            // not found
        }
    }

//    public Page getPage(int index, int perPage) {
//        EntityViewer.getInstance().getLogger().atInfo().log("getPage");
//
//        var start = index * perPage;
//        var end = start + perPage;
//
//        end = Math.min(end, Entities.size());
//        start = Math.min(start, end);
//
//        return new Page(start, end);
//    }

    public void iterPage(int index, int perPage, Consumer<EntityData> consumer) {
        var start = index * perPage;
        var end = start + perPage;

        end = Math.min(end, Entities.size());
        start = Math.min(start, end);

        if (start == end) return;

        for (int i = start; i < end; i++) {
            var entity = Entities.get(i);
            consumer.accept(entity);
        }
    }

//    public static class Page {
//        public int Start, End;
//
//        public Page(int start, int end) {
//            this.Start = start;
//            this.End = end;
//        }
//    }
}