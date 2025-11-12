package com.lukeonuke.dlawfabric.service;

import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TimeoutService {
    private TimeoutService() {}
    private static TimeoutService instance = null;

    public static int TIMEOUT_CLASSIC = 10;
    public static int TIMEOUT_PENALTY = 60;
    public static int TIMEOUT_BANNED = 600;

    public static TimeoutService getInstance(){
        if(instance == null) instance = new TimeoutService();
        return instance;
    }

    private HashMap<UUID, PlayerTimeoutData> data = new HashMap<>();

    @Getter
    private static class PlayerTimeoutData{
        private Instant lastJoin;
        private int timeout;

        public PlayerTimeoutData(int timeout) {
            this.timeout = timeout;
            this.lastJoin = Instant.now();
        }

        public boolean isTimeoutOver(){
            return Instant.now().isAfter(lastJoin.plusSeconds(timeout));
        }
    }

    public void addTimeout(UUID uuid, int timeout){
        data.put(uuid, new PlayerTimeoutData(timeout));
    }

    public boolean isTimeoutOver(UUID uuid){
        PlayerTimeoutData ptd = data.get(uuid);
        if(ptd == null) return true;
        return ptd.isTimeoutOver();
    }

    public int getTimeout(UUID uuid){
        PlayerTimeoutData ptd = data.get(uuid);
        if(ptd == null) return -1;
        return (int) (ptd.lastJoin.plusSeconds(ptd.timeout).getEpochSecond() - Instant.now().getEpochSecond());
    }
}
