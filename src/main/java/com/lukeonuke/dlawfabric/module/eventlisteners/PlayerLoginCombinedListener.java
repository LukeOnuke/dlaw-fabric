package com.lukeonuke.dlawfabric.module.eventlisteners;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.model.DiscordModel;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.TimeoutService;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.security.auth.login.LoginException;
import java.util.UUID;

public class PlayerLoginCombinedListener implements ServerPlayConnectionEvents.Init, ServerPlayConnectionEvents.Disconnect {
    private final DlawFabric mod;

    public PlayerLoginCombinedListener(DlawFabric mod) {
        this.mod = mod;
    }

    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        GameProfile gameProfile = player.getGameProfile();
        PlayerManager playerManager = server.getPlayerManager();
        UUID playerUUID = player.getUuid();
        final TimeoutService ts = TimeoutService.getInstance();

        // Timeout/Cooldown management
        if (!ts.isTimeoutOver(playerUUID)) {
            handler.disconnect(Text.literal("Wait " + ts.getTimeout(playerUUID) + " more second(s) before reconnecting.").formatted(Formatting.GREEN));
            return;
        }

        // Check if whitelist will allow player, and if not: skip.
        if (server.isEnforceWhitelist() && !playerManager.isWhitelisted(gameProfile)) {
            ts.addTimeout(playerUUID, TimeoutService.TIMEOUT_BANNED);
            handler.disconnect(Text.of("You are not whitelisted."));
            return;
        }
        // Check if player is banned, and if yes: skip.
        if (playerManager.getUserBanList().contains(gameProfile) || playerManager.getIpBanList().isBanned(player.getIp())) {
            ts.addTimeout(playerUUID, TimeoutService.TIMEOUT_BANNED);
            handler.disconnect(Text.literal("You have been banned.").formatted(Formatting.RED));
            return;
        }

        /*
        * SECTION : DISCORD MANAGEMENT AND NOTIFICATION
        * =============================================
        * */
        final Text vanillaMessage = Text.translatable("multiplayer.player.joined", handler.getPlayer().getDisplayName());
        final int playTime = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

        new Thread(() -> {
            final ConfigurationService cs = ConfigurationService.getInstance();

            sendMessage(player, cs.getDiscordJoinColor(), vanillaMessage.getString(), getOnline() + 1, false);
            if (playTime == 0) {
                server.getPlayerManager().broadcast(Text.of(Formatting.DARK_PURPLE + "Welcome " + player.getDisplayName().getString() + " to " + cs.getServerName() + Formatting.RESET + "!"), false);
                mod.sendPlayerEmbed(player, cs.getDiscordJoinColor(), new EmbedBuilder().setDescription("It's " + PluginUtils.escapeMarkdown(player.getDisplayName().getString()) + " first time on the server!"));
            }

            int playTimeHours = playTime / (20 * 60 * 60); //playtime in hours

            Guild guild = mod.getJda().getGuildById(cs.getDiscordGuildID());
            if (guild == null) return;

            Member member = guild.retrieveMemberById(
                    mod.getPlayers().get(handler.getPlayer().getUuid()).getId()
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
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer minecraftServer) {
        final ServerPlayerEntity player = handler.getPlayer();
        final UUID uuid = player.getUuid();
        final TimeoutService ts = TimeoutService.getInstance();
        if(ts.isTimeoutOver(uuid)) ts.addTimeout(uuid, 5);

        /*
         * SECTION : DISCORD MANAGEMENT AND NOTIFICATION
         * =============================================
         * */
        Text vanillaMessage = Text.translatable("multiplayer.player.left", handler.getPlayer().getDisplayName());
        final ConfigurationService cs = ConfigurationService.getInstance();
        sendMessage(player, cs.getDiscordLeaveColor(), vanillaMessage.getString(), getOnline() - 1, true);
    }

    private void sendMessage(ServerPlayerEntity player, int color, String title, int online, boolean quit) {
        int max = mod.getMinecraftServer().getMaxPlayerCount();
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
                mod.getPlayers().remove(player.getUuid());
            }
        }).start();
    }

    private int getOnline() {
        return mod.getMinecraftServer().getPlayerManager().getCurrentPlayerCount();
    }
}
