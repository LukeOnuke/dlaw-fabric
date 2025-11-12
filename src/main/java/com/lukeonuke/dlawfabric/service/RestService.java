package com.lukeonuke.dlawfabric.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukeonuke.dlawfabric.DlawFabric;
import com.lukeonuke.dlawfabric.model.*;
import com.lukeonuke.dlawfabric.model.backend.DataModel;
import com.lukeonuke.dlawfabric.service.config.ConfigurationService;
import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import spark.Spark;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RestService implements Runnable {

    private final DlawFabric main;
    private final MinecraftServer server;
    private final ConfigurationService cs;
    private final ObjectMapper mapper;

    private static final Pattern UUID_WITHOUT_HYPHENS = Pattern.compile("^[0-9a-fA-F]{32}$");

    public RestService(DlawFabric main) {
        this.main = main;
        this.server = main.getMinecraftServer();
        cs = ConfigurationService.getInstance();
        this.mapper = DataService.getInstance().getMapper();
    }

    @Override
    public void run() {
        // Check if enabled
        if (!cs.getApiEnabled()) return;

        DlawFabric.LOGGER.info("Enabling REST API");
        Spark.port(cs.getApiPort());
        Spark.after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
            response.type("application/json");
        });

        Spark.get("/api/members", ((request, response) -> {
            String id = cs.getDiscordGuildID();
            Guild guild = main.getJda().getGuildById(id);
            if (guild == null) {
                response.status(500);
                generateError("Guild not found");
            }
            return mapper.writeValueAsString(guild.getMembers().stream()
                    .filter(m -> !m.getUser().isBot())
                    .map(m -> MemberModel.builder()
                            .id(m.getId())
                            .name(m.getEffectiveName())
                            .joinedAt(m.getTimeJoined().toLocalDateTime().toString())
                            .build())
                    .collect(Collectors.toList()));
        }));

//        Spark.get("/api/players", ((request, response) -> {
//            UserCache cache = server.getUserCache();
//            if (cache == null) return generateError("User chache not available");
//
//            for(Object rawEntry : cache.load()){
//                EntryAcsessWidener entry = (EntryAcsessWidener) rawEntry;
//            }
//
//        }));

        Spark.get("/api/players/:uuid", (request, response) -> {
            try {
                UUID uuid = parseUUID(request.params("uuid"));
                if(server.getUserCache() == null) return generateError("Game server user cache unavailable!");
                Optional<GameProfile> user = server.getUserCache().getByUuid(uuid);
                if (user.isEmpty()) {
                    response.status(404);
                    return generateError("Player not found");
                }
                GameProfile player = user.get();

                PlayerData p = PlayerData.builder()
                        .id(player.getId().toString())
                        .name(player.getName())
                        .build();
                return mapper.writeValueAsString(p);

            } catch (IllegalArgumentException e) {
                response.status(400);
                return generateError("Invalid UUID format");
            }
        });

        Spark.get("/api/status", (request, response) ->
                mapper.writeValueAsString(ServerStatus.builder()
                        .players(getPlayerStatus())
                        .plugins(getPluginData())
                        .world(getWorldData())
                        .version(server.getVersion())
                        .build()));

        Spark.get("/api/status/players", (request, response) ->
                mapper.writeValueAsString(getPlayerStatus()));

        Spark.get("/api/status/plugins", (request, response) ->
                mapper.writeValueAsString(getPluginData()));

        Spark.get("/api/status/world", (request, response) ->
                mapper.writeValueAsString(getWorldData()));

        Spark.get("/api/user/:uuid", (request, response) -> {
            try {
                DataService service = DataService.getInstance();
                UUID uuid = parseUUID(request.params("uuid"));

                // No data found in cache
                DiscordModel model = main.getPlayers().get(uuid);
                if (model == null) {
                    Guild guild = main.getJda().getGuildById(cs.getDiscordGuildID());
                    if (guild == null) {
                        response.status(404);
                        return generateError("Discord server not found");
                    }
                    DataModel data = service.getData(uuid.toString());
                    Member member = guild.retrieveMemberById(data.getUser().getDiscordId()).complete();
                    if (member == null) {
                        response.status(404);
                        return generateError("Member not found");
                    }
                    return service.getMapper().writeValueAsString(DiscordModel.builder()
                            .id(member.getId())
                            .name(MarkdownSanitizer.sanitize(member.getUser().getEffectiveName()))
                            .nickname(MarkdownSanitizer.sanitize(member.getEffectiveName()))
                            .avatar(member.getEffectiveAvatarUrl())
                            .build());
                }
                return service.getMapper().writeValueAsString(model);
            } catch (IllegalArgumentException e) {
                response.status(400);
                return generateError("Invalid UUID format");
            }
        });
    }

    public static String generateError(String error) throws JsonProcessingException {
        ObjectMapper mapper = DataService.getInstance().getMapper();
        return mapper.writeValueAsString(SparkError.builder()
                .message(error)
                .timestamp(System.currentTimeMillis())
                .build());
    }

    private PlayerStatus getPlayerStatus() {
        HashSet<PlayerData> list = new HashSet<>();
        final PlayerManager playerManager = server.getPlayerManager();
        playerManager.getPlayerList().forEach(player -> {
            list.add(PlayerData.builder()
                    .id(player.getUuid().toString())
                    .name(player.getName().getString())
                    .build()
            );
        });

        return PlayerStatus.builder()
                .max(playerManager.getMaxPlayerCount())
                .online(list.size())
                .list(list)
                .build();
    }

    private List<PluginData> getPluginData() {
        return FabricLoader.getInstance().getAllMods().stream().map(mod -> {
            final ModMetadata metadata = mod.getMetadata();
            return PluginData.builder()
                    .name(metadata.getName())
                    .version(metadata.getVersion().getFriendlyString())
                    .authors(metadata.getAuthors().stream().map(Person::getName).toList())
                    .description(metadata.getDescription())
                    .website("N/A")
                    .build();
        }).toList();
    }

    private WorldData getWorldData() {
        ServerWorld world = server.getOverworld();
        return WorldData.builder()
                .seed(String.valueOf(world.getSeed()))
                .time(world.getTime())
                .type("N/A")
                .build();
    }

    private UUID parseUUID(String uuidStr) {
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException ignored) {
        }

        if (UUID_WITHOUT_HYPHENS.matcher(uuidStr).matches()) {
            String formattedUUID = uuidStr.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"
            );
            return UUID.fromString(formattedUUID);
        }

        throw new IllegalArgumentException("Invalid UUID format");
    }
}
