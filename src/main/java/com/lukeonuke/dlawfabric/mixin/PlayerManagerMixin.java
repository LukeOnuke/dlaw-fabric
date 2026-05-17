package com.lukeonuke.dlawfabric.mixin;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.model.DiscordModel;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.TimeoutService;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.security.auth.login.LoginException;
import java.net.SocketAddress;
import java.util.UUID;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
    @Inject(
            method = "canPlayerLogin(Ljava/net/SocketAddress;Lnet/minecraft/server/players/NameAndId;)Lnet/minecraft/network/chat/Component;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dlaw_checkCanJoin(SocketAddress address, NameAndId gameProfile, CallbackInfoReturnable<Component> cir){
        final TimeoutService ts = TimeoutService.getInstance();
        final UUID playerUUID = gameProfile.id();
        final long timestamp = System.currentTimeMillis();

        // Timeout/Cooldown management
        if (!ts.isTimeoutOver(playerUUID)) {
            cir.setReturnValue(Component.literal("Wait " + ts.getTimeout(playerUUID) + " more second(s) before reconnecting.").withStyle(ChatFormatting.GREEN));
            return;
        }

        try {
            final DlawFabric mod = DlawFabric.getMod();
            DiscordModel discord = PluginUtils.authentificatePlayer(mod, playerUUID.toString());
            mod.getPlayers().put(playerUUID, discord);
            DlawFabric.LOGGER.info(String.format("%s authenticated as: %s [ID: %s] in %sms",
                    gameProfile.name(),
                    discord.getNickname(),
                    discord.getId(),
                    System.currentTimeMillis() - timestamp
            ));
        } catch (LoginException e) {
            String message = e.getMessage();
            cir.setReturnValue(Component.literal(message).withStyle(ChatFormatting.RED));
            ts.addTimeout(playerUUID, TimeoutService.TIMEOUT_CLASSIC);
        }
    }
}