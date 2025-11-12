package com.lukeonuke.dlawfabric.module.discord;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.model.PlayerData;
import com.lukeonuke.dlawfabric.model.backend.DataModel;
import com.lukeonuke.dlawfabric.model.backend.LinkModel;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.DataService;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.time.Instant;

public class VerifyCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DlawFabric mod) {
        OptionMapping option = event.getOption("minecraft-username");
        if (option == null) {
            throw new RuntimeException("Minecraft username is a required command argument");
        }
        try {
            final ConfigurationService cs = ConfigurationService.getInstance();

            DataService service = DataService.getInstance();
            PlayerData account = service.getAccount(option.getAsString());
            User user = event.getUser();
            DataModel data = service.saveData(
                    LinkModel.builder()
                            .uuid(account.getId())
                            .userId(user.getId())
                            .guildId(event.getGuild().getId())
                            .build(),
                    cs.getAuthUser(),
                    cs.getAuthToken()
            );

            // Adding verified role
            new Thread(() -> {
                String role = cs.getDiscordLinkedRoleID();
                PluginUtils.addRoleToMember(event.getMember(), role);
            }).start();

            event.getHook().sendMessage(user.getAsMention() + " You have successfully linked your minecraft account")
                    .addEmbeds(new EmbedBuilder()
                            .setColor(cs.getDiscordCommandColor())
                            .setTitle(MarkdownUtil.bold("Verification data"))
                            .addField("User:", user.getEffectiveName(), false)
                            .addField("Username:", account.getName(), false)
                            .setThumbnail(user.getEffectiveAvatarUrl())
                            .setImage(PluginUtils.playerBustUrl(account.getId()))
                            .setTimestamp(Instant.now())
                            .setFooter("Database ID: " + data.getId())
                            .build()
                    ).queue();
        } catch (Exception ex) {
            // Return it back
            throw new RuntimeException(ex);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("verify", "Links a Minecraft account").addOptions(
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
