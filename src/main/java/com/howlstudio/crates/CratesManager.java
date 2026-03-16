package com.howlstudio.crates;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CratesManager {

    private final Path dataDir;
    private final Map<String, CrateType> crateTypes = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Integer>> playerKeys = new ConcurrentHashMap<>(); // uuid → crateType → count
    private final Map<UUID, String> playerNames = new ConcurrentHashMap<>();

    public CratesManager(Path dataDir) {
        this.dataDir = dataDir;
        registerDefaults();
        load();
    }

    private void registerDefaults() {
        // Vote crate — common rewards, low weight spread
        crateTypes.put("vote", new CrateType("vote", "§aVote Crate", List.of(
            new CrateReward("§f500 Coins", CrateReward.Type.COMMAND, "eco give %player% 500", 40, "Common"),
            new CrateReward("§f1,000 Coins", CrateReward.Type.COMMAND, "eco give %player% 1000", 25, "Common"),
            new CrateReward("§b2,500 Coins", CrateReward.Type.COMMAND, "eco give %player% 2500", 15, "Rare"),
            new CrateReward("§b3x Daily Reward", CrateReward.Type.COMMAND, "daily give %player% 3", 12, "Rare"),
            new CrateReward("§5VIP Kit Access", CrateReward.Type.COMMAND, "kit give %player% vip", 6, "Epic"),
            new CrateReward("§6§lLEGENDARY: 10K Coins", CrateReward.Type.COMMAND, "eco give %player% 10000", 2, "Legendary")
        )));

        // Donor crate — better rewards
        crateTypes.put("donor", new CrateType("donor", "§6Donor Crate", List.of(
            new CrateReward("§f2,000 Coins", CrateReward.Type.COMMAND, "eco give %player% 2000", 30, "Common"),
            new CrateReward("§b5,000 Coins", CrateReward.Type.COMMAND, "eco give %player% 5000", 25, "Rare"),
            new CrateReward("§b7-Day VIP Rank", CrateReward.Type.COMMAND, "rank set %player% vip 7d", 15, "Rare"),
            new CrateReward("§5Rare Kit Access", CrateReward.Type.COMMAND, "kit give %player% rare", 15, "Epic"),
            new CrateReward("§5§lEpic: 30-Day VIP", CrateReward.Type.COMMAND, "rank set %player% vip 30d", 10, "Epic"),
            new CrateReward("§6§lLEGENDARY: 25K Coins", CrateReward.Type.COMMAND, "eco give %player% 25000", 5, "Legendary")
        )));

        // Legendary crate — rare drop, high-value rewards
        crateTypes.put("legendary", new CrateType("legendary", "§6§lLegendary Crate", List.of(
            new CrateReward("§b10,000 Coins", CrateReward.Type.COMMAND, "eco give %player% 10000", 25, "Rare"),
            new CrateReward("§b30-Day VIP", CrateReward.Type.COMMAND, "rank set %player% vip 30d", 20, "Rare"),
            new CrateReward("§530-Day MVP", CrateReward.Type.COMMAND, "rank set %player% mvp 30d", 20, "Epic"),
            new CrateReward("§5§lEpic Kit Access", CrateReward.Type.COMMAND, "kit give %player% epic", 15, "Epic"),
            new CrateReward("§6100K Coins", CrateReward.Type.COMMAND, "eco give %player% 100000", 10, "Legendary"),
            new CrateReward("§6§l90-Day MVP+", CrateReward.Type.COMMAND, "rank set %player% mvpplus 90d", 10, "Legendary")
        )));
    }

    // --- Keys ---
    public void giveKey(UUID uuid, String crateType, int amount) {
        playerKeys.computeIfAbsent(uuid, k -> new HashMap<>())
            .merge(crateType.toLowerCase(), amount, Integer::sum);
        save();
    }

    public int getKeys(UUID uuid, String crateType) {
        var map = playerKeys.get(uuid);
        return map == null ? 0 : map.getOrDefault(crateType.toLowerCase(), 0);
    }

    public Map<String, Integer> getAllKeys(UUID uuid) {
        return Collections.unmodifiableMap(playerKeys.getOrDefault(uuid, Collections.emptyMap()));
    }

    // --- Opening ---
    public CrateReward openCrate(UUID uuid, String crateTypeName, PlayerRef playerRef) {
        String key = crateTypeName.toLowerCase();
        CrateType crate = crateTypes.get(key);
        if (crate == null) return null;

        int keys = getKeys(uuid, key);
        if (keys <= 0) return null;

        // Consume key
        playerKeys.get(uuid).merge(key, -1, Integer::sum);
        if (playerKeys.get(uuid).get(key) <= 0) playerKeys.get(uuid).remove(key);

        CrateReward reward = crate.roll();
        if (reward != null) {
            executeReward(reward, uuid, playerRef);
        }
        save();
        return reward;
    }

    private void executeReward(CrateReward reward, UUID uuid, PlayerRef playerRef) {
        String playerName = playerNames.getOrDefault(uuid, "unknown");
        switch (reward.getType()) {
            case COMMAND -> {
                String cmd = reward.getValue().replace("%player%", playerName);
                // Execute as the player (they have the reward permissions)
                try {
                    com.hypixel.hytale.server.core.command.system.CommandManager.get()
                        .handleCommand(playerRef, cmd);
                } catch (Exception ignored) {
                    System.out.println("[Crates] Reward command: " + cmd);
                }
            }
            case MESSAGE -> playerRef.sendMessage(Message.raw(reward.getValue()));
            case CURRENCY -> { /* handled via COMMAND with eco give */ }
        }
    }

    public void registerPlayer(UUID uuid, String name) { playerNames.put(uuid, name); }
    public CrateType getCrateType(String name) { return crateTypes.get(name.toLowerCase()); }
    public Map<String, CrateType> getCrateTypes() { return Collections.unmodifiableMap(crateTypes); }

    @SuppressWarnings("unchecked")
    private void load() {
        Path file = dataDir.resolve("crates-keys.dat");
        if (!Files.exists(file)) return;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            playerKeys.putAll((Map<UUID, Map<String, Integer>>) ois.readObject());
        } catch (Exception ignored) {}
    }

    public void save() {
        try {
            Files.createDirectories(dataDir);
            try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(dataDir.resolve("crates-keys.dat")))) {
                oos.writeObject(new HashMap<>(playerKeys));
            }
        } catch (IOException ignored) {}
    }
}
