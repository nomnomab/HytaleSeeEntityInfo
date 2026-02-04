package com.nomnom.seeentityinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EntityViewerBook {
    public List<EntityData> Entities;

    public EntityViewerBook() {
        Entities = new ArrayList<>(512);
    }

    public void clear() {
        Entities.clear();
    }

    public void remove(UUID uuid) {
        Entities.removeIf(e -> e.UUID.equals(uuid));
    }

    public boolean hasElementId(String elementId) {
        for (var e : Entities) {
            if (e.ElementId.equals(elementId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canFilter(String filter, WorldData worldData, EntityData entityData) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }

        // no prefix = name
        // c         = component
        // uuid      = uuid
        var prefix = filter.contains(":") ? filter.substring(0, filter.indexOf(":")) : null;
        var value = prefix == null ? filter : filter.substring(filter.indexOf(":") + 1);
        if (prefix == null) {
            return entityData.DisplayName != null && containsIgnoreCase(entityData.DisplayName, value);
        }

        switch (prefix) {
            case "c": {
                for (var component : entityData.Components) {
                    if (containsIgnoreCase(component, value)) {
                        return true;
                    }
                }
                break;
            }

            case "uuid": {
                // case-sensitive
                if (entityData.UUIDString.startsWith(value)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void filter(String filter, WorldData worldData) {
        var entities = worldData.Entities;
        Entities.clear();

        if (filter == null || filter.isEmpty()) {
            for (var entity : entities.values()) {
                if (equalsIgnoreCase(entity.WorldName, worldData.Name)) {
                    Entities.add(entity);
                }
            }
            return;
        }

        for (var entity : entities.values()) {
            if (canFilter(filter, worldData, entity)) {
                Entities.add(entity);
            }
        }
    }

    // Source - https://stackoverflow.com/a/25379180
    // Posted by icza, modified by community. See post 'Timeline' for change history
    // Retrieved 2026-02-04, License - CC BY-SA 4.0
    static boolean containsIgnoreCase(String src, String what) {
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }

    static boolean equalsIgnoreCase(String src, String what) {
        if (src == null || what == null)
            return Objects.equals(src, what);

        return src.regionMatches(true, 0, what, 0, what.length()) && src.length() == what.length();
    }

}