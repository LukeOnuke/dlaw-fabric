package com.lukeonuke.dlawfabric.mixin;

import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.model.DiscordModel;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.TimeoutService;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.security.auth.login.LoginException;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.UUID;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(
            method = "checkCanJoin(Ljava/net/SocketAddress;Lnet/minecraft/server/PlayerConfigEntry;)Lnet/minecraft/text/Text;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dlaw_checkCanJoin(SocketAddress address, PlayerConfigEntry configEntry, CallbackInfoReturnable<Text> cir){
        final TimeoutService ts = TimeoutService.getInstance();
        final UUID playerUUID = configEntry.id();
        final long timestamp = System.currentTimeMillis();

        // Timeout/Cooldown management
        if (!ts.isTimeoutOver(playerUUID)) {
            cir.setReturnValue(Text.literal("Wait " + ts.getTimeout(playerUUID) + " more second(s) before reconnecting.").formatted(Formatting.GREEN));
            return;
        }

        try {
            final DlawFabric mod = DlawFabric.getMod();
            DiscordModel discord = PluginUtils.authentificatePlayer(mod, playerUUID.toString());
            mod.getPlayers().put(playerUUID, discord);
            DlawFabric.LOGGER.info(String.format("%s authenticated as: %s [ID: %s] in %sms",
                    configEntry.name(),
                    discord.getNickname(),
                    discord.getId(),
                    System.currentTimeMillis() - timestamp
            ));
        } catch (LoginException e) {
            String message = e.getMessage();
            cir.setReturnValue(Text.literal(message).formatted(Formatting.RED));
            ts.addTimeout(playerUUID, TimeoutService.TIMEOUT_CLASSIC);
        }
    }
}
