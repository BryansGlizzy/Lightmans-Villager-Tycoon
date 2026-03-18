package com.yourname.lcvillagertycoon.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Server-side config for LCVillagerTycoon.
 * Generated at: config/lcvillagertycoon-server.toml
 *
 * All values here are fully configurable by server operators.
 */
public final class TycoonConfig {

    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = specPair.getLeft();
        SERVER_SPEC = specPair.getRight();
    }

    public static class Server {

        // ─── Spawning ────────────────────────────────────────────────────────────

        /** How many natural shopper spawns to attempt per player per world-day tick cycle. */
        public final ForgeConfigSpec.IntValue shopperSpawnAttemptsPerDay;

        /** Chance (0–100) that a given spawn attempt succeeds. */
        public final ForgeConfigSpec.IntValue shopperSpawnChancePercent;

        /** Maximum number of ShopperVillagers that can exist simultaneously per loaded chunk. */
        public final ForgeConfigSpec.IntValue maxShoppersPerChunk;

        /** Maximum total ShopperVillagers allowed in the entire loaded world at once. */
        public final ForgeConfigSpec.IntValue maxShoppersGlobal;

        // ─── Shopping Behaviour ──────────────────────────────────────────────────

        /**
         * How often (in ticks) each ShopperVillager re-evaluates whether to go shopping.
         * 20 ticks = 1 second. Default 1200 = 60 seconds.
         */
        public final ForgeConfigSpec.IntValue shopDecisionIntervalTicks;

        /**
         * Radius in blocks within which a ShopperVillager will search for nearby traders.
         */
        public final ForgeConfigSpec.IntValue traderSearchRadiusBlocks;

        /**
         * Maximum number of distinct trades a ShopperVillager will attempt per shopping trip
         * before wandering away. Acts as a "cart limit" per visit.
         */
        public final ForgeConfigSpec.IntValue maxTradesPerVisit;

        /**
         * Ticks the NPC will pause/stand at the trader block before executing each trade.
         * Gives a feel of "browsing". 40 ticks = 2 seconds.
         */
        public final ForgeConfigSpec.IntValue browsingPauseTicks;

        // ─── Currency ────────────────────────────────────────────────────────────

        /**
         * Whether the NPC's wallet refreshes every in-game day (true) or is single-use (false).
         */
        public final ForgeConfigSpec.BooleanValue refreshBudgetDaily;

        /**
         * If refreshBudgetDaily is true, the amount of coins added per in-game day refresh.
         * Set to -1 to fully restore budget to the original spawned amount.
         */
        public final ForgeConfigSpec.IntValue dailyBudgetRefreshAmount;

        // ─── Trader Filtering ────────────────────────────────────────────────────

        /**
         * If true, ShopperVillagers will only visit traders that have been "opened for business"
         * (i.e. have at least one valid trade configured with non-zero stock).
         */
        public final ForgeConfigSpec.BooleanValue requireStockedTrader;

        /**
         * Minimum number of items in stock a trade slot must have for the NPC to consider it.
         */
        public final ForgeConfigSpec.IntValue minStockRequired;

        // ─── HUD / Client ────────────────────────────────────────────────────────

        /**
         * Whether the income notification overlay is shown to the trader owner.
         * Client-side preference but stored in server config for consistency across sessions.
         */
        public final ForgeConfigSpec.BooleanValue enableIncomeHud;

        /**
         * How long (in ticks) to display each income notification on screen. 100 ticks = 5 sec.
         */
        public final ForgeConfigSpec.IntValue hudNotificationDurationTicks;

        // ─── Reputation Tiers ────────────────────────────────────────────────────

        /** Copper-coin threshold to reach 1 star. Default: 1,000 (≈ 1 Emerald coin). */
        public final ForgeConfigSpec.LongValue reputationStar1Threshold;
        /** Min copper budget a shopper spawns with at 1 ★. */
        public final ForgeConfigSpec.IntValue reputationStar1BudgetMin;
        /** Max copper budget a shopper spawns with at 1 ★. */
        public final ForgeConfigSpec.IntValue reputationStar1BudgetMax;

        /** Copper-coin threshold to reach 2 stars. Default: 5,000. */
        public final ForgeConfigSpec.LongValue reputationStar2Threshold;
        /** Min copper budget a shopper spawns with at 2 ★. */
        public final ForgeConfigSpec.IntValue reputationStar2BudgetMin;
        /** Max copper budget a shopper spawns with at 2 ★. */
        public final ForgeConfigSpec.IntValue reputationStar2BudgetMax;

        /** Copper-coin threshold to reach 3 stars. Default: 10,000 (≈ 1 Diamond coin). */
        public final ForgeConfigSpec.LongValue reputationStar3Threshold;
        /** Min copper budget a shopper spawns with at 3 ★. */
        public final ForgeConfigSpec.IntValue reputationStar3BudgetMin;
        /** Max copper budget a shopper spawns with at 3 ★. */
        public final ForgeConfigSpec.IntValue reputationStar3BudgetMax;

        /** Copper-coin threshold to reach 4 stars. Default: 50,000. */
        public final ForgeConfigSpec.LongValue reputationStar4Threshold;
        /** Min copper budget a shopper spawns with at 4 ★. */
        public final ForgeConfigSpec.IntValue reputationStar4BudgetMin;
        /** Max copper budget a shopper spawns with at 4 ★. */
        public final ForgeConfigSpec.IntValue reputationStar4BudgetMax;

        /** Copper-coin threshold to reach 5 stars (max). Default: 100,000 (≈ 1 Netherite coin). */
        public final ForgeConfigSpec.LongValue reputationStar5Threshold;
        /** Min copper budget a shopper spawns with at 5 ★. */
        public final ForgeConfigSpec.IntValue reputationStar5BudgetMin;
        /** Max copper budget a shopper spawns with at 5 ★. */
        public final ForgeConfigSpec.IntValue reputationStar5BudgetMax;

        /** Min copper budget a shopper spawns with at 0 ★ (no reputation). */
        public final ForgeConfigSpec.IntValue reputationStar0BudgetMin;
        /** Max copper budget a shopper spawns with at 0 ★ (no reputation). */
        public final ForgeConfigSpec.IntValue reputationStar0BudgetMax;

        // ─── Despawn ─────────────────────────────────────────────────────────────

        /**
         * Ticks of inactivity (no trader found nearby) after which a ShopperVillager naturally despawns.
         * Set to -1 to disable despawning (not recommended for performance).
         */
        public final ForgeConfigSpec.IntValue idleDespawnTicks;

        /**
         * Ticks a ShopperVillager lingers after completing a purchase before despawning.
         * Keeps the world from being cluttered with post-shopping NPCs.
         * Default 200 = 10 seconds. Set to -1 to disable (NPC stays until idle timeout).
         */
        public final ForgeConfigSpec.IntValue postPurchaseDespawnTicks;

        Server(ForgeConfigSpec.Builder builder) {

            builder.comment("LCVillagerTycoon Server Configuration")
                   .comment("All values are hot-reloaded from config/lcvillagertycoon-server.toml")
                   .push("spawning");

            shopperSpawnAttemptsPerDay = builder
                .comment("Spawn attempts per player per in-game day cycle.",
                         "Higher values = more shoppers but more CPU usage at dawn each day.",
                         "Range: 1–20")
                .defineInRange("shopperSpawnAttemptsPerDay", 10, 1, 20);

            shopperSpawnChancePercent = builder
                .comment("Percentage chance (1–100) that each spawn attempt actually places an NPC.",
                         "Multiply with shopperSpawnAttemptsPerDay to tune effective spawn rate.")
                .defineInRange("shopperSpawnChancePercent", 60, 1, 100);

            maxShoppersPerChunk = builder
                .comment("Hard cap on simultaneous ShopperVillagers per loaded chunk.")
                .defineInRange("maxShoppersPerChunk", 10, 1, 20);

            maxShoppersGlobal = builder
                .comment("Absolute cap on ShopperVillagers in the entire loaded world.",
                         "Prevents runaway population on busy servers.")
                .defineInRange("maxShoppersGlobal", 60, 1, 500);

            builder.pop().push("shopping_behaviour");

            shopDecisionIntervalTicks = builder
                .comment("Ticks between each NPC shopping decision check. 20 ticks = 1 second.",
                         "Default 1200 = checks every 60 seconds.")
                .defineInRange("shopDecisionIntervalTicks", 600, 100, 72000);

            traderSearchRadiusBlocks = builder
                .comment("Block radius within which an NPC will detect and walk to a trader.")
                .defineInRange("traderSearchRadiusBlocks", 64, 8, 256);

            maxTradesPerVisit = builder
                .comment("Maximum trades an NPC executes in a single visit before leaving.",
                         "Think of this as the NPC's 'shopping cart' size.")
                .defineInRange("maxTradesPerVisit", 3, 1, 16);

            browsingPauseTicks = builder
                .comment("Ticks the NPC stands at a trader before each trade. Adds realism.",
                         "40 ticks = 2 seconds.")
                .defineInRange("browsingPauseTicks", 40, 0, 600);

            builder.pop().push("currency");

            refreshBudgetDaily = builder
                .comment("If true, the NPC's wallet replenishes each in-game day.",
                         "If false, their budget is fixed from spawn until death/despawn.")
                .define("refreshBudgetDaily", false);

            dailyBudgetRefreshAmount = builder
                .comment("Copper coins added to NPC wallet each in-game day refresh.",
                         "Set to -1 to fully restore budget to original spawned amount.")
                .defineInRange("dailyBudgetRefreshAmount", 200, -1, 100000);

            builder.pop().push("trader_filtering");

            requireStockedTrader = builder
                .comment("If true, NPCs ignore traders that have no item stock available.")
                .define("requireStockedTrader", true);

            minStockRequired = builder
                .comment("Minimum items in a trade slot for the NPC to consider purchasing it.",
                         "Only applies when requireStockedTrader is true.")
                .defineInRange("minStockRequired", 1, 1, 64);

            builder.pop().push("hud");

            enableIncomeHud = builder
                .comment("Show the on-screen income notification when an NPC buys from your trader.")
                .define("enableIncomeHud", true);

            hudNotificationDurationTicks = builder
                .comment("Ticks to display each income notification. 100 ticks = 5 seconds.")
                .defineInRange("hudNotificationDurationTicks", 100, 20, 600);

            builder.pop().push("reputation");

            // ── 0 ★ (unrated) — default tier ──────────────────────────────────────
            reputationStar0BudgetMin = builder
                .comment("Min copper budget for a shopper spawning near a 0-star trader.")
                .defineInRange("star0BudgetMin", 5, 1, 1_000_000);
            reputationStar0BudgetMax = builder
                .comment("Max copper budget for a shopper spawning near a 0-star trader.")
                .defineInRange("star0BudgetMax", 50, 1, 1_000_000);

            // ── 1 ★ ───────────────────────────────────────────────────────────────
            reputationStar1Threshold = builder
                .comment("Copper coins earned to reach 1 ★.  Default 1,000 ≈ 1 Emerald coin.")
                .defineInRange("star1Threshold", 1500L, 1L, Long.MAX_VALUE);
            reputationStar1BudgetMin = builder
                .comment("Min copper budget for a shopper spawning near a 1-star trader.")
                .defineInRange("star1BudgetMin", 10, 1, 1_000_000);
            reputationStar1BudgetMax = builder
                .comment("Max copper budget for a shopper spawning near a 1-star trader.")
                .defineInRange("star1BudgetMax", 300, 1, 1_000_000);

            // ── 2 ★ ───────────────────────────────────────────────────────────────
            reputationStar2Threshold = builder
                .comment("Copper coins earned to reach 2 ★.  Default 5,000.")
                .defineInRange("star2Threshold", 6000L, 1L, Long.MAX_VALUE);
            reputationStar2BudgetMin = builder
                .comment("Min copper budget for a shopper spawning near a 2-star trader.")
                .defineInRange("star2BudgetMin", 10, 1, 1_000_000);
            reputationStar2BudgetMax = builder
                .comment("Max copper budget for a shopper spawning near a 2-star trader.")
                .defineInRange("star2BudgetMax", 600, 1, 1_000_000);

            // ── 3 ★ ───────────────────────────────────────────────────────────────
            reputationStar3Threshold = builder
                .comment("Copper coins earned to reach 3 ★.  Default 10,000 ≈ 1 Diamond coin.")
                .defineInRange("star3Threshold", 12000L, 1L, Long.MAX_VALUE);
            reputationStar3BudgetMin = builder
                .comment("Min copper budget for a shopper spawning near a 3-star trader.")
                .defineInRange("star3BudgetMin", 10, 1, 1_000_000);
            reputationStar3BudgetMax = builder
                .comment("Max copper budget for a shopper spawning near a 3-star trader.")
                .defineInRange("star3BudgetMax", 1000, 1, 1_000_000);

            // ── 4 ★ ───────────────────────────────────────────────────────────────
            reputationStar4Threshold = builder
                .comment("Copper coins earned to reach 4 ★.  Default 50,000.")
                .defineInRange("star4Threshold", 80000L, 1L, Long.MAX_VALUE);
            reputationStar4BudgetMin = builder
                .comment("Min copper budget for a shopper spawning near a 4-star trader.")
                .defineInRange("star4BudgetMin", 10, 1, 1_000_000);
            reputationStar4BudgetMax = builder
                .comment("Max copper budget for a shopper spawning near a 4-star trader.")
                .defineInRange("star4BudgetMax", 1800, 1, 1_000_000);

            // ── 5 ★ ───────────────────────────────────────────────────────────────
            reputationStar5Threshold = builder
                .comment("Copper coins earned to reach 5 ★ (max).  Default 100,000 ≈ 1 Netherite coin.")
                .defineInRange("star5Threshold", 150000L, 1L, Long.MAX_VALUE);
            reputationStar5BudgetMin = builder
                .comment("Min copper budget for a shopper spawning near a 5-star trader.")
                .defineInRange("star5BudgetMin", 100, 1, 1_000_000);
            reputationStar5BudgetMax = builder
                .comment("Max copper budget for a shopper spawning near a 5-star trader.")
                .defineInRange("star5BudgetMax", 2500, 1, 1_000_000);

            builder.pop().push("despawn");

            idleDespawnTicks = builder
                .comment("Ticks of idling (no nearby trader found) before the NPC despawns.",
                         "Set to -1 to disable automatic despawning.",
                         "Default 6000 = 5 minutes.")
                .defineInRange("idleDespawnTicks", 6000, -1, 288000);

            postPurchaseDespawnTicks = builder
                .comment("Ticks the NPC lingers after completing a purchase before despawning.",
                         "Keeps the world from being cluttered with post-shopping NPCs.",
                         "Set to -1 to disable (NPC stays until idle timeout).",
                         "Default 200 = 10 seconds.")
                .defineInRange("postPurchaseDespawnTicks", 800, -1, 24000);

            builder.pop();
        }
    }

    private TycoonConfig() {}
}
