package com.nomnom.entityviewer.items;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.nomnom.entityviewer.EntityViewer;
import com.nomnom.entityviewer.commands.ShowEntityViewerCommand;

import javax.annotation.Nonnull;

public class DebugStickSneakInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<DebugStickSneakInteraction> CODEC = BuilderCodec.builder(
            DebugStickSneakInteraction.class, DebugStickSneakInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        var ctx = interactionContext.getState();

        var commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            ctx.state = InteractionState.Failed;
            return;
        }

        var ref = interactionContext.getEntity();
        var playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            ctx.state = InteractionState.Failed;
            return;
        }

        var player = commandBuffer.getComponent(ref, Player.getComponentType());
        ShowEntityViewerCommand.openPage(playerRef, player, ref, ref.getStore());

        ctx.state = InteractionState.Finished;
    }
}