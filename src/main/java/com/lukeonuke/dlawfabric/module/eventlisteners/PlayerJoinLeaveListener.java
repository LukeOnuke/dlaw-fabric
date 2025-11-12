package com.lukeonuke.dlawfabric.module.eventlisteners;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.TimeoutService;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.security.auth.login.LoginException;
import java.text.Normalizer;

@AllArgsConstructor
public class PlayerJoinLeaveListener implements ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect {
    private final DlawFabric mod;

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer minecraftServer) {
        final TimeoutService ts = TimeoutService.getInstance();
        if(!ts.isTimeoutOver(handler.getPlayer().getUuid())) return;

        ServerPlayerEntity player = handler.getPlayer();
        Text vanillaMessage = Text.translatable("multiplayer.player.left", handler.getPlayer().getDisplayName());
        final ConfigurationService cs = ConfigurationService.getInstance();
        //sendMessage(player, cs.getDiscordLeaveColor(), vanillaMessage.getString(), getOnline()-1, true);
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer minecraftServer) {
        ServerPlayerEntity player = handler.getPlayer();

        final TimeoutService ts = TimeoutService.getInstance();
        if(!ts.isTimeoutOver(player.getUuid())) return;


    }
}
