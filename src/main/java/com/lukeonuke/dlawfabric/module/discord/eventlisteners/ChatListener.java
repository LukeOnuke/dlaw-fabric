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
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
            MutableText replyText = Text.empty();
            if (referencedMessage != null) {
                String referencedMessageContent = getMessageContent(referencedMessage);
                if (!referencedMessageContent.isBlank()) {
                    replyText = Text.empty()
                            .append(
                                    Text.literal(" Replying to ").formatted(Formatting.DARK_PURPLE)
                            ).append(
                                    Text.literal(referencedMessage.getAuthor().getEffectiveName()).formatted(Formatting.LIGHT_PURPLE)
                            ).append(
                                    Text.literal(": ").formatted(Formatting.DARK_PURPLE)
                            ).append(
                                    Text.empty().formatted(Formatting.RESET)
                            ).append(
                                    Text.literal(PluginUtils.truncateString(referencedMessageContent, 55) + "\n").formatted(Formatting.GRAY, Formatting.ITALIC)
                            );
                }
            }

            content = content.replace("§", "&");
            MutableText text = replyText.append(
                    Text.literal("> ").formatted(Formatting.DARK_PURPLE).append(
                            Text.literal(author.getEffectiveName()).formatted(Formatting.LIGHT_PURPLE).append(
                                    Text.literal(": " + PluginUtils.truncateString(content, 256)).formatted(Formatting.WHITE)
                            )
                    )
            );

            text.setStyle(
                    text.getStyle().withHoverEvent(
                            new HoverEvent.ShowText(Text.literal("This is a message sent from discord. \nPowered by dlaw-fabric."))
                    )
            );

            mod.getMinecraftServer().getPlayerManager().broadcast(text, false);
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
