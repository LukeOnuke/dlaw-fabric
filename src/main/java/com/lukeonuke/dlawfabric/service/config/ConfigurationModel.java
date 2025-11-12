package com.lukeonuke.dlawfabric.service.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConfigurationModel {
    protected boolean respectGlobalBans;

    protected String discordStaffRoleID;
    protected String discordGuildID;
    protected int discordCommandColor;
    protected int discordSystemColor;
    protected int discordJoinColor;
    protected int discordLeaveColor;
    protected String discordChatChannelID;
    protected String discordToken;
    protected String discordLinkedRoleID;
    protected String activity3HourRole;
    protected String activity24HourRole;
    protected String activity72HourRole;


    protected boolean authenticationEnabled;
    protected String authUser;
    protected String authToken;

    protected String minecraftServerAddress;
    protected String serverName;

    protected boolean apiEnabled;
    protected int apiPort;

    protected static ConfigurationModel getBlankConfig() {
        return new ConfigurationModel(
                true,
                "",
                "",
                0xff4d01,
                0x00ffff,
                0x14de25,
                0xde1414,
                "",
                "",
                "",
                "",
                "",
                "",
                true,
                "auth-user",
                "auth-token",
                "example.com",
                "example",
                true,
                8010);
    }
}
