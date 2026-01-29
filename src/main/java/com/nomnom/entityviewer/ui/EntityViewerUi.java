//package com.nomnom.entityviewer.ui;
//
//import com.hypixel.hytale.codec.Codec;
//import com.hypixel.hytale.codec.KeyedCodec;
//import com.hypixel.hytale.codec.builder.BuilderCodec;
//import com.hypixel.hytale.component.Ref;
//import com.hypixel.hytale.component.Store;
//import com.hypixel.hytale.math.vector.Vector3f;
//import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
//import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
//import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
//import com.hypixel.hytale.server.core.command.system.CommandManager;
//import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
//import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
//import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
//import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
//import com.hypixel.hytale.server.core.ui.LocalizableString;
//import com.hypixel.hytale.server.core.ui.builder.EventData;
//import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
//import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
//import com.hypixel.hytale.server.core.universe.PlayerRef;
//import com.hypixel.hytale.server.core.universe.Universe;
//import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
//import com.nomnom.entityviewer.EntityViewer;
//import com.nomnom.entityviewer.PlayerData;
//import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
//
//import java.util.ArrayList;
//import java.util.UUID;
//
//public class EntityViewerUi extends InteractiveCustomUIPage<EntityViewerUi.Data> {
////    private static final Value<String> TAB_STYLE_ACTIVE = Value.ref("Common.ui", "DefaultTextButtonStyle");
////    private static final Value<String> TAB_STYLE_INACTIVE = Value.ref("Common.ui", "SecondaryTextButtonStyle");
//
//    private boolean _usingDropdown;
//    private boolean _waitingToRedraw;
//
//    public EntityViewerUi(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime) {
//        super(playerRef, lifetime, Data.CODEC);
//
//        EntityViewer.getInstance().registerPlayer(playerRef);
//    }
//
//    private PlayerData getPlayerData() {
//        return EntityViewer.getInstance().Players.get(playerRef.getUuid());
//    }
//
//    @Override
//    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
//        var playerData = getPlayerData();
//
//        buildWindow(playerData, uiCommandBuilder, uiEventBuilder);
//        buildBook(playerData, uiCommandBuilder,  uiEventBuilder);
//        buildPage(playerData, uiCommandBuilder,  uiEventBuilder);
//    }
//
//    void buildWindow(@NonNullDecl PlayerData playerData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
//        uiCommandBuilder.append("Pages/MainPanel.ui");
//
//        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", EventData.of(Data.KEY_BUTTON, "BackButton"), false);
//        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReloadButton", EventData.of(Data.KEY_BUTTON, "ReloadButton"), false);
//
//        uiCommandBuilder.set("#SearchInput.Value", playerData.Filter);
//        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of(Data.KEY_FILTER, "#SearchInput.Value"), false);
//    }
//
//    void buildWorldContent(@NonNullDecl PlayerData playerData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
//        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#WorldContent #TpWorldButton", EventData.of(Data.KEY_BUTTON, "TpToWorld"), false);
//    }
//
//    void buildTabs(@NonNullDecl PlayerData playerData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
//        // world dropdown
//        var options = new ArrayList<DropdownEntryInfo>();
//        for (var world : EntityViewer.getInstance().Worlds.keySet()) {
//            options.add(new DropdownEntryInfo(LocalizableString.fromString(world), world));
//        }
//
//        uiCommandBuilder.set("#WorldDropdown.Entries", options);
//        uiCommandBuilder.set("#WorldDropdown.Value", playerData.SelectedWorldName);
//        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#WorldDropdown", EventData.of(Data.KEY_BUTTON, "SwitchWorld")
//                .append(Data.KEY_DROPDOWN, "#WorldDropdown.Value"));
////        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#WorldDropdown", EventData.of(Data.KEY_BUTTON, "SwitchWorldGained"));
////        uiEventBuilder.addEventBinding(CustomUIEventBindingType.FocusLost, "#WorldDropdown", EventData.of(Data.KEY_BUTTON, "SwitchWorldLost"));
//    }
//
//    void buildPagination(@NonNullDecl PlayerData playerData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
//        uiCommandBuilder.clear("#PaginationButtons");
//        if (playerData.Book.Entities.isEmpty()) {
//            return;
//        }
//
//        var pageCount = (int)Math.ceil(playerData.Book.Entities.size() / (float)EntityViewer.MAX_ENTRIES_PER_PAGE);
//        for (int i = 0; i < pageCount; i++) {
//            uiCommandBuilder.append("#PaginationButtons", "Pages/EntityViewerPaginateButton.ui");
//            uiCommandBuilder.set("#PaginationButtons[" + i + "] #PageButton.Text", String.valueOf(i + 1));
//
//            uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PaginationButtons[" + i + "] #PageButton", EventData.of(Data.KEY_BUTTON, "PageButton" + i), false);
//        }
//    }
//
//    public void buildContent() {
//        var playerData = getPlayerData();
//        var commandBuilder = new UICommandBuilder();
//        var eventBuilder =  new UIEventBuilder();
//
//        buildWorldContent(playerData, commandBuilder, eventBuilder);
//        buildBook(playerData, commandBuilder, eventBuilder);
//        buildPage(playerData, commandBuilder, eventBuilder);
//
//        this.sendUpdate(commandBuilder, eventBuilder, false);
//    }
//
//    void buildBook(@NonNullDecl PlayerData playerData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
//        var worldData = playerData.getSelectedWorldData();
//
//        // get current entities
//        playerData.Book.filter(playerData.Filter, worldData);
//
//        // now bind those entities to the gui
//        uiCommandBuilder.set("#EntityCount.Text", playerData.Book.Entities.size() + " entities");
//
//        buildTabs(playerData, uiCommandBuilder, uiEventBuilder);
//        buildPagination(playerData, uiCommandBuilder, uiEventBuilder);
//    }
//
//    void buildPage(@NonNullDecl PlayerData playerData, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder) {
//        uiCommandBuilder.clear("#EntityList");
//
//        // build the list
//        // need to construct each entity from the archetypes
//        var book = playerData.Book;
//        if (book == null) return;
//
//        var ctx = new Object() {
//            int listIndex = 0;
//        };
//
//        book.iterPage(playerData.PageIndex, EntityViewer.MAX_ENTRIES_PER_PAGE, entityData -> {
//            uiCommandBuilder.append("#EntityList", "Pages/Entity/Entity.ui");
//            uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #EntityId.Text", "#" + entityData.Id);
//
//            // show the display name if possible
//            var usedModelAssetId = false;
//            if (entityData.DisplayName == null) {
//                if (entityData.ModelAssetId != null) {
//                    entityData.DisplayName = entityData.ModelAssetId;
//                    usedModelAssetId = true;
//                }
//            }
//
//            if (entityData.DisplayName != null) {
//                uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #DisplayName.Text", entityData.DisplayName);
//            }
//
//            uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #UniqueId.Text", entityData.UniqueId.toString());
//
//            // properties
//            if (entityData.Properties != null && !entityData.Properties.isEmpty()) {
//                uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #DisplayProperties.Visible", true);
//
//                var p = 0;
//                for (var property : entityData.Properties.keySet()) {
//                    if (usedModelAssetId && property.equals("model_asset_id")) {
//                        continue;
//                    }
//
//                    uiCommandBuilder.append("#EntityList[" + ctx.listIndex + "] #DisplayProperties", "Pages/Entity/Property.ui");
//                    uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #DisplayProperties[" + p + "] #PropertyKey.Text", property + ":");
//
//                    var value = entityData.Properties.get(property);
//                    uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #DisplayProperties[" + p + "] #PropertyValue.Text", value);
//                    p++;
//                }
//            }
//
//            // memory icon
//            if (entityData.ModelAssetId != null && !entityData.ModelAssetId.isEmpty()) {
//                var model = EntityViewer.MODELS.get(entityData.ModelAssetId);
//                var iconPath = "Pages/Memories/npcs/" + model.getId() + ".png";
//                var fullIconPath = "UI/Custom/" + iconPath;
//                if (CommonAssetRegistry.hasCommonAsset(fullIconPath)) {
//                    uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #EntityImage.Background", iconPath);
//                    uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #EntityImage.Visible", true);
//                }
//            }
//
//            // entity options
//            // does it have a transform though?
//            if (entityData.Components.contains("TransformComponent")) {
//                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EntityList[" + ctx.listIndex + "] #EntityTpTo", EventData.of(Data.KEY_BUTTON, "EntityTpTo")
//                        .append(Data.KEY_UUID, entityData.UniqueId.toString())
//                        , false);
//                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EntityList[" + ctx.listIndex + "] #EntityBringHere", EventData.of(Data.KEY_BUTTON, "EntityBringHere")
//                        .append(Data.KEY_UUID, entityData.UniqueId.toString())
//                        , false);
//            } else {
//                uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #EntityOptions.Visible", false);
//            }
//
//            ctx.listIndex++;
//
//            // show the components as a csv list
//            if (entityData.Components != null) {
//                uiCommandBuilder.append("#EntityList", "Pages/Entity/ComponentList.ui");
//
//                var componentString = String.join(", ", entityData.Components);
//                uiCommandBuilder.set("#EntityList[" + ctx.listIndex + "] #ComponentList.Text", componentString);
//                ctx.listIndex++;
//            }
//        });
//    }
//
//    @Override
//    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl Data data) {
//        super.handleDataEvent(ref, store, data);
//
//        EntityViewer.log("data event. button: " + data.button + ", filter: " + data.filter);
//
//        if (data.filter != null) {
//            var playerData = getPlayerData();
//            playerData.Filter = data.filter;
//
//            playerData.getSelectedWorldData().DrawPage = true;
//        }
//
//        if (data.button != null) {
//            if (data.button.equals("ReloadButton")) {
//                EntityViewer.log("reloading entities");
//
//                getPlayerData().getSelectedWorldData().WantsFullRebuild = true;
//            }
//
//            if (data.button.equals("BackButton")) {
//                close();
//            }
//
//            if (data.button.equals("TpToWorld")) {
//                try {
//                    var playerData = getPlayerData();
//                    var world = playerData.getSelectedWorld();
//
//                    var player = playerData.getPlayer();
//                    assert player.getWorld() != null;
//                    EntityViewer.log("Requesting world tp from " + player.getWorld().getName() + " to " + world.getName());
//                    if (player.getWorld() != world) {
//                        // teleport to world
//                        player.getWorld().execute(() -> {
//                            CommandManager.get().handleCommand(player, "tp world " + world.getName());
//                        });
//
//                        close();
//                    }
//                } catch (Exception e) {
//                    EntityViewer.err("Error while executing TpToWorld");
//                }
//            }
//
//            if (data.button.startsWith("PageButton")) {
//                try {
//                    var playerData = getPlayerData();
//                    playerData.PageIndex = Integer.parseInt(data.button.substring("PageButton".length()));
//
//                    var commandBuilder = new UICommandBuilder();
//                    var eventBuilder = new UIEventBuilder();
//
//                    buildPage(playerData, commandBuilder,  eventBuilder);
//
//                    this.sendUpdate(commandBuilder, eventBuilder, false);
//                    return;
//                } catch (NumberFormatException _) {
//                    EntityViewer.err("error parsing page index");
//                }
//            }
//
//            if (data.button.equals("EntityTpTo")) {
//                var commandBuilder = new UICommandBuilder();
//                var eventBuilder = new UIEventBuilder();
//
//                try {
//                    var uuid = UUID.fromString(data.uuid);
//                    var entityRef = store.getExternalData().getRefFromUUID(uuid);
//                    var entityTransform = store.getComponent(entityRef, TransformComponent.getComponentType());
//
//                    // move player to entity
//                    assert playerRef.getWorldUuid() != null;
//                    var world = Universe.get().getWorld(playerRef.getWorldUuid());
//
//                    assert world != null;
//                    world.execute(() -> {
//                        var teleport = Teleport.createForPlayer(world,
//                                entityTransform.getPosition(),
//                                new Vector3f(0, 0, 0)
//                        );
//                        store.addComponent(playerRef.getReference(), Teleport.getComponentType(), teleport);
//                    });
//                } catch (Exception e) {
//                    EntityViewer.err("error when using EntityTpTo, error: " + e);
//                }
//
//                this.sendUpdate(commandBuilder, eventBuilder, false);
//
//                return;
//            }
//
//            if (data.button.equals("EntityBringHere")) {
//                var commandBuilder = new UICommandBuilder();
//                var eventBuilder = new UIEventBuilder();
//
//                try {
//                    var uuid = UUID.fromString(data.uuid);
//                    var entityRef = store.getExternalData().getRefFromUUID(uuid);
//
//                    // move entity to player
//                    var world = entityRef.getStore().getExternalData().getWorld();
//
//                    world.execute(() -> {
//                        var playerTransform = store.getComponent(playerRef.getReference(), TransformComponent.getComponentType());
//                        var teleport = Teleport.createForPlayer(world,
//                                playerTransform.getPosition(),
//                                new Vector3f(0, 0, 0)
//                        );
//                        store.addComponent(entityRef, Teleport.getComponentType(), teleport);
//                    });
//                } catch (Exception e) {
//                    EntityViewer.err("error when using EntityBringHere, error: " + e);
//                }
//
//                this.sendUpdate(commandBuilder, eventBuilder, false);
//
//                return;
//            }
//
//            if (data.button.equals("SwitchWorld")) {
//                var worldName = data.dropdownValue;
//                try {
//                    EntityViewer.log("Switching world " + worldName);
//
//                    var playerData = getPlayerData();
//                    playerData.SelectedWorldName = worldName;
//                    playerData.getSelectedWorldData().DrawPage = true;
//                } catch (Exception e) {
//                    EntityViewer.err("error when using dropdown value, error: " + e);
//                }
//            }
//        }
//
//        var commandBuilder = new UICommandBuilder();
//        var eventBuilder = new UIEventBuilder();
//        this.sendUpdate(commandBuilder, eventBuilder, false);
//    }
//
//    public static class Data {
//        static final String KEY_BUTTON = "Button";
//        static final String KEY_FILTER = "@SearchInput";
//        static final String KEY_DROPDOWN = "@DropdownValue";
//        static final String KEY_INDEX = "Index";
//        static final String KEY_UUID = "UUID";
//        static final String KEY_STRING = "String";
//
//        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
//                .append(
//                        new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
//                        (data, s) -> data.button = s,
//                        data -> data.button
//                )
//                .add()
//                .append(
//                        new KeyedCodec<>(KEY_FILTER, Codec.STRING),
//                        (data, s) -> {
//                            data.filter = s;
//                            if (data.filter == null) {
//                                data.filter = "";
//                            }
//                        },
//                        data -> data.filter
//                )
//                .add()
//                .append(
//                        new KeyedCodec<>(KEY_DROPDOWN, Codec.STRING),
//                        (searchGuiData, s) -> searchGuiData.dropdownValue = s,
//                        searchGuiData -> searchGuiData.dropdownValue
//                )
//                .add()
//                .append(
//                        new KeyedCodec<>(KEY_INDEX, Codec.STRING),
//                        (searchGuiData, s) -> searchGuiData.index = s,
//                        searchGuiData -> searchGuiData.index
//                )
//                .add()
//                .append(
//                        new KeyedCodec<>(KEY_UUID, Codec.STRING),
//                        (searchGuiData, s) -> searchGuiData.uuid = s,
//                        searchGuiData -> searchGuiData.uuid
//                )
//                .add()
//                .append(
//                        new KeyedCodec<>(KEY_STRING, Codec.STRING),
//                        (searchGuiData, s) -> searchGuiData.string = s,
//                        searchGuiData -> searchGuiData.string
//                )
//                .add()
//                .build();
//
//        private String button;
//        private String filter;
//        private String dropdownValue;
//        private String index;
//        private String uuid;
//        private String string;
//    }
//}
