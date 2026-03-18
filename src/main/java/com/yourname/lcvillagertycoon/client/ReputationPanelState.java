package com.yourname.lcvillagertycoon.client;

/**
 * Client-side singleton holding the local player's current reputation data.
 * Updated by TycoonReputationPacket whenever a purchase is recorded or player logs in.
 */
public final class ReputationPanelState {

    private ReputationPanelState() {}

    /** Whether the reputation panel is currently visible on screen. */
    public static boolean panelVisible = false;

    /** The local player's username (used as shop name). */
    public static String playerName = "Trader";

    /** Total copper-equivalent coins earned across all the player's traders. */
    public static long coinsEarned = 0L;

    /** Returns the current star level (0–5), reading thresholds from server config. */
    public static int getStarLevel() {
        var cfg = com.yourname.lcvillagertycoon.config.TycoonConfig.SERVER;
        if (coinsEarned >= cfg.reputationStar5Threshold.get()) return 5;
        if (coinsEarned >= cfg.reputationStar4Threshold.get()) return 4;
        if (coinsEarned >= cfg.reputationStar3Threshold.get()) return 3;
        if (coinsEarned >= cfg.reputationStar2Threshold.get()) return 2;
        if (coinsEarned >= cfg.reputationStar1Threshold.get()) return 1;
        return 0;
    }

    /**
     * Copper coins still needed to reach the next tier.
     * Returns 0 if already at max (5 stars).
     */
    public static long coinsToNextTier() {
        var cfg = com.yourname.lcvillagertycoon.config.TycoonConfig.SERVER;
        long[] thresholds = {
            0L,
            cfg.reputationStar1Threshold.get(),
            cfg.reputationStar2Threshold.get(),
            cfg.reputationStar3Threshold.get(),
            cfg.reputationStar4Threshold.get(),
            cfg.reputationStar5Threshold.get()
        };
        int level = getStarLevel();
        if (level >= 5) return 0;
        return thresholds[level + 1] - coinsEarned;
    }
}
