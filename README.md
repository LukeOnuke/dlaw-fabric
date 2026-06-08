# <img src="https://github.com/LukeOnuke/dlaw-fabric/blob/main/art/logo.png?raw=true" height=16></img> dlaw-fabric
![](https://img.shields.io/badge/made_for-fabric_mc-green)
![GitHub License](https://img.shields.io/github/license/lukeonuke/dlaw-fabric)


Fully feature complete discord link for fabric servers, includes
- Discord/Minecraft user linking
- Complete chat integration
- Ban sync with discord server
- `/mute` and `/ignore` commands

# Tutorial to linking your Minecraft server chat to a discord channel.

Accurate as of June 3rd, 2026.

## Requirements:

- You (or whoever owns the bot) must be a moderator or owner of the Discord Server
- Your Java Minecraft server must be running Fabric 26.1.2 (Or whichever newest version this mod now supports)
- Do all of this on a PC please

## Overview

0. Obtain an "authToken"
1. Discord Bot
2. Discord Channel
3. Minecraft Server
4. Verification
5. Functionality

### 0. Obtain an "authToken" (This step is the bottle neck to the entire process. If you haven't completed this step, don't do the next steps yet).

  1. Send a friend request to Discord user "pequla". 
	2. Once this person accepts the friend request, send a message like "I am here from LukeONuke's Discord integration mod for Minecraft. Can I please have an authToken?". 
	3. ***Be patient and wait for a reply, and do not harass pequla for any reasons.***. 
	4. After you obtain an "authToken", save this in your Notepad. Do not share this token witn anyone else.

### 1. Discord Bot

  1. Go to your [Discord Developer Portal](https://discord.com/developers/applications/) (Make sure you're logged into your Discord account)
	2. Click "New Application" on the top right corner, and name it (This will be the Discord Bot's name)
	3. Optionally, update the General Information page of your bot by uploading a profile picture and writing a description (It can be anything). Click "Save Changes"
	4. On the left ribbon, select the "Installation" tab. Deselect "User Install", and select "None" from the "Install Link" dropdown. Click "Save Changes"
	5. On the left ribbon, select the "Bot" tab. Under "Authorization Flow", deselect "Public Bot". Under "Privileged Gateway Intents", select "Server Members Intent" and "Message Content Intent". Click "Save Changes"
	6. On the same tab, scroll up until you see "Reset Token". Click it and enter your "Multi-Factor Authentication" code to receive your bot code. ***DO NOT SHARE THIS TOKEN TO ANYONE ELSE, OR YOU MIGHT GET HACKED***. Copy this token and save it in your Notepad as "discordToken"
	7. On the left ribbon, select "General Information". Click "Copy" under "Application ID".
	8. Go to [this website](https://scarsz.me/authorize) and paste your "Application ID". You should be taken to a discord bot verification page. Choose your server from the "Add To Server" dropdown, and click "Authorize". This will let your bot join your server.

### 2. Discord Channel

  1. In Discord, click on your "User Settings", then scroll down until you see the "Advanced" tab. Make sure "Developer Mode" is enabled.
	2. Left click your profile pictire on the bottom left and click "Copy User ID". Save it in your Notepad as "authUser"
	3. Left click your server's name on the top left and click "Copy Server ID". Save it in your Notepad as "discordGuildID"
	4. In your Discord Server, create a new channel to link to your Minecraft server chat. This will be where you and your friends link your Discord accounts to your Minecraft accounts. Left click on the channel and click "Copy Channel ID". Save this in your Notepad as "discordChatChannelID" 
	5. In your Discord Server, go to your server settings and create a new role for your Discord Bot in the "Roles" tab. Switch to the "Permissions" tab on the top ribbon of your new role, scroll down and select "Administrator" under "Advanced Permissions". Switch to the "Mange Members" tab, click "Add Members" and add your Discord bot. Save your changes.
	6. Create a new role for discord users who have successfully linked their discord account with their Minecraft account (You can name it anything). Right click on the role and click "Copy Role ID" and save this in your Notepad as "discordLinkedRoleID"
	7. Your server should have an "Admin", "Mod" or "Owner" role for discord moderators/the owner (and you should have this role as well). Right click on the role and click "Copy Role ID" and save this in your Notepad as "discordStaffRoleID" 	
	8. Optionally, create 3 new roles for verified users who have been online for over 3, 24 and 72 hours in the server respectively. Right click on the roles and click "Copy Role ID" and save these in your Notepad as "activity3HourRole", "activity24HourRole", "activity72HourRole", respectively

### 3. Minecraft Server

  1. Go to [Dlaw-Fabric's Modrinth page](https://modrinth.com/mod/dlaw-fabric) and select the appropriate version for your server. The downloaded file should be named something like "dlaw-fabric-X.X.X-all.jar"
	2. Go to [fabric-permission-api's Github page](https://github.com/lucko/fabric-permissions-api/releases/). In the box where there is a small green "Latest" label, left click "fabric-permissions-api-X.X.X.jar" to download the file.
	3. Make sure your Minecraft server is off. Put both downloaded .jars files into your Minecraft server's "mods" folder and start the server, and wait. You know you have done it right when you see "Done (6.241s)! For help, type "help"" in your Minecraft server console with no new errors.
	4. Turn your server off after the last step. Go into your Minecraft server files and click on "config" => "dlaw-fabric" => "config.json". Edit and save this .json file by pasting all your saved IDs and token from previous steps into the corresponding quotes. For example, "discordStaffRoleID": "" becomes "discordStaffRoleID": "122345464754742467". Here are all fields you need to fill out: 
		-"discordStaffRoleID"
		-"discordGuildID"
		-"discordChatChannelID"
		-"discordToken"
		-"discordLinkedRoleID"
		-"authUser"
		-"minecraftServerAddress" (just your Minecraft server address)
		-"serverName" (Minecraft server name, can be anything)
		-"authToken" (see next step)
	5. Run the server and make sure there are no new errors

### 4. Verification

  1. Go to your Discord server and go to the newly created channel that links to your Minecraft server. To see if it is properly linked with your Minecraft server, make sure the bot has sent these messages after starting the server: "Server starting..." and "Server started!"
	2. Everyone on your Discord server who wishes to play on the Minecraft server will need to go into this channel and type /verify [insert Minecraft username] to link their Discord account to their Minecraft account (This applies to you, as well). Make sure the bot replies the command with "You have successfully linked your minecraft account".
	3. In this channel, you can also type /unverify to 

***If there are any errors and confusion, please find and contact me on Discord, my username is lukeonuke. Please be respectful and patient.***
