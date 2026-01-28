package com.lukeonuke.dlawfabric.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukeonuke.dlawfabric.DlawFabric;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * Service class responsible for handling personal and global muting.
 *
 * Mute Data is stored inside a HashMap that maps a UUID to a HashSet of
 * UUID's.
 * To indicate a global mute, a users HashSet will contain a NIL UUID (uuid
 * that consists of all zeros).
 * To indicate a personal mute between two players, a players HashSet will
 * contain all the players that he has muted.
 */
public class MuteService {
    private HashMap<UUID, HashSet<UUID>> data = new HashMap<>();
    private static MuteService instance = null;

    private final UUID nillUUID = new UUID(0L, 0L);
    private final Path configPath;
    private boolean dirty = false;

    public static MuteService getInstance(){
        if (instance == null) {
            instance = new MuteService();
        }
        return instance;
    }

    public MuteService(){
        ObjectMapper om = new ObjectMapper();
        configPath = FabricLoader.getInstance().getConfigDir().resolve("dlaw-fabric/mute-data.json");
        try{
            Files.createDirectories(configPath.getParent());
            if(!configPath.toFile().exists()) om.writeValue(configPath.toFile(), new HashMap<UUID, HashSet<UUID>>());
            data = om.readValue(configPath.toFile(), new TypeReference<HashMap<UUID, HashSet<UUID>>>(){});
        }catch (Exception e){
            DlawFabric.LOGGER.error("FATAL ERROR - Encountered exception whilst reading mute-data.json! {} {}", e.getClass().getName(), e.getMessage());
        }

        ServerLifecycleEvents.BEFORE_SAVE.register((minecraftServer, b, b1) -> {
            save();
        });
    }

    public boolean isGloballyMuted(UUID player){
        if (!data.containsKey(player)) return false;
        return data.get(player).contains(nillUUID);
    }

    public boolean isPersonallyMuted(UUID sender, UUID receiver){
        if (!data.containsKey(receiver)) return false;
        return data.get(receiver).contains(sender);
    }

    public void globallyMute(UUID player){
        ensureHashSetExists(player);
        data.get(player).add(nillUUID);
        markDirty();
    }

    public void personallyMute(UUID playerToBeMuted, UUID muter){
        ensureHashSetExists(muter);
        data.get(muter).add(playerToBeMuted);
        markDirty();
    }

    public void globallyUnmute(UUID player){
        ensureHashSetExists(player);
        data.get(player).remove(nillUUID);
        markDirty();
    }

    public void personallyUnmute(UUID playerToBeMuted, UUID muter){
        ensureHashSetExists(muter);
        data.get(muter).remove(playerToBeMuted);
        markDirty();
    }

    public void save(){
        if(!dirty) return;
        ObjectMapper om = new ObjectMapper();
        try{
            Files.createDirectories(configPath.getParent());
            om.writeValue(configPath.toFile(), data);
            dirty = false;
            DlawFabric.LOGGER.info("Saved mute-data.json!");
        }catch (Exception e){
            DlawFabric.LOGGER.error("FATAL ERROR - Encountered exception whilst saving mute-data.json! {} {}", e.getClass().getName(), e.getMessage());
        }
    }

    private void ensureHashSetExists(UUID uuid){
        if(!data.containsKey(uuid)){
            data.put(uuid, new HashSet<>());
        }
    }

    private void markDirty(){
        dirty = true;
    }
}
