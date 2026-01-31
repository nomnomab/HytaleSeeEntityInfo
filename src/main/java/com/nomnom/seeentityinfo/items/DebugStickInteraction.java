package com.nomnom.seeentityinfo.items;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.nomnom.seeentityinfo.SeeEntityInfo;
import com.nomnom.seeentityinfo.commands.ShowEntityViewerCommand;

import javax.annotation.Nonnull;

public class DebugStickInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<DebugStickInteraction> CODEC = BuilderCodec.builder(
            DebugStickInteraction.class, DebugStickInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        var ctx = interactionContext.getState();
        var target = interactionContext.getTargetEntity();

        if (target == null) {
            ctx.state = InteractionState.Failed;
            return;
        }

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

        // set the entity and world
        var playerData = SeeEntityInfo.getPlayerData(playerRef.getUuid());
        var targetStore = target.getStore();
        var world = targetStore.getExternalData().getWorld();
        var targetUUIDComponent = targetStore.getComponent(target, UUIDComponent.getComponentType());
        assert targetUUIDComponent != null;

        playerData.SelectedEntity = targetUUIDComponent.getUuid();
        playerData.SelectedWorldName = world.getName();

        if (playerData.Page == null) {
            // open the page
            var player = commandBuffer.getComponent(ref, Player.getComponentType());
            if (player == null) {
                ctx.state = InteractionState.Failed;
                return;
            }

            ShowEntityViewerCommand.openPage(playerRef, player, ref, ref.getStore());
            ctx.state = InteractionState.Finished;
            return;
        }

        ctx.state = InteractionState.Failed;
    }
}