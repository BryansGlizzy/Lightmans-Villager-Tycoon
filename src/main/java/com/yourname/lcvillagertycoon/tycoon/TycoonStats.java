package com.yourname.lcvillagertycoon.tycoon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TycoonStats extends SavedData {

    private static final String DATA_NAME = "lcvillagertycoon_stats";

    private final Map<UUID, TraderStatsEntry> playerStats = new HashMap<>();

    public static TycoonStats get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                TycoonStats::load,
                TycoonStats::new,
                DATA_NAME
        );
    }

    public void recordPurchase(UUID ownerId, long coinsSpent) {
        if (ownerId == null) return;
        TraderStatsEntry entry = playerStats.computeIfAbsent(ownerId, k -> new TraderStatsEntry());
        entry.visits++;
        entry.coinsEarned += coinsSpent;
        setDirty();
    }

    public TraderStatsEntry getStats(UUID ownerId) {
        return playerStats.getOrDefault(ownerId, new TraderStatsEntry());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, TraderStatsEntry> e : playerStats.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Owner", e.getKey());
            entryTag.putInt("Visits", e.getValue().visits);
            entryTag.putLong("CoinsEarned", e.getValue().coinsEarned);
            list.add(entryTag);
        }
        tag.put("PlayerStats", list);
        return tag;
    }

    public static TycoonStats load(CompoundTag tag) {
        TycoonStats stats = new TycoonStats();
        ListTag list = tag.getList("PlayerStats", 10); // 10 = CompoundTag
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            UUID owner = entryTag.getUUID("Owner");
            TraderStatsEntry entry = new TraderStatsEntry();
            entry.visits = entryTag.getInt("Visits");
            entry.coinsEarned = entryTag.getLong("CoinsEarned");
            stats.playerStats.put(owner, entry);
        }
        return stats;
    }

    public static class TraderStatsEntry {
        public int visits = 0;
        public long coinsEarned = 0L;
    }

    /**
     * Calculates a 0 to 5 star reputation rating based on total coins earned.
     * Thresholds are read from TycoonConfig so server admins can tune them.
     */
    public static int getReputationLevel(long coinsEarned) {
        var cfg = com.yourname.lcvillagertycoon.config.TycoonConfig.SERVER;
        if (coinsEarned >= cfg.reputationStar5Threshold.get()) return 5;
        if (coinsEarned >= cfg.reputationStar4Threshold.get()) return 4;
        if (coinsEarned >= cfg.reputationStar3Threshold.get()) return 3;
        if (coinsEarned >= cfg.reputationStar2Threshold.get()) return 2;
        if (coinsEarned >= cfg.reputationStar1Threshold.get()) return 1;
        return 0;
    }

    /**
     * Returns the maximum shopping budget in copper-coin-equivalent
     * for a given reputation tier.
     */
    public static int getMaxBudgetForReputation(int reputationLevel) {
        var cfg = com.yourname.lcvillagertycoon.config.TycoonConfig.SERVER;
        return switch (reputationLevel) {
            case 5 -> (int) Math.min(cfg.reputationStar5Threshold.get(), Integer.MAX_VALUE);
            case 4 -> (int) Math.min(cfg.reputationStar4Threshold.get(), Integer.MAX_VALUE);
            case 3 -> (int) Math.min(cfg.reputationStar3Threshold.get(), Integer.MAX_VALUE);
            case 2 -> (int) Math.min(cfg.reputationStar2Threshold.get(), Integer.MAX_VALUE);
            case 1 -> (int) Math.min(cfg.reputationStar1Threshold.get(), Integer.MAX_VALUE);
            default -> 100;
        };
    }
}
