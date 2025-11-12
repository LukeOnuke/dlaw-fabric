package com.lukeonuke.dlawfabric.module.discord;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class IpCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DlawFabric mod) {
        final ConfigurationService cs = ConfigurationService.getInstance();
        String address = cs.getMinecraftServerAddress();
        event.getHook().sendMessage("IP: " + MarkdownUtil.bold(address)).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("ip", "Shows server address");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
