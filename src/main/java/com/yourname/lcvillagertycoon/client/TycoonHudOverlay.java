package com.yourname.lcvillagertycoon.client;

import com.yourname.lcvillagertycoon.config.TycoonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class TycoonHudOverlay {

    private static final List<Notification> notifications = new ArrayList<>();

    // ─── Colours (ARGB) ──────────────────────────────────────────────────────
    /** Same dark overlay used by vanilla tooltips. */
    private static final int PANEL_BG     = 0xF0100010;
    private static final int PANEL_BORDER = 0xFF6400FF; // vanilla tooltip purple
    private static final int COLOR_GOLD   = 0xFFFFD700;
    private static final int COLOR_WHITE  = 0xFFFFFFFF;

    // ─── Star glyphs ─────────────────────────────────────────────────────────
    private static final String FILLED_STAR = "★";
    private static final String EMPTY_STAR  = "☆";

    // ─────────────────────────────────────────────────────────────────────────

    public static void addNotification(int coins, String itemName, int quantity) {
        if (!TycoonConfig.SERVER.enableIncomeHud.get()) return;

        Component text = Component.literal(
                String.format("🪙 +%d coins — a visitor bought %dx %s!", coins, quantity, itemName));
        int duration = TycoonConfig.SERVER.hudNotificationDurationTicks.get();
        notifications.add(new Notification(text, duration));

        if (notifications.size() > 5) {
            notifications.remove(0);
        }
    }

    // ─── Client Tick ─────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();

        // Toggle reputation panel via keybind
        if (mc.player != null) {
            while (TycoonKeyBindings.OPEN_REPUTATION_PANEL.consumeClick()) {
                ReputationPanelState.panelVisible = !ReputationPanelState.panelVisible;
            }
        }

        // Decrement notification timers
        if (!mc.isPaused() && !notifications.isEmpty()) {
            Iterator<Notification> it = notifications.iterator();
            while (it.hasNext()) {
                Notification notif = it.next();
                notif.ticksRemaining--;
                if (notif.ticksRemaining <= 0) it.remove();
            }
        }
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        GuiGraphics g   = event.getGuiGraphics();
        Font        font = mc.font;
        int         sw   = event.getWindow().getGuiScaledWidth();

        // ── Income Notifications (top-right corner) ───────────────────────────
        if (!notifications.isEmpty()) {
            int yOff = 10;
            for (Notification notif : notifications) {
                int alpha = notif.ticksRemaining < 20
                        ? (int) (255.0f * notif.ticksRemaining / 20.0f)
                        : 255;
                int color = (alpha << 24) | 0xFFD700;
                int textW = font.width(notif.text);
                g.drawString(font, notif.text, sw - textW - 10, yOff, color);
                yOff += 12;
            }
        }

        // ── Reputation Panel (centred, near the top) ──────────────────────────
        if (!ReputationPanelState.panelVisible) return;

        int    stars    = ReputationPanelState.getStarLevel();
        long   earned   = ReputationPanelState.coinsEarned;
        long   toNext   = ReputationPanelState.coinsToNextTier();
        String shopName = ReputationPanelState.playerName + "'s Shop";
        String starStr  = FILLED_STAR.repeat(stars) + EMPTY_STAR.repeat(5 - stars);
        String earned_s = "Earned: " + earned + " coins";
        String next_s   = stars < 5 ? "Next tier: " + toNext + " more coins" : "MAX REPUTATION!";

        int lineW  = Math.max(Math.max(font.width(shopName), font.width(starStr)),
                              Math.max(font.width(earned_s),  font.width(next_s)));
        int panelW = lineW + 16;
        int panelH = 4 * (font.lineHeight + 4) + 8;
        int panelX = sw / 2 - panelW / 2;
        int panelY = 40;

        // Vanilla-style border + dark background
        g.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, PANEL_BORDER);
        g.fill(panelX,     panelY,     panelX + panelW,     panelY + panelH,     PANEL_BG);

        // Text lines
        int tx = panelX + 8;
        int ty = panelY + 8;
        g.drawString(font, shopName, tx, ty, COLOR_WHITE, false); ty += font.lineHeight + 4;
        g.drawString(font, starStr,  tx, ty, COLOR_GOLD,  false); ty += font.lineHeight + 4;
        g.drawString(font, earned_s, tx, ty, COLOR_WHITE, false); ty += font.lineHeight + 4;
        g.drawString(font, next_s,   tx, ty, stars < 5 ? COLOR_GOLD : COLOR_WHITE, false);
    }

    // ─── Inner class ─────────────────────────────────────────────────────────

    private static class Notification {
        public final Component text;
        public int ticksRemaining;

        public Notification(Component text, int ticksRemaining) {
            this.text           = text;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
