package com.lukeonuke.dlawfabric;

import com.lukeonuke.dlawfabric.model.DiscordModel;
import com.lukeonuke.dlawfabric.module.discord.eventlisteners.ChatListener;
import com.lukeonuke.dlawfabric.module.discord.eventlisteners.CommandListener;
import com.lukeonuke.dlawfabric.module.eventlisteners.ChatMessageListener;
import com.lukeonuke.dlawfabric.module.eventlisteners.PlayerLoginCombinedListener;
import com.lukeonuke.dlawfabric.module.eventlisteners.ServerLifecycleEventsListener;
import com.lukeonuke.dlawfabric.service.PluginUtils;
import com.lukeonuke.dlawfabric.service.RestService;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class DlawFabric implements ModInitializer {
	public static final String MOD_ID = "dlaw-fabric";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ChatMessageListener chatMessageListener;
	private CommandListener commandModule;
	private JDA jda;
	private MinecraftServer minecraftServer;

	private final Map<UUID, DiscordModel> players = new HashMap<>();

	@Getter
	private static DlawFabric mod = null;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		long startTime = System.nanoTime();
		LOGGER.info("Initialising dlaw-fabric...");

		LOGGER.info("	Setting mod instance");
		mod = this;
		LOGGER.info("   Set!");

		LOGGER.info("   Reading config...");
		final ConfigurationService cs = ConfigurationService.getInstance();
		LOGGER.info("   Read config!");

		LOGGER.info("   Establishing JDA connection...");
		jda = JDABuilder.createDefault(cs.getDiscordToken())
				.setActivity(Activity.playing("Minecraft"))
				.enableIntents(GatewayIntent.DIRECT_MESSAGES)
				.enableIntents(GatewayIntent.GUILD_MESSAGES)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT)
				.enableIntents(GatewayIntent.GUILD_MEMBERS)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.setChunkingFilter(ChunkingFilter.ALL)
				.addEventListeners(new ChatListener(this))
				.addEventListeners(new CommandListener(this))
				.build();
		try {
			sendSystemEmbed("Minecraft server service started... Please wait!");
			jda.awaitReady();
			LOGGER.info("	Established JDA connection!");
			SelfUser bot = jda.getSelfUser();
			LOGGER.info("		Name: " + bot.getName());
			LOGGER.info("		ID: " + bot.getId());
			LOGGER.info("		Servers: " + jda.getGuilds().size());
		} catch (InterruptedException e) {
			handleException(e);
		}


		LOGGER.info("	Registering game events...");
		PlayerLoginCombinedListener plcl = new PlayerLoginCombinedListener(this);
		ServerPlayConnectionEvents.INIT.register(plcl);
		ServerPlayConnectionEvents.DISCONNECT.register(plcl);
		ServerMessageEvents.CHAT_MESSAGE.register(new ChatMessageListener(this));
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);

		final ServerLifecycleEventsListener slel = new ServerLifecycleEventsListener(this);
		ServerLifecycleEvents.SERVER_STARTING.register(slel);
		ServerLifecycleEvents.SERVER_STARTED.register(slel);
		ServerLifecycleEvents.SERVER_STOPPING.register(slel);
		ServerLifecycleEvents.SERVER_STOPPED.register(slel);

		LOGGER.info("	Registered game events!");

		LOGGER.info("dlaw-fabric initialised in {}ms.", (System.nanoTime() - startTime) / 1000000L);
	}

	public void shutdown(){
		LOGGER.info("dlaw-fabric shutting down!");
		LOGGER.info("	Disconnecting from discord api...");
		jda.shutdownNow();
		LOGGER.info("	Disconnected from discord api!");
		LOGGER.info("	Spark shutting down...");
		Spark.stop();
		LOGGER.info("	Spark stopped...");
	}

	private void onServerStarted(MinecraftServer server) {
		minecraftServer = server;

		new RestService(this).run();
	}

	public void handleException(Exception e) {
		LOGGER.error(e.getClass().getSimpleName() + ": " + e.getMessage());
	}

	public void sendLogEmbed(EmbedBuilder builder) {
		final ConfigurationService cs = ConfigurationService.getInstance();
		new Thread(()->{
			TextChannel channel = jda.getTextChannelById(cs.getDiscordChatChannelID());
			if (channel != null) {
				channel.sendMessageEmbeds(builder.setTimestamp(Instant.now()).build()).queue();
			}
		}).start();
	}

	public void sendSystemEmbed(String text) {
		final ConfigurationService cs = ConfigurationService.getInstance();
		sendLogEmbed(new EmbedBuilder()
				.setColor(cs.getDiscordSystemColor())
				.setDescription(MarkdownUtil.bold(text)));
	}

	public void sendPlayerEmbed(ServerPlayerEntity player, int color, EmbedBuilder builder) {
		if (players.containsKey(player.getUuid())) {
			DiscordModel model = players.get(player.getUuid());

			sendLogEmbed(builder.setColor(color)
					.setAuthor(model.getNickname(), null, model.getAvatar())
					.setThumbnail(getMinecraftAvatarUrl(player))
					.setFooter(model.getId()));
		}
	}

	public void sendPlayerEmbed(ServerPlayerEntity player, EmbedBuilder builder) {
		final ConfigurationService cs = ConfigurationService.getInstance();
		sendPlayerEmbed(player, cs.getDiscordCommandColor(), builder);
	}

	public String getMinecraftAvatarUrl(ServerPlayerEntity player) {
		return "https://visage.surgeplay.com/face/" + PluginUtils.cleanUUID(player.getUuid());
	}
}