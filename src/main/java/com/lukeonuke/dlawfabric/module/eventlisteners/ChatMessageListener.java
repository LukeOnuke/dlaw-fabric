package com.lukeonuke.dlawfabric.module.eventlisteners;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;

@AllArgsConstructor
public class ChatMessageListener implements ServerMessageEvents.ChatMessage{
    private final DlawFabric mod;
    @Override
    public void onChatMessage(SignedMessage message, ServerPlayerEntity player, MessageType.Parameters parameters) {
        new Thread(() -> {
            final ConfigurationService cs = ConfigurationService.getInstance();
            TextChannel channel = mod.getJda().getTextChannelById(cs.getDiscordChatChannelID());
            if(channel != null){
                channel.sendMessage(MarkdownUtil.bold(player.getName().getString()) + " " + MarkdownUtil.monospace(message.getContent().getString())).queue();
            }
        }).start();
    }
}
