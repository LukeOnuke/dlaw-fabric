package com.lukeonuke.dlawfabric.module.minecraft.eventlisteners;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.MuteService;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ChatMessageListener implements ServerMessageEvents.AllowChatMessage{
    private final DlawFabric mod;

    @Override
    public boolean allowChatMessage(@NotNull PlayerChatMessage signedMessage, @NotNull ServerPlayer sender, ChatType.@NotNull Bound parameters) {
        final MuteService ms = MuteService.getInstance();

        // Check if globally muted
        if(ms.isGloballyMuted(sender.getUUID())) {
            sender.sendChatMessage(OutgoingChatMessage.create(signedMessage), false, parameters);
            return false;
        }

        // Send message to discord
        new Thread(() -> {
            final ConfigurationService cs = ConfigurationService.getInstance();
            TextChannel channel = mod.getJda().getTextChannelById(cs.getDiscordChatChannelID());
            if(channel != null){
                channel.sendMessage(MarkdownUtil.bold(sender.getName().getString()) + " " + MarkdownUtil.monospace(signedMessage.decoratedContent().getString())).queue();
            }
        }).start();

        // Get all other variables
        final Component message = signedMessage.decoratedContent();
        final MinecraftServer server = mod.getMinecraftServer();
        // Send message to server
        server.sendSystemMessage(Component.empty().append(Component.literal(sender.getName().getString() + ": ")).append(message));

        // Handle message sending to players
        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (!ms.isPersonallyMuted(sender.getUUID(), receiver.getUUID())) {
                receiver.sendChatMessage(OutgoingChatMessage.create(signedMessage), false, parameters);
            }
        }
        return false;
    }
}