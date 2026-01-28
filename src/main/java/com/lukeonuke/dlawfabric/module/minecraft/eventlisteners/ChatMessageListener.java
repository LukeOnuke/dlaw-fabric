package com.lukeonuke.dlawfabric.module.minecraft.eventlisteners;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.MuteService;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ChatMessageListener implements ServerMessageEvents.AllowChatMessage{
    private final DlawFabric mod;

    @Override
    public boolean allowChatMessage(@NotNull SignedMessage signedMessage, @NotNull ServerPlayerEntity sender, MessageType.@NotNull Parameters parameters) {
        final MuteService ms = MuteService.getInstance();

        // Check if globally muted
        if(ms.isGloballyMuted(sender.getUuid())) {
            sender.sendChatMessage(SentMessage.of(signedMessage), false, parameters);
            return false;
        }

        // Send message to discord
        new Thread(() -> {
            final ConfigurationService cs = ConfigurationService.getInstance();
            TextChannel channel = mod.getJda().getTextChannelById(cs.getDiscordChatChannelID());
            if(channel != null){
                channel.sendMessage(MarkdownUtil.bold(sender.getName().getString()) + " " + MarkdownUtil.monospace(signedMessage.getContent().getString())).queue();
            }
        }).start();

        // Get all other variables
        final Text message = signedMessage.getContent();
        final MinecraftServer server = mod.getMinecraftServer();
        // Send message to server
        server.sendMessage(Text.empty().append(Text.literal(sender.getName().getString() + ": ")).append(message));

        // Handle message sending to players
        for (ServerPlayerEntity receiver : server.getPlayerManager().getPlayerList()) {
            if (!ms.isPersonallyMuted(sender.getUuid(), receiver.getUuid())) {
                receiver.sendChatMessage(SentMessage.of(signedMessage), false, parameters);
            }
        }
        return false;
    }
}
