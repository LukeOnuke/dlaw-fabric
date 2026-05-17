package com.lukeonuke.dlawfabric.module.discord.eventlisteners;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ChatListener extends ListenerAdapter implements EventListener {
    private final DlawFabric mod;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannelType() != ChannelType.TEXT) return;

        User author = event.getAuthor();
        Message message = event.getMessage();

        // In these cases we reject the check
        if (author.isBot() || author.isSystem() || message.isWebhookMessage()) {
            return;
        }

        String content = getMessageContent(message);
        // Obv, no blank shi
        if (content.isBlank()) {
            return;
        }

        final TextChannel channel = event.getChannel().asTextChannel();
        final ConfigurationService cs = ConfigurationService.getInstance();

        // Chat channel
        if (channel.getId().equals(cs.getDiscordChatChannelID())) {
            Message referencedMessage = message.getReferencedMessage();
            MutableComponent replyText = Component.empty();
            if (referencedMessage != null) {
                String referencedMessageContent = getMessageContent(referencedMessage);
                if (!referencedMessageContent.isBlank()) {
                    replyText = Component.empty()
                            .append(
                                    Component.literal(" Replying to ").withStyle(ChatFormatting.DARK_PURPLE)
                            ).append(
                                    Component.literal(referencedMessage.getAuthor().getEffectiveName()).withStyle(ChatFormatting.LIGHT_PURPLE)
                            ).append(
                                    Component.literal(": ").withStyle(ChatFormatting.DARK_PURPLE)
                            ).append(
                                    Component.empty().withStyle(ChatFormatting.RESET)
                            ).append(
                                    Component.literal(PluginUtils.truncateString(referencedMessageContent, 55) + "\n").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                            );
                }
            }

            content = content.replace("§", "&");
            MutableComponent text = replyText.append(
                    Component.literal("> ").withStyle(ChatFormatting.DARK_PURPLE).append(
                            Component.literal(author.getEffectiveName()).withStyle(ChatFormatting.LIGHT_PURPLE).append(
                                    Component.literal(": " + PluginUtils.truncateString(content, 256)).withStyle(ChatFormatting.WHITE)
                            )
                    )
            );

            text.setStyle(
                    text.getStyle().withHoverEvent(
                            new HoverEvent.ShowText(Component.literal("This is a message sent from discord. \nPowered by dlaw-fabric."))
                    )
            );

            mod.getMinecraftServer().getPlayerList().broadcastSystemMessage(text, false);
        }
    }

    /**
     * Get textual representation of message content. Aware of attachments.
     * @param message Message to be translated.
     * @return Attachment aware message content.
     */
    private String getMessageContent(Message message) {
        final String content = message.getContentStripped();
        if (content.isBlank()) {
            if (message.getAttachments().isEmpty()) {
                return "";
            }
            return "(attachment)";
        }
        if (message.getAttachments().isEmpty()) return content;
        return "(attachment) " + content;
    }
}