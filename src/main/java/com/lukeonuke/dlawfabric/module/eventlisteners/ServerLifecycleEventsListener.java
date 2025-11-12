package com.lukeonuke.dlawfabric.module.eventlisteners;

import com.lukeonuke.dlawfabric.DlawFabric;
import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

@AllArgsConstructor
public class ServerLifecycleEventsListener implements ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStarting, ServerLifecycleEvents.ServerStopped, ServerLifecycleEvents.ServerStopping {
    private DlawFabric mod;
    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        mod.sendSystemEmbed("Server started!");
    }

    @Override
    public void onServerStarting(MinecraftServer minecraftServer) {
        mod.sendSystemEmbed("Server starting...");
    }

    @Override
    public void onServerStopped(MinecraftServer minecraftServer) {
    }

    @Override
    public void onServerStopping(MinecraftServer minecraftServer) {
        mod.sendSystemEmbed("Server stopping...");
        mod.shutdown();
    }
}
