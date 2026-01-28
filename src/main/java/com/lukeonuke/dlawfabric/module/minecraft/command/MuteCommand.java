package com.lukeonuke.dlawfabric.module.minecraft.command;

import com.lukeonuke.dlawfabric.service.MuteService;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

public class MuteCommand extends BoltsCommand{
    @Override
    public String getCommandName() {
        return "mute";
    }

    @Override
    public List<RequiredArgumentBuilder<ServerCommandSource, ?>> getArguments() {
        return List.of(
                CommandManager.argument("target", EntityArgumentType.player())
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
        final ServerPlayerEntity target = EntityArgumentType.getPlayer(commandContext, "target");
        final ServerCommandSource source = commandContext.getSource();

        if (!source.isExecutedByPlayer() || source.getPlayer() == null){
            source.sendError(PluginUtils.formatFullErrorMessage("Command can only be run by a player!"));
            return SINGLE_SUCCESS;
        }

        if(target == null){
            source.sendError(PluginUtils.formatFullErrorMessage("Can't find player."));
            return SINGLE_SUCCESS;
        }

        final MuteService ms = MuteService.getInstance();
        UUID targetUUID = target.getUuid();
        if(ms.isGloballyMuted(targetUUID)){
            ms.globallyUnmute(target.getUuid());
        }else {
            ms.globallyMute(target.getUuid());
        }

        source.sendMessage(PluginUtils.prependPrefix(Text.literal("Player ").append(Text.empty().formatted(Formatting.LIGHT_PURPLE).append(target.getDisplayName())).append(" is now " + (ms.isGloballyMuted(targetUUID) ? "muted." : "unmuted."))));
        return SINGLE_SUCCESS;
    }
}
