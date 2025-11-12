package com.lukeonuke.dlawfabric.module.discord;

import com.lukeonuke.dlawfabric.DlawFabric;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class SeedCommand implements SlashCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, DlawFabric mod) {
        if(mod.getMinecraftServer() == null) {
            event.getHook().sendMessage("Minecraft server hasn't loaded yet.").queue();
            return;
        }
        String seed = String.valueOf(mod.getMinecraftServer().getOverworld().getSeed());
        event.getHook().sendMessage("Seed: " + MarkdownUtil.bold(seed)).queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("seed", "Displays world seed");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
