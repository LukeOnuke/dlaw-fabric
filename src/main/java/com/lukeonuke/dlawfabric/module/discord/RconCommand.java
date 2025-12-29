package com.lukeonuke.dlawfabric.module.discord;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import com.mojang.brigadier.ParseResults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.minecraft.command.CommandSource;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.awt.*;
import java.util.logging.Logger;

public class RconCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DlawFabric mod) {
//        Server server = plugin.getServer();
//        Logger logger = plugin.getLogger();
        Member member = event.getMember();
//
        OptionMapping option = event.getOption("command");
        if (option == null) {
            throw new RuntimeException("Minecraft command is a requited command argument");
        }

        ConfigurationService cs = ConfigurationService.getInstance();
        if(
                !(
                        member.getRoles().stream().map(role -> role.getId()).toList().contains(cs.getDiscordStaffRoleID())
                )
        ) throw new RuntimeException("You gotta be staff to execute this command!");

        // Member is OP beyond this point!
        MinecraftServer server = mod.getMinecraftServer();

        if (server == null) throw new RuntimeException("Server has not finished starting yet!");
        ServerCommandSource commandSource = mod.getMinecraftServer().getCommandSource().withPermissions(PermissionPredicate.ALL);
        server.getCommandManager().parseAndExecute(commandSource, option.getAsString());

        event.getHook().sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(MarkdownUtil.bold("Command executed"))
                .setDescription("Command " + MarkdownUtil.monospace(option.getAsString()) + " executed!")
                .build()).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("rcon", "Executes a command on the server").addOptions(
                new OptionData(OptionType.STRING,
                        "command",
                        "Minecraft command",
                        true)
        );
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
