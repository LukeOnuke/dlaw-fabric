package com.lukeonuke.dlawfabric.module.discord;

import com.lukeonuke.dlawfabric.DlawFabric;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommand {
    void execute(SlashCommandInteractionEvent event, DlawFabric mod);

    CommandData getCommandData();

    boolean isAdminOnly();
}
