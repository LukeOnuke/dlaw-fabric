package com.lukeonuke.dlawfabric.module.minecraft.eventlisteners;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.TimeoutService;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.UUID;

public class PlayerLoginCombinedListener implements ServerPlayConnectionEvents.Init, ServerPlayConnectionEvents.Disconnect {
    private final DlawFabric mod;

    public PlayerLoginCombinedListener(DlawFabric mod) {
        this.mod = mod;
    }

    @Override
    public void onPlayInit(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        ServerPlayer player = handler.player;

        /*
         * SECTION : DISCORD MANAGEMENT AND NOTIFICATION
         * =============================================
         * */
        final Component vanillaMessage = Component.translatable("multiplayer.player.joined", player.getDisplayName());
        final int playTime = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));

        new Thread(() -> {
            final ConfigurationService cs = ConfigurationService.getInstance();

            sendMessage(player, cs.getDiscordJoinColor(), vanillaMessage.getString(), getOnline() + 1, false);
            if (playTime == 0) {
                server.getPlayerList().broadcastSystemMessage(Component.literal(ChatFormatting.DARK_PURPLE + "Welcome " + player.getDisplayName().getString() + " to " + cs.getServerName() + ChatFormatting.RESET + "!"), false);
                mod.sendPlayerEmbed(player, cs.getDiscordJoinColor(), new EmbedBuilder().setDescription("It's " + PluginUtils.escapeMarkdown(player.getDisplayName().getString()) + " first time on the server!"));
            }

            int playTimeHours = playTime / (20 * 60 * 60); //playtime in hours

            Guild guild = mod.getJda().getGuildById(cs.getDiscordGuildID());
            if (guild == null) return;

            Member member = guild.retrieveMemberById(
                    mod.getPlayers().get(player.getUUID()).getId()
            ).complete();

            if (playTimeHours > 2) {
                PluginUtils.addRoleToMember(member, cs.getActivity3HourRole());
            }
            if (playTimeHours > 23) {
                PluginUtils.addRoleToMember(member, cs.getActivity24HourRole());
            }
            if (playTimeHours > 71) {
                PluginUtils.addRoleToMember(member, cs.getActivity72HourRole());
            }
        }, "dlaw-worker-role-autoasign").start();
    }

    @Override
    public void onPlayDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer minecraftServer) {
        final ServerPlayer player = handler.player;
        final UUID uuid = player.getUUID();
        final TimeoutService ts = TimeoutService.getInstance();
        if(ts.isTimeoutOver(uuid)) ts.addTimeout(uuid, 5);

        /*
         * SECTION : DISCORD MANAGEMENT AND NOTIFICATION
         * =============================================
         * */
        Component vanillaMessage = Component.translatable("multiplayer.player.left", player.getDisplayName());
        final ConfigurationService cs = ConfigurationService.getInstance();
        sendMessage(player, cs.getDiscordLeaveColor(), vanillaMessage.getString(), getOnline() - 1, true);
    }

    private void sendMessage(ServerPlayer player, int color, String title, int online, boolean quit) {
        int max = mod.getMinecraftServer().getMaxPlayers();
        new Thread(() -> {
            // Update bot activity
            String text = online + " online";
            if (online == 0) {
                text = "alone";
            }
            mod.getJda().getPresence().setActivity(Activity.playing(text));

            // Send chat message
            mod.sendPlayerEmbed(player, color, new EmbedBuilder()
                    .setDescription(MarkdownUtil.bold(title))
                    .addField("Online:", online + "/" + max, false));

            if (quit) {
                // Removing the player from cache
                mod.getPlayers().remove(player.getUUID());
            }
        }).start();
    }

    private int getOnline() {
        return mod.getMinecraftServer().getPlayerList().getPlayerCount();
    }
}