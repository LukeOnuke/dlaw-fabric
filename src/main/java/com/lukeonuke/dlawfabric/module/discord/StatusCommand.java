package com.lukeonuke.dlawfabric.module.discord;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StatusCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DlawFabric mod) {
        final ConfigurationService cs = ConfigurationService.getInstance();
        final MinecraftServer server = mod.getMinecraftServer();

        String address = cs.getMinecraftServerAddress();
        List<String> players = server.getPlayerManager().getPlayerList().stream().map(p -> Objects.requireNonNull(p.getDisplayName()).getString()).toList();
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(cs.getDiscordCommandColor())
                .setTitle(MarkdownUtil.bold("Server status"))
                .setThumbnail("https://api.mcsrvstat.us/icon/" + address)
                .addField("Online:", String.valueOf(players.size()), true)
                .addField("Max:", String.valueOf(server.getMaxPlayerCount()), true)
                .addField("Version:", server.getVersion(), false)
                .setTimestamp(Instant.now());

        if (!players.isEmpty()) {
            builder.addField("List:", players.toString(), false);
        }

        // Send response
        event.getHook().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("status", "Shows server status");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
