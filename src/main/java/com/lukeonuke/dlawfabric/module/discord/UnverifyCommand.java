package com.lukeonuke.dlawfabric.module.discord;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.DataService;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class UnverifyCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DlawFabric mod) {
        try {
            final ConfigurationService cs = ConfigurationService.getInstance();
            User user = event.getUser();
            DataService.getInstance().deleteData(
                    user.getId(),
                    cs.getAuthUser(),
                    cs.getAuthToken()
            );

            // Adding verified role
            String role = cs.getDiscordLinkedRoleID();
            PluginUtils.removeRoleFromMember(event.getMember(), role);

            event.getHook().sendMessage("You have successfully removed your verification!").queue();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("unverify", "Unlinks discord and minecraft account");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
