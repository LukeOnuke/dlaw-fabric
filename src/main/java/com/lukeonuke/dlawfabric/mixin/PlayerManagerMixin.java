package com.lukeonuke.dlawfabric.mixin;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.model.DiscordModel;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.TimeoutService;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.security.auth.login.LoginException;
import java.net.SocketAddress;
import java.util.UUID;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(
            method = "checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dlaw_checkCanJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir){
        TimeoutService ts = TimeoutService.getInstance();
        UUID playerUUID = profile.getId();
        try {
            DiscordModel discord = PluginUtils.authentificatePlayer(mod, playerUUID.toString());
            mod.getPlayers().put(playerUUID, discord);
            DlawFabric.LOGGER.info(String.format("%s authenticated as: %s [ID: %s]",
                    profile.getName(),
                    discord.getNickname(),
                    discord.getId()
            ));
        } catch (LoginException e) {
            String message = e.getMessage();
            cir.setReturnValue(Text.literal(message).formatted(Formatting.RED));
            ts.addTimeout(playerUUID, TimeoutService.TIMEOUT_CLASSIC);
        }
    }
}
