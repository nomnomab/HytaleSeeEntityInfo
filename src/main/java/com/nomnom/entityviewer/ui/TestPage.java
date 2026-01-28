package com.nomnom.entityviewer.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.entityviewer.EntityViewer;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class TestPage extends InteractiveCustomUIPage<TestPage.Data> {
    public TestPage(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime) {
        super(playerRef, lifetime, TestPage.Data.CODEC);

        EntityViewer.getInstance().registerPlayer(playerRef);
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/EntityViewer/MainPanel.ui");
    }

    public static class Data {
        public static final BuilderCodec<TestPage.Data> CODEC = BuilderCodec.builder(TestPage.Data.class, TestPage.Data::new)
                .build();
    }
}
