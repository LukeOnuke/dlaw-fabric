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

public class IgnoreCommand extends BoltsCommand{
    @Override
    public String getCommandName() {
        return "ignore";
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
        final ServerPlayerEntity sourcePlayer = source.getPlayer();

        if (!source.isExecutedByPlayer() || sourcePlayer == null){
            source.sendError(PluginUtils.formatFullErrorMessage("Command can only be run by a player!"));
            return SINGLE_SUCCESS;
        }

        if(target == null){
            source.sendError(PluginUtils.formatFullErrorMessage("Can't find player!"));
            return SINGLE_SUCCESS;
        }

        if(target.equals(sourcePlayer)){
            source.sendError(PluginUtils.formatFullErrorMessage("You can't ignore yourself!"));
            return SINGLE_SUCCESS;
        }

        final MuteService ms = MuteService.getInstance();
        UUID targetUUID = target.getUuid();
        UUID sourceUUID = sourcePlayer.getUuid();
        if(ms.isPersonallyMuted(targetUUID, sourceUUID)){
            ms.personallyUnmute(targetUUID, sourceUUID);
        }else {
            ms.personallyMute(targetUUID, sourceUUID);
        }

        source.sendMessage(PluginUtils.prependPrefix(Text.literal("Player ").append(Text.empty().formatted(Formatting.LIGHT_PURPLE).append(target.getDisplayName())).append(" is now " + (ms.isPersonallyMuted(targetUUID, sourceUUID) ? "ignored." : "no longer ignored."))));
        return SINGLE_SUCCESS;
    }
}
