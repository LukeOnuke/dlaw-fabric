package com.lukeonuke.dlawfabric.module.minecraft.command;

import com.lukeonuke.dlawfabric.service.MuteService;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.UUID;

public class MuteCommand extends BoltsCommand{
    @Override
    public String getCommandName() {
        return "mute";
    }

    @Override
    public List<RequiredArgumentBuilder<CommandSourceStack, ?>> getArguments() {
        return List.of(
                Commands.argument("target", EntityArgument.player())
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        final ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        final CommandSourceStack source = commandContext.getSource();

        if (!source.isPlayer() || source.getPlayer() == null){
            source.sendFailure(PluginUtils.formatFullErrorMessage("Command can only be run by a player!"));
            return SINGLE_SUCCESS;
        }

        if(target == null){
            source.sendFailure(PluginUtils.formatFullErrorMessage("Can't find player."));
            return SINGLE_SUCCESS;
        }

        final MuteService ms = MuteService.getInstance();
        UUID targetUUID = target.getUUID();
        if(ms.isGloballyMuted(targetUUID)){
            ms.globallyUnmute(target.getUUID());
        }else {
            ms.globallyMute(target.getUUID());
        }

        source.sendSystemMessage(PluginUtils.prependPrefix(Component.literal("Player ").append(Component.empty().withStyle(ChatFormatting.LIGHT_PURPLE).append(target.getDisplayName())).append(" is now " + (ms.isGloballyMuted(targetUUID) ? "muted." : "unmuted."))));
        return SINGLE_SUCCESS;
    }
}