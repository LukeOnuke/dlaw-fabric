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

public class IgnoreCommand extends BoltsCommand{
    @Override
    public String getCommandName() {
        return "ignore";
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
        final ServerPlayer sourcePlayer = source.getPlayer();

        if (!source.isPlayer() || sourcePlayer == null){
            source.sendFailure(PluginUtils.formatFullErrorMessage("Command can only be run by a player!"));
            return SINGLE_SUCCESS;
        }

        if(target == null){
            source.sendFailure(PluginUtils.formatFullErrorMessage("Can't find player!"));
            return SINGLE_SUCCESS;
        }

        if(target.equals(sourcePlayer)){
            source.sendFailure(PluginUtils.formatFullErrorMessage("You can't ignore yourself!"));
            return SINGLE_SUCCESS;
        }

        final MuteService ms = MuteService.getInstance();
        UUID targetUUID = target.getUUID();
        UUID sourceUUID = sourcePlayer.getUUID();
        if(ms.isPersonallyMuted(targetUUID, sourceUUID)){
            ms.personallyUnmute(targetUUID, sourceUUID);
        }else {
            ms.personallyMute(targetUUID, sourceUUID);
        }

        source.sendSystemMessage(PluginUtils.prependPrefix(Component.literal("Player ").append(Component.empty().withStyle(ChatFormatting.LIGHT_PURPLE).append(target.getDisplayName())).append(" is now " + (ms.isPersonallyMuted(targetUUID, sourceUUID) ? "ignored." : "no longer ignored."))));
        return SINGLE_SUCCESS;
    }
}