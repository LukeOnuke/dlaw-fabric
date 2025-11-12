package com.lukeonuke.dlawfabric.module.discord;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.model.PlayerData;
import com.lukeonuke.dlawfabric.model.backend.DataModel;
import com.lukeonuke.dlawfabric.service.DataService;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.io.IOException;
import java.time.Instant;

public class LookupCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DlawFabric mod) {
        final ConfigurationService cs = ConfigurationService.getInstance();
        OptionMapping option = event.getOption("minecraft-username");
        if (option == null) {
            throw new RuntimeException("Minecraft username is a required command argument");
        }

        try {
            String username = option.getAsString();
            PlayerData account = DataService.getInstance().getAccount(username);
            DataModel data = DataService.getInstance().getData(account.getId());

            event.getJDA().retrieveUserById(data.getUser().getDiscordId()).queue(user ->
                    event.getHook().sendMessage("Flowing data was found")
                            .addEmbeds(new EmbedBuilder()
                                    .setColor(cs.getDiscordCommandColor())
                                    .setTitle(MarkdownUtil.bold("Verification data"))
                                    .addField("User:", user.getAsMention(), false)
                                    .addField("Username:", account.getName(), false)
                                    .setThumbnail(user.getEffectiveAvatarUrl())
                                    .setImage(PluginUtils.playerBustUrl(account.getId()))
                                    .setTimestamp(Instant.now())
                                    .build()
                            ).queue());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("lookup", "Looks up a Minecraft account").addOptions(
                new OptionData(OptionType.STRING,
                        "minecraft-username",
                        "Accepts only Minecraft Java usernames from paid accounts",
                        true)
        );
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
