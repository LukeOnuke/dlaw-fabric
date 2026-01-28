package com.lukeonuke.dlawfabric.module.minecraft.command;

import com.lukeonuke.dlawfabric.service.MuteService;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

public class PmCommand extends BoltsCommand {
    private final MutableText prefix = Text.literal("you ").formatted(Formatting.BOLD, Formatting.DARK_PURPLE).append(Text.empty().formatted(Formatting.RESET));

    @Override
    public String getCommandName() {
        return "pm";
    }

    @Override
    public List<RequiredArgumentBuilder<ServerCommandSource, ?>> getArguments() {
        return List.of(CommandManager.argument("target", EntityArgumentType.player()),
                CommandManager.argument("message", StringArgumentType.greedyString()));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(commandContext, "target");
        ServerCommandSource source = commandContext.getSource();

        if (!source.isExecutedByPlayer() || source.getPlayer() == null){
            source.sendError(PluginUtils.formatFullErrorMessage("Command can only be run by a player!"));
            return SINGLE_SUCCESS;
        }

        if(target == null){
            source.sendError(PluginUtils.formatFullErrorMessage("Can't find player."));
            return SINGLE_SUCCESS;
        }

        final MuteService ms = MuteService.getInstance();
        final UUID sender = source.getPlayer().getUuid();

        String message = StringArgumentType.getString(commandContext, "message");
        source.sendMessage(Text.empty().append(prefix.copy()).append("-> ").formatted(Formatting.GREEN).append(Text.empty().formatted(Formatting.LIGHT_PURPLE).append(target.getDisplayName())).append(Text.literal(" " + message).formatted(Formatting.ITALIC, Formatting.GRAY)));
        if(ms.isGloballyMuted(sender)) return SINGLE_SUCCESS;
        if(!ms.isPersonallyMuted(sender, target.getUuid())){
            target.sendMessage(Text.empty().append(prefix.copy()).append("<- ").formatted(Formatting.AQUA).append(Text.empty().formatted(Formatting.LIGHT_PURPLE).append(source.getPlayer().getDisplayName())).append(Text.literal(" " + message).formatted(Formatting.ITALIC, Formatting.GRAY)));
        }

        return SINGLE_SUCCESS;
    }
}
