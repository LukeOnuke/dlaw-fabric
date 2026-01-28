package com.lukeonuke.dlawfabric.service;

import com.lukeonuke.dlawfabric.model.DiscordModel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.UUID;
/**
 * Service made to cache players so that they can skip authorisation. Intended
 * to fix a bug in 1.21.11 where canPlayerJoin is called twice, hence creating
 * double the lag when waiting for api responses.
 */
public class CacheService {
    // Singleton
    private CacheService() {
    }
    private static CacheService instance = null;
    public static CacheService getInstance(){
        if (instance == null) {
            instance = new CacheService();
        }
        return instance;
    }

    // Actual storage for the cache. Hashmap used cause of its O(1) access time
    private final HashMap<String, CacheContainer> storage = new HashMap<>();

    /**
     * Check if cache contains a valid entry for the player (identified via
     * uuid string).
     * @param uuid UUID of player.
     * @return Boolean value indicating if cache contains valid entry for
     * player.
     */
    public boolean contains(String uuid){
        CacheContainer cc = storage.get(uuid);
        if(cc == null) return false;
        return cc.isValid();
    }

    /**
     * Store entry for player.
     * @param uuid UUID of player.
     * @param discordModel Discord data of player.
     */
    public void put(String uuid, DiscordModel discordModel){
        storage.put(uuid, new CacheContainer(discordModel));
    }

    /**
     * Get data for player.
     * @param uuid UUID of player.
     * @return Discord data of player stored in cache.
     */
    public DiscordModel get(String uuid){
        return storage.get(uuid).getDiscordModel();
    }

    /**
     * Container for cache values, contains code for checking entry
     * validity and stores everything in a nice little package.
     */
    @Getter
    @Setter
    private static class CacheContainer{
        public CacheContainer(DiscordModel discordModel) {
            this.discordModel = discordModel;
            this.time = System.currentTimeMillis();
        }

        private final DiscordModel discordModel;
        private final long time;

        public boolean isValid(){
            return System.currentTimeMillis() < (time + 5000);
        }
    }
}
