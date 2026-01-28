package com.nomnom.entityviewer.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Notification;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.entityviewer.EntityViewer;
import com.nomnom.entityviewer.ui.EntityViewerUi;
import com.nomnom.entityviewer.ui.TestPage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class ShowTestUiCommand extends AbstractPlayerCommand {
    public ShowTestUiCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        var player = commandContext.senderAs(Player.class);

        CompletableFuture.runAsync(() -> {
            var page = new TestPage(playerRef, CustomPageLifetime.CanDismiss);
            player.getPageManager().openCustomPage(ref, store, page);
        });
    }

    public static boolean reopenWindow(PlayerRef playerRef, Packet packet) {
        if (!(packet instanceof Notification notification)) {
            return false;
        }

        // bad message content
        if (notification.secondaryMessage == null) return false;
        var rawText = notification.secondaryMessage.rawText;
        if (rawText == null) return false;

        // not from me
        if (!notification.secondaryMessage.rawText.startsWith("com.nomnom:Entity Viewer:UI/Custom/Pages")) {
            return false;
        }

        // get store
        var ref = playerRef.getReference();
        var store = ref != null ? ref.getStore() : null;
        if (store == null) return false;

        // manage page
        var player = store.getComponent(ref, Player.getComponentType());
        var pageManager = player != null ? player.getPageManager() : null;
        if (pageManager == null)  return false;

        var page = pageManager.getCustomPage();
        if  (page == null) return false;

        var pageClass = page.getClass();
        EntityViewer.log("package: " + pageClass.getPackageName());

        if (pageClass.getPackageName().startsWith("com.nomnom.entityviewer")) {
            pageManager.openCustomPage(ref, store, page);
        }

        return false;
    }
}
