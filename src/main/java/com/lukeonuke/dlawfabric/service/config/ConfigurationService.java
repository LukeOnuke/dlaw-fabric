package com.lukeonuke.dlawfabric.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukeonuke.dlawfabric.DlawFabric;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationService {
    private static ConfigurationService instance = null;
    private final ConfigurationModel cm;


    private ConfigurationService() {
        final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("dlaw-fabric/config.json");
        final ObjectMapper objectMapper = new ObjectMapper();

        if(!configPath.toFile().exists()){
//            try (InputStream in = ConfigurationService.class.getResourceAsStream("assets/dlaw-fabric/config.json")) {
//                if (in == null) throw new RuntimeException("Default config file input stream unable to initialise!");
//                Files.createDirectories(configPath.getParent());
//                Files.copy(in, configPath);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            try {
                DlawFabric.LOGGER.warn("DLAW is starting for the first time, as such a blank configuration is being created.");
                DlawFabric.LOGGER.warn("Please fill in the config file with your tokens, as without them the server will not run.");
                Files.createDirectories(configPath.getParent());
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), ConfigurationModel.getBlankConfig());
                DlawFabric.LOGGER.info("Blank config file created at {}", configPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            cm = objectMapper.readValue(configPath.toFile(), ConfigurationModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConfigurationService getInstance(){
        if(instance == null) instance = new ConfigurationService();
        return instance;
    }

    public boolean getRespectGlobalBans(){
        return cm.respectGlobalBans;
    }

    public String getDiscordStaffRoleID(){
        return cm.discordStaffRoleID;
    }

    public String getDiscordGuildID(){
        return cm.discordGuildID;
    }

    public boolean getAuthenticationEnabled(){
        return cm.authenticationEnabled;
    }

    public String getMinecraftServerAddress(){
        return cm.minecraftServerAddress;
    }

    public int getDiscordCommandColor(){
        return cm.discordCommandColor;
    }

    public int getDiscordSystemColor(){
        return cm.discordSystemColor;
    }

    public int getDiscordJoinColor(){
        return cm.discordJoinColor;
    }

    public int getDiscordLeaveColor(){
        return cm.discordLeaveColor;
    }

    public String getDiscordChatChannelID(){
        return cm.discordChatChannelID;
    }

    public String getDiscordToken(){
        return cm.discordToken;
    }

    public String getDiscordLinkedRoleID(){
        return cm.discordLinkedRoleID;
    }

    public String getAuthUser(){
        return cm.authUser;
    }

    public String getAuthToken(){
        return cm.authToken;
    }

    public boolean getApiEnabled(){
        return cm.apiEnabled;
    }

    public int getApiPort(){
        return cm.apiPort;
    }

    public String getActivity3HourRole(){
        return cm.activity3HourRole;
    }

    public String getActivity24HourRole(){
        return cm.activity24HourRole;
    }

    public String getActivity72HourRole(){
        return cm.activity72HourRole;
    }

    public String getServerName(){
        return cm.serverName;
    }
}
