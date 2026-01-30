package com.nomnom.entityviewer.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.nomnom.entityviewer.EntityViewer;
import com.nomnom.entityviewer.ui.EntityViewerPage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class ShowEntityViewerCommand extends AbstractPlayerCommand {
    public ShowEntityViewerCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        var player = commandContext.senderAs(Player.class);

        CompletableFuture.runAsync(() -> {
            openPage(playerRef, player, ref, store);
        });
    }

    public static void openPage(PlayerRef playerRef, Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        var page = new EntityViewerPage(playerRef, CustomPageLifetime.CanDismiss);
        player.getPageManager().openCustomPage(ref, store, page);

        var playerData = EntityViewer.getPlayerData(playerRef);
        playerData.getSelectedWorldData().RebuildEntityLookup = true;
    }
}
