package com.nomnom.entityviewer.ui;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.entityviewer.EntityData;
import com.nomnom.entityviewer.EntityViewer;
import com.nomnom.entityviewer.PlayerData;
import com.nomnom.entityviewer.WorldData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EntityViewerPage extends InteractiveCustomUIPage<EntityViewerPage.Data> {
    public EntityViewerPage(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime) {
        super(playerRef, lifetime, Data.CODEC);

        EntityViewer.registerPlayer(playerRef);
        getPlayerData().Page = this;
        EntityViewer.log("Player " + playerRef.getUsername() + " opened page");
    }

    @Override
    protected void close() {
        super.close();

        cleanup();
    }

    @Override
    public void onDismiss(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store) {
        super.onDismiss(ref, store);

        cleanup();
    }

    void cleanup() {
        var playerData = getPlayerData();
        if (playerData.Page == this) {
            playerData.Page = null;
        }

        EntityViewer.log("Player " + playerRef.getUsername() + " closed page");
    }

    public void fullRebuild() {
        EntityViewer.log("Full Rebuild");
        this.rebuild();
    }

    public void update(@Nullable UICommandBuilder commandBuilder, @Nullable UIEventBuilder eventBuilder) {
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    @Override
    protected void rebuild() {
        super.rebuild();
        getPlayerData().Page = this;
    }

    PlayerData getPlayerData() {
        var uuid = playerRef.getUuid();
        return EntityViewer.Players.get(uuid);
    }

    WorldTimeResource getWorldTime(Store<EntityStore> store) {
        return store.getResource(WorldTimeResource.getResourceType());
    }

    // builds the page from scratch
    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        // need to fetch the current entities
        var playerData = getPlayerData();
        var worldData = playerData.getSelectedWorldData();
        playerData.Book.filter(playerData.Filter, worldData);

        uiCommandBuilder.append("Pages/EntityViewer/MainPanel.ui");

        uiCommandBuilder.set("#SearchInput.Value", playerData.Filter);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                "#SearchInput",
                EventData.of("@SearchInput", "#SearchInput.Value"),
                false
        );

        // build panels
        try {
            buildLeftPanel(playerData, worldData, ref, uiCommandBuilder, uiEventBuilder, store);
            buildMiddlePanel(playerData, worldData, ref, uiCommandBuilder, uiEventBuilder, store);
            buildRightPanel(playerData, worldData, ref, uiCommandBuilder, uiEventBuilder, store);

            buildRealtimeElements(playerData, ref, uiCommandBuilder, uiEventBuilder);
        } catch (Exception e) {
            EntityViewer.err("error building EntityViewer page, e: " + e);
        }
    }

    // world data
    void buildLeftPanel(@NonNullDecl PlayerData playerData, @NonNullDecl WorldData worldData, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        buildWorldDetails(playerData, worldData, ref, uiCommandBuilder, uiEventBuilder, store);
        buildPlayersList(worldData, uiCommandBuilder, uiEventBuilder, store);
        buildTeleportersList(worldData, uiCommandBuilder, uiEventBuilder, store);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#ReloadButton",
                EventData.of("Button", "Reload")
        );

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#WorldTpButton",
                EventData.of("Button", "World_GoTo")
        );
    }

    void buildWorldDetails(@NonNullDecl PlayerData playerData, @NonNullDecl WorldData worldData, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        // world dropdown
        var worldDropdownOptions = new ArrayList<DropdownEntryInfo>();
        for (var world : EntityViewer.Worlds.keySet()) {
            worldDropdownOptions.add(new DropdownEntryInfo(LocalizableString.fromString(world), world));
        }
        uiCommandBuilder.set("#WorldDropdown.Entries", worldDropdownOptions);
        uiCommandBuilder.set("#WorldDropdown.Value", playerData.SelectedWorldName);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                "#WorldDropdown",
                EventData.of("Button", "World_Switch")
                        .append("@DropdownValue", "#WorldDropdown.Value"));

        // world details
        var worldConfig = worldData.getWorld().getWorldConfig();
        uiCommandBuilder.clear("#WorldPropertiesList");

        var worldName = worldConfig.getDisplayName();
        if (worldName == null || worldName.isEmpty()) {
            worldName = worldData.Name;
        }
        appendEntityPropertyItem("#WorldPropertiesList", uiCommandBuilder);
        setEntityPropertyItem("#WorldPropertiesList[0]", "name", worldName, uiCommandBuilder);

        appendEntityPropertyItem("#WorldPropertiesList", uiCommandBuilder);
        setEntityPropertyItem("#WorldPropertiesList[1]", "uuid", worldConfig.getUuid().toString(), uiCommandBuilder);

        appendEntityPropertyItem("#WorldPropertiesList", uiCommandBuilder);
        setEntityPropertyItem("#WorldPropertiesList[2]", "time", "-", uiCommandBuilder);

        appendEntityPropertyItem("#WorldPropertiesList", uiCommandBuilder);
        setEntityPropertyItem("#WorldPropertiesList[3]", "last updated", "-", uiCommandBuilder);
    }

    void buildPlayersList(@NonNullDecl WorldData worldData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        var world = worldData.getWorld();
        if (world == null) return;

        var playerRefs = world.getPlayerRefs();
        uiCommandBuilder.set("#WorldPlayersCount #CountText.Text", String.valueOf(playerRefs.size()));

        uiCommandBuilder.clear("#WorldPlayersList");

        var index = 0;
        for (var playerRef : playerRefs) {
            var playerName = playerRef.getUsername();

            uiCommandBuilder.append("#WorldPlayersList", "Pages/EntityViewer/ListItem.ui");
            uiCommandBuilder.set("#WorldPlayersList[" + index + "] #Name.Text", playerName);

//            if (playerRef != this.playerRef) {
                uiCommandBuilder.append("#WorldPlayersList[" + index + "]", "Pages/EntityViewer/InlineTextButton.ui");
                uiCommandBuilder.set("#WorldPlayersList[" + index + "] #InlineTextButton.Text", "tp");

                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                        "#WorldPlayersList[" + index + "] #InlineTextButton",
                        EventData.of("Button", "Player_GoTo")
                                .append("EntityId", playerRef.getUuid().toString())
                );
//            }

            index++;
        }
    }

    void buildTeleportersList(@NonNullDecl WorldData worldData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        var world = worldData.getWorld();
        if (world == null) return;

        var warps = TeleportPlugin.get().getWarps().values();
        uiCommandBuilder.clear("#WorldTeleportersList");

        var index = 0;
        for (var warp : warps) {
            // restrict warps to this world only
            if (!warp.getWorld().equals(worldData.Name)) {
                continue;
            }

            uiCommandBuilder.append("#WorldTeleportersList", "Pages/EntityViewer/ListItem.ui");
            uiCommandBuilder.set("#WorldTeleportersList[" + index + "] #Name.Text", warp.getId());

            uiCommandBuilder.append("#WorldTeleportersList[" + index + "]", "Pages/EntityViewer/InlineTextButton.ui");
            uiCommandBuilder.set("#WorldTeleportersList[" + index + "] #InlineTextButton.Text", "tp");

            uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                    "#WorldTeleportersList[" + index + "] #InlineTextButton",
                    EventData.of("Button", "Teleporter_Warp")
                            .append("Warp", warp.getId())
            );

            index++;
        }
        uiCommandBuilder.set("#WorldTeleportersCount #CountText.Text", String.valueOf(index));
    }

    // entity list
    void buildMiddlePanel(@NonNullDecl PlayerData playerData, @NonNullDecl WorldData worldData, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        buildEntitiesList(playerData, worldData, uiCommandBuilder, uiEventBuilder, store);
    }

    void buildEntitiesList(@NonNullDecl PlayerData playerData, @NonNullDecl WorldData worldData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        var entities = playerData.Book.Entities;
        uiCommandBuilder.set("#EntitiesCount #CountText.Text", String.valueOf(entities.size()));

        uiCommandBuilder.clear("#EntitiesList");

        for (var entity : entities) {
            createEntityListItem(entity, worldData, false, uiCommandBuilder, uiEventBuilder);
        }
    }

    // selected entity data
    void buildRightPanel(@NonNullDecl PlayerData playerData, @NonNullDecl WorldData worldData, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        var selectedEntity = playerData.getSelectedEntityData();
        buildSelectedEntity(selectedEntity, uiCommandBuilder, uiEventBuilder);
    }

    // dynamic parts

    // builds only the realtime elements
    public void buildRealtimeElements() {
        var playerData = getPlayerData();
        var playerRef = playerData.getPlayerRef();
        if (playerRef == null) return;

        var ref = playerRef.getReference();
        if (ref == null) return;

        var uiCommandBuilder = new UICommandBuilder();
        var uiEventBuilder = new UIEventBuilder();
        buildRealtimeElements(playerData, ref, uiCommandBuilder, uiEventBuilder);

        this.sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    public void buildRealtimeElements(PlayerData playerData, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
        buildWorldDetails(playerData, ref, uiCommandBuilder, uiEventBuilder);

        var store = ref.getStore();
        var selectedEntity = playerData.getSelectedEntityData();
        buildSelectedEntityProperties(selectedEntity, store, uiCommandBuilder, uiEventBuilder);
    }

    void buildWorldDetails(PlayerData playerData, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
        // world time
        var worldTime = getWorldTime(ref.getStore());
        var localTime = worldTime.getGameDateTime();
        var day = localTime.getDayOfYear();

        uiCommandBuilder.set("#WorldPropertiesList[2] #Value.Text",
                String.format("Day %d, %s", day, localTime.format(DateTimeFormatter.ofPattern("HH:mm")))
        );

        // redrawn time
        var timeNowString = Instant.now()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        uiCommandBuilder.set("#WorldPropertiesList[3] #Value.Text", timeNowString);
    }

    public void buildPlayersList() {
        var playerData = getPlayerData();
        var ref = this.playerRef.getReference();
        if (ref == null) return;

        var store = ref.getStore();
        var worldData = playerData.getWorldData();
        var uiCommandBuilder = new UICommandBuilder();
        var uiEventBuilder = new UIEventBuilder();

        buildPlayersList(worldData, uiCommandBuilder, uiEventBuilder, store);

        this.sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    public void buildTeleportersList() {
        var playerData = getPlayerData();
        var ref = this.playerRef.getReference();
        if (ref == null) return;

        var store = ref.getStore();
        var worldData = playerData.getWorldData();
        var uiCommandBuilder = new UICommandBuilder();
        var uiEventBuilder = new UIEventBuilder();

        buildTeleportersList(worldData, uiCommandBuilder, uiEventBuilder, store);

        this.sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    public void buildEntitiesList() {
        var playerData = getPlayerData();
        var ref = this.playerRef.getReference();
        if (ref == null) return;

        var store = ref.getStore();
        var worldData = playerData.getWorldData();
        var uiCommandBuilder = new UICommandBuilder();
        var uiEventBuilder = new UIEventBuilder();

        buildEntitiesList(playerData, worldData, uiCommandBuilder, uiEventBuilder, store);

        this.sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    public void buildSelectedEntity() {
        var playerData = getPlayerData();
        var ref = this.playerRef.getReference();
        if (ref == null) return;

        var uiCommandBuilder = new UICommandBuilder();
        var uiEventBuilder = new UIEventBuilder();

        var selectedEntity = playerData.getSelectedEntityData();
        buildSelectedEntity(selectedEntity, uiCommandBuilder, uiEventBuilder);

        this.sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    void buildSelectedEntity(EntityData selectedEntity, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
        uiCommandBuilder.set("#RightPanel.Visible", selectedEntity != null);
        if (selectedEntity == null) return;

        // draw inspector of entity
        var iconPath = selectedEntity.getIconPath();
        if (iconPath != null) {
            uiCommandBuilder.set("#EntityIcon.Background", iconPath);
        } else {
            uiCommandBuilder.set("#EntityIcon.Background", "");
        }

        uiCommandBuilder.set("#EntityName.Text", selectedEntity.DisplayName);
        uiCommandBuilder.set("#EntityUUID.Visible", selectedEntity.UniqueIdString != null);
        if (selectedEntity.UniqueIdString != null) {
            uiCommandBuilder.set("#EntityUUID.Text", selectedEntity.UniqueIdString);
        }
        uiCommandBuilder.set("#EntityId.Text", "entity #" + selectedEntity.Id);

        buildSelectedEntityComponentList(selectedEntity, uiCommandBuilder, uiEventBuilder);

        // tp button
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#EntityOptions #TpButton",
                EventData.of("Button", "Entity_GoTo")
                        .append("EntityId", String.valueOf(selectedEntity.Id))
        );

        // bring here button
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#EntityOptions #BringHereButton",
                EventData.of("Button", "Entity_BringHere")
                        .append("EntityId", String.valueOf(selectedEntity.Id))
        );
    }

    void buildSelectedEntityComponentList(EntityData selectedEntity, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
        uiCommandBuilder.clear("#EntityComponentsList");

        var components = selectedEntity.Components;
        uiCommandBuilder.set("#EntityComponentsCount #CountText.Text", String.valueOf(components.size()));

        uiCommandBuilder.clear("#EntityComponentsList");

        var index = 0;
        for (var component : components) {
            uiCommandBuilder.append("#EntityComponentsList", "Pages/EntityViewer/EntityComponentListItem.ui");
            uiCommandBuilder.set("#EntityComponentsList[" + index + "] #ComponentName.Text", component);

            index++;
        }
    }

    void buildSelectedEntityProperties(EntityData selectedEntity, @NonNullDecl Store<EntityStore> store, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
        if (selectedEntity == null) return;

        uiCommandBuilder.clear("#EntityPropertiesList");

        var count = 0;
        for (var property : selectedEntity.StaticProperties.keySet()) {
            var value = selectedEntity.StaticProperties.get(property);
            appendEntityPropertyItem("#EntityPropertiesList", count, property, value, uiCommandBuilder);
            count++;
        }

        for (var property : selectedEntity.Properties.keySet()) {
            var value = selectedEntity.Properties.get(property);
            appendEntityPropertyItem("#EntityPropertiesList", count, property, value, uiCommandBuilder);
            count++;
        }

        uiCommandBuilder.set("#EntityPropertiesCount #CountText.Text", String.valueOf(count));
    }

    public void addEntity(EntityData entityData, UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {
        EntityViewer.log("addEntity: " + entityData.Id);

        var worldData = getPlayerData().getSelectedWorldData();
        createEntityListItem(entityData, worldData, true, uiCommandBuilder, uiEventBuilder);
    }

    public void removeEntity(EntityData entityData, UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {
        EntityViewer.log("removeEntity: " + entityData.Id);

        uiCommandBuilder.remove(entityData.ElementId);

        var playerData = getPlayerData();
        if (playerData.SelectedEntityId == entityData.Id) {
            playerData.SelectedEntityId = -1;
            buildSelectedEntity(null, uiCommandBuilder, uiEventBuilder);
        }
    }

    // helpers

    void appendEntityPropertyItem(String root, @NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append(root, "Pages/EntityViewer/EntityPropertyItem.ui");
    }

    void appendEntityPropertyItem(String root, int index, String key, String value, @NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append(root, "Pages/EntityViewer/EntityPropertyItem.ui");
        setEntityPropertyItem(root + "[" + index + "]", key, value, uiCommandBuilder);
    }

    void setEntityPropertyItem(String selector, String key, String value, @NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.set(selector + " #Key.Text", key);
        uiCommandBuilder.set(selector + " #Value.Text", value);
    }

    void createEntityListItem(EntityData entityData, WorldData worldData, boolean canInsert, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
        var elementId = entityData.ElementId;

        if (canInsert) {
            if (worldData.Entities.size() == 1) {
                uiCommandBuilder.appendInline("#EntitiesList", "Group " + elementId + " {}");
            }

            // find the first entity prior
            var firstEntityId = -1;
            var priorEntityId = -1;
            for (var id : worldData.Entities.keySet()) {
                if (firstEntityId == -1) {
                    firstEntityId = id;
                }

                if (id < entityData.Id && id > priorEntityId) {
                    priorEntityId = id;
                }
            }

            if (priorEntityId != -1) {
                uiCommandBuilder.appendInline("#EntitiesList", "Group " + elementId + " {}");
            } else {
                if (firstEntityId != -1) {
                    uiCommandBuilder.insertBeforeInline("#Entity" + firstEntityId, "Group " + elementId + " {}");
                }
            }
        } else {
            uiCommandBuilder.appendInline("#EntitiesList", "Group " + elementId + " {}");
        }

        uiCommandBuilder.append(elementId, "Pages/EntityViewer/EntityListItem.ui");

        uiCommandBuilder.set(elementId + " #Button.Text", entityData.DisplayName);
        uiCommandBuilder.set(elementId + " #Id.Text", "#" + entityData.Id);

        // icon
        var iconPath = entityData.getIconPath();
        if (iconPath != null) {
            uiCommandBuilder.set(elementId + " #Icon.Background", iconPath);
            uiCommandBuilder.set(elementId + " #Icon.Visible", true);
        }

        // click event
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                entityData.ElementId + " #Button",
                EventData.of("Button", "Entity_Select")
                        .append("EntityId", String.valueOf(entityData.Id))
        );
    }

    // events


    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl Data data) {
        var commandBuilder = new UICommandBuilder();
        var eventBuilder = new UIEventBuilder();

        // filter
        if (data.filter != null) {
//            EntityViewer.log("Filtering " + data.filter);

            var playerData = getPlayerData();
            playerData.Filter = data.filter;
            playerData.Book.filter(playerData.Filter, playerData.getSelectedWorldData());

            var worldData = playerData.getSelectedWorldData();
            buildEntitiesList(playerData, worldData, commandBuilder, eventBuilder, store);
        }

        // button
        if (data.button != null) {
//            EntityViewer.log("data.button: " + data.button);
//            EntityViewer.log("data.entityId: " + data.entityId);

            switch (data.button) {
                // reload the whole lookup for the world
                case "Reload": {
                    var playerData = getPlayerData();
                    PageSignals.rebuildEntityLookup(playerData.getSelectedWorld());
                    break;
                }

                // selects an entity to display in the side panel
                case "Entity_Select": {
                    var playerData = getPlayerData();
                    var worldData = playerData.getSelectedWorldData();
                    var entityId = Integer.parseInt(data.entityId);
                    var selectedEntity = worldData.Entities.get(entityId);

                    if (selectedEntity != null && playerData.SelectedEntityId != selectedEntity.Id) {
                        playerData.SelectedEntityId = selectedEntity.Id;
                        buildSelectedEntity(selectedEntity, commandBuilder, eventBuilder);
                    }

                    break;
                }

                // tp to the entity
                case "Player_GoTo":
                case "Entity_GoTo": {
                    var playerData = getPlayerData();
                    var worldData = playerData.getSelectedWorldData();
                    var entityId = Integer.parseInt(data.entityId);
                    var selectedEntity = worldData.Entities.get(entityId);

                    try {
                        var entityRef = selectedEntity.getRef(worldData.getWorld());
                        var entityTransform = store.getComponent(entityRef, TransformComponent.getComponentType());

                        // move player to entity
                        assert playerRef.getWorldUuid() != null;
                        var world = Universe.get().getWorld(playerRef.getWorldUuid());

                        assert world != null;

                        var playerRef = this.playerRef;
                        world.execute(() -> {
                            var teleport = Teleport.createForPlayer(world,
                                    entityTransform.getPosition(),
                                    new Vector3f(0, 0, 0)
                            );
                            store.addComponent(playerRef.getReference(), Teleport.getComponentType(), teleport);

                            close();
                        });
                    } catch (Exception e) {
                        EntityViewer.err("error when using EntityTpTo, error: " + e);
                    }

                    break;
                }

                // tp the entity to me
                case "Entity_BringHere": {
                    var playerData = getPlayerData();
                    var worldData = playerData.getSelectedWorldData();
                    var entityId = Integer.parseInt(data.entityId);
                    var selectedEntity = worldData.Entities.get(entityId);

                    try {
                        var world = worldData.getWorld();
                        var entityRef = selectedEntity.getRef(world);
                        var playerTransform = store.getComponent(ref, TransformComponent.getComponentType());

                        // move entity to player
                        assert world != null;

                        var entityStore = world.getEntityStore().getStore();
                        world.execute(() -> {
                            var teleport = Teleport.createForPlayer(world,
                                    playerTransform.getPosition(),
                                    new Vector3f(0, 0, 0)
                            );
                            entityStore.addComponent(entityRef, Teleport.getComponentType(), teleport);

                            close();
                        });
                    } catch (Exception e) {
                        EntityViewer.err("error when using EntityTpTo, error: " + e);
                    }

                    break;
                }

                // changes the world shown in the page for the player
                case "World_Switch": {
                    var worldName = data.dropdown;
                    try {
                        EntityViewer.log("Switching world " + worldName);

                        var playerData = getPlayerData();
                        playerData.SelectedWorldName = worldName;

                        commandBuilder.set("#SearchInput.Value", "");

                        playerData.rebuildPage();
                    } catch (Exception e) {
                        EntityViewer.err("error when using dropdown value, error: " + e);
                    }
                    break;
                }

                // tp the player to this world
                case "World_GoTo": {
                    try {
                        var playerData = getPlayerData();
                        var world = playerData.getSelectedWorld();

                        var player = playerData.getPlayer();
                        assert player.getWorld() != null;

                        EntityViewer.log("Requesting world tp from " + player.getWorld().getName() + " to " + world.getName());
                        if (player.getWorld() != world) {
                            // teleport to world
                            player.getWorld().execute(() -> {
                                CommandManager.get().handleCommand(player, "tp world " + world.getName());
                            });

                            close();
                        }
                    } catch (Exception e) {
                        EntityViewer.err("Error while executing TpToWorld");
                    }
                    break;
                }

                // tp the player to this teleporter
                case "Teleporter_Warp": {
                    var warps = TeleportPlugin.get().getWarps().values();
                    var wantedWarp = data.warp;

                    try {
                        for (var warp : warps) {
                            if (warp.getId().equals(wantedWarp)) {
                                var teleport = warp.toTeleport();

                                // move player to warp
                                assert playerRef.getWorldUuid() != null;
                                var world = Universe.get().getWorld(playerRef.getWorldUuid());

                                assert world != null;

                                var playerRef = this.playerRef;
                                world.execute(() -> {
                                    var forward = Transform.getDirection(
                                            teleport.getRotation().getPitch(),
                                            teleport.getRotation().getYaw()
                                    );
                                    teleport.setPosition(
                                            teleport.getPosition().add(forward.scale(2))
                                    );
                                    store.addComponent(playerRef.getReference(), Teleport.getComponentType(), teleport);

                                    close();
                                });
                            }
                        }
                    } catch (Exception e) {
                        EntityViewer.err("error when using EntityTpTo, error: " + e);
                    }

                    break;
                }
            }
        }

        // fallback
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec
                .builder(Data.class, Data::new)
                // button
                .append(
                        new KeyedCodec<>("Button", Codec.STRING),
                        (data, s) -> data.button = s,
                        data -> data.button
                )
                .add()
                // entity id
                .append(
                        new KeyedCodec<>("EntityId", Codec.STRING),
                        (data, s) -> data.entityId = s,
                        data -> data.entityId
                )
                .add()
                // dropdown
                .append(
                        new KeyedCodec<>("@DropdownValue", Codec.STRING),
                        (data, s) -> data.dropdown = s,
                        data -> data.dropdown
                )
                .add()
                // filter
                .append(
                        new KeyedCodec<>("@SearchInput", Codec.STRING),
                        (data, s) -> {
                            data.filter = s;
                            if (data.filter == null) {
                                data.filter = "";
                            }
                        },
                        data -> data.filter
                )
                .add()
                // warp
                .append(
                        new KeyedCodec<>("Warp", Codec.STRING),
                        (data, s) -> {
                            data.warp = s;
                        },
                        data -> data.warp
                )
                .add()
                .build();

        private String button;
        private String entityId;
        private String dropdown;
        private String filter;
        private String warp;
    }
}
