package com.lukeonuke.dlawfabric.module.minecraft.command;

import com.lukeonuke.dlawfabric.service.MuteService;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.UUID;

public class PmCommand extends BoltsCommand {
    private final MutableComponent prefix = Component.literal("you ").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE).append(Component.empty().withStyle(ChatFormatting.RESET));

    @Override
    public String getCommandName() {
        return "pm";
    }

    @Override
    public List<RequiredArgumentBuilder<CommandSourceStack, ?>> getArguments() {
        return List.of(Commands.argument("target", EntityArgument.player()),
                Commands.argument("message", StringArgumentType.greedyString()));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
        CommandSourceStack source = commandContext.getSource();

        if (!source.isPlayer() || source.getPlayer() == null){
            source.sendFailure(PluginUtils.formatFullErrorMessage("Command can only be run by a player!"));
            return SINGLE_SUCCESS;
        }

        if(target == null){
            source.sendFailure(PluginUtils.formatFullErrorMessage("Can't find player."));
            return SINGLE_SUCCESS;
        }

        final MuteService ms = MuteService.getInstance();
        final UUID sender = source.getPlayer().getUUID();

        String message = StringArgumentType.getString(commandContext, "message");
        source.sendSystemMessage(Component.empty().append(prefix.copy()).append("-> ").withStyle(ChatFormatting.GREEN).append(Component.empty().withStyle(ChatFormatting.LIGHT_PURPLE).append(target.getDisplayName())).append(Component.literal(" " + message).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)));
        if(ms.isGloballyMuted(sender)) return SINGLE_SUCCESS;
        if(!ms.isPersonallyMuted(sender, target.getUUID())){
            target.sendSystemMessage(Component.empty().append(prefix.copy()).append("<- ").withStyle(ChatFormatting.AQUA).append(Component.empty().withStyle(ChatFormatting.LIGHT_PURPLE).append(source.getPlayer().getDisplayName())).append(Component.literal(" " + message).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)));
        }

        return SINGLE_SUCCESS;
    }
}