package dev.atlasclient.inject.mixin.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.*;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.*;
import net.minecraft.world.GameMode;
import dev.atlasclient.*;
import dev.atlasclient.event.bus.EventBus;
import dev.atlasclient.event.impl.minecraft.HudStartRenderEvent;
import dev.atlasclient.mod.HudMod;
import dev.atlasclient.protocol.ProtocolVersion;
import dev.atlasclient.themes.ThemeManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static dev.atlasclient.Helper.*;

/**
 * Mixin for the InGameHud class to inject custom HUD rendering behavior.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Final @Shadow private MinecraftClient client;
    @Shadow @Final private PlayerListHud playerListHud;

    @Unique private final List<Long> leftClicks = new ArrayList<>();
    @Unique private final List<Long> rightClicks = new ArrayList<>();
    @Unique private long lastWorldTime = -1;
    @Unique private long lastTpsUpdateTime = 0;
    @Unique private double currentTps = 20.0;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftClient client, CallbackInfo ci) {
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.client == null || this.client.player == null) {
            return;
        }

        ArrayList<String> text = new ArrayList<>();
        // Title with gold color and bold
        text.add("§6§l" + Constants.windowTitle);
        
        // Server info with aqua icon
        text.add("§b✦ §7Server: §f" + ((!Objects.isNull(this.client.getCurrentServerEntry()) && AtlasClient.HUD_MOD.showServerIP) ?
                this.client.getCurrentServerEntry().address : "§8Hidden"));
        
        // Engine info with purple icon
        String engine = Objects.isNull(this.client.getNetworkHandler()) ? "§8N/A" :
                this.client.getNetworkHandler().getBrand();
        text.add("§d✦ §7Engine: §f" + (Objects.isNull(engine) ? "§8N/A" : engine.replaceAll("§[0-9a-fk-or]", "")));
        
        // Performance stats
        int fps = this.client.getCurrentFps();
        long lastPacket = System.currentTimeMillis() - AtlasClient.NETWORK_CONFIGURATION.lastPacket;
        String fpsColor = fps >= 120 ? "§a§l" : fps >= 60 ? "§a" : fps >= 30 ? "§e" : "§c";
        String packetColor = lastPacket <= 50 ? "§a§l" : lastPacket <= 100 ? "§a" : lastPacket <= 500 ? "§e" : "§c";
        text.add("§2✦ §7FPS: " + fpsColor + fps);
        text.add("§3✦ §7Packet: " + packetColor + lastPacket + "ms");
        
        // Protocol info with dark purple icon
        text.add("§5✦ §7Protocol: §f" + ProtocolVersion.getProtocolVersion(AtlasClient.NETWORK_CONFIGURATION.protocolVersion).getVersionIntroducedIn());
        
        // Player count with blue icon
        int playerCount = this.client.getNetworkHandler().getPlayerList().size();
        String playerColor = playerCount > 10000 ? "§c§l" : playerCount > 1000 ? "§e§l" : "§a";
        text.add("§9✦ §7Players: " + playerColor + playerCount);
        
        // Coordinates with yellow icon and gradient colors for values
        String coordinates = String.format("§e✦ §7XYZ: §f§l%.1f §7/ §a§l%.1f §7/ §b§l%.1f",
                this.client.player.getX(),
                this.client.player.getY(),
                this.client.player.getZ());
        text.add(coordinates);
        
        // Gamemode with red icon and mode-specific colors
        String gamemode = getGamemode();
        String gmColor = switch(gamemode) {
            case "Survival" -> "§a";
            case "Creative" -> "§d";
            case "Adventure" -> "§6";
            case "Spectator" -> "§7";
            default -> "§f";
        };
        text.add("§c✦ §7Gamemode: " + gmColor + gamemode);
        
        // Ping with dark aqua icon
        int ping = getPing();
        String pingColor = ping < 50 ? "§a§l" : ping < 100 ? "§a" : ping < 200 ? "§e" : "§c";
        text.add("§3✦ §7Ping: " + pingColor + ping + "ms");
        
        // TPS with dark green icon
        updateTps();
        String tpsColor;
        if (currentTps >= 19.5) tpsColor = "§a§l";
        else if (currentTps >= 18.0) tpsColor = "§a";
        else if (currentTps >= 15.0) tpsColor = "§e";
        else if (currentTps >= 10.0) tpsColor = "§6";
        else tpsColor = "§c";
        text.add("§2✦ §7TPS: " + tpsColor + String.format("%.1f", currentTps));
        
        // CPS with magenta icon
        int leftCps = getLeftCPS();
        int rightCps = getRightCPS();
        String leftCpsColor = leftCps >= 15 ? "§a§l" : leftCps >= 10 ? "§a" : leftCps >= 5 ? "§e" : "§f";
        String rightCpsColor = rightCps >= 15 ? "§a§l" : rightCps >= 10 ? "§a" : rightCps >= 5 ? "§e" : "§f";
        text.add("§d✦ §7CPS: " + leftCpsColor + leftCps + " §8| " + rightCpsColor + rightCps);

        AtlasClient.HUD_MOD.hudElements.clear();
        AtlasClient.HUD_MOD.hudElements.addAll(text);
        EventBus.HUD_START_RENDER_EVENT_CHANNEL.fire(HudStartRenderEvent.INSTANCE);

        int maxWidth = 0;
        for (String s : AtlasClient.HUD_MOD.hudElements) {
            String cleanText = s.replaceAll("§[0-9a-fk-or]", "");
            maxWidth = Math.max(maxWidth, this.client.textRenderer.getWidth(cleanText));
        }
        int lineHeight = this.client.textRenderer.fontHeight + 2;
        
        // Add subtle semi-transparent background for better readability
        int panelHeight = AtlasClient.HUD_MOD.hudElements.size() * lineHeight;
        context.fill(1, 1, maxWidth + 5, panelHeight + 3, 0x60000000);
        
        int i = 0;
        for (String s : AtlasClient.HUD_MOD.hudElements) {
            int textY = 3 + lineHeight * i;
            if (i == 0) {
                String cleanTitle = s.replaceAll("§[0-9a-fk-or]", "");
                int titleWidth = this.client.textRenderer.getWidth(cleanTitle);
                int centeredX = 3 + (maxWidth - titleWidth) / 2;
                renderTextWithEnhancedGlow(context, s, centeredX, textY);
            } else {
                renderText(context, s, 3, textY);
            }
            i++;
        }

        AtlasClient.NOTIFICATION_MANAGER.drawNotifications(context, this.client.textRenderer);
    }

    @Unique private void renderTextWithEnhancedGlow(DrawContext ct, String s, int x, int y) {
        String cleanText = s.replaceAll("&([0-9a-fk-or])", "§$1");
        // Multiple shadow layers for stronger glow effect
        ct.drawText(this.client.textRenderer, cleanText, x + 1, y + 1, 0x80000000, false);
        ct.drawText(this.client.textRenderer, cleanText, x + 2, y + 2, 0x40000000, false);
        ct.drawText(this.client.textRenderer, cleanText, x, y, 0xFFFFFF00, false); // Gold color for title
    }

    @Unique private void renderText(DrawContext ct, String s, int x, int y) {
        String cleanText = s.replaceAll("&([0-9a-fk-or])", "§$1");
        ct.drawText(this.client.textRenderer, cleanText, x, y, -1, false);
    }

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderPlayerList", at = @At("HEAD"), cancellable = true)
    private void renderPlayerList(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        assert this.client.world != null;
        Scoreboard scoreboard = this.client.world.getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);
        if (!this.client.options.playerListKey.isPressed() ||
                (this.client.isInSingleplayer() &&
                        Objects.requireNonNull(this.client.player).networkHandler.getListedPlayerListEntries().size() <= 1 &&
                        scoreboardObjective == null)) {
            this.playerListHud.setVisible(false);
            if (AtlasClient.HUD_MOD.showPlayerList) {
                this.renderTAB(context, context.getScaledWindowWidth(), scoreboard, scoreboardObjective);
            }
        } else {
            this.renderTAB(context, context.getScaledWindowWidth(), scoreboard, scoreboardObjective);
        }
        ci.cancel();
    }

    @Unique
    private void renderTAB(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, @Nullable ScoreboardObjective scoreboardObjective) {
        this.playerListHud.setVisible(true);
        this.playerListHud.render(context, scaledWindowWidth, scoreboard, scoreboardObjective);
    }

    @Unique
    private String getGamemode() {
        if (this.client.interactionManager == null) return "Unknown";
        GameMode gameMode = this.client.interactionManager.getCurrentGameMode();
        if (gameMode == null) return "Unknown";
        return switch (gameMode) {
            case SURVIVAL -> "Survival";
            case CREATIVE -> "Creative";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
    }

    @Unique
    private int getPing() {
        if (this.client.player == null || this.client.getNetworkHandler() == null) return 0;
        var entry = this.client.getNetworkHandler().getPlayerListEntry(this.client.player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }

    @Unique
    private void updateTps() {
        if (this.client.world == null) return;

        long currentTime = System.currentTimeMillis();
        long worldTime = this.client.world.getTime();

        if (lastWorldTime != -1) {
            long timeDiff = currentTime - lastTpsUpdateTime;
            long tickDiff = worldTime - lastWorldTime;

            if (timeDiff >= 1000) {
                // ticks per second = (ticks advanced) / (seconds passed)
                currentTps = (tickDiff * 1000.0) / timeDiff;
                currentTps = Math.min(20.0, Math.max(0.0, currentTps)); // Clamp to [0,20]
                lastTpsUpdateTime = currentTime;
            }
        }

        lastWorldTime = worldTime;
    }

    @Unique private int getLeftCPS() {
        long now = System.currentTimeMillis();
        leftClicks.removeIf(t -> now - t > 1000);
        return leftClicks.size();
    }

    @Unique private int getRightCPS() {
        long now = System.currentTimeMillis();
        rightClicks.removeIf(t -> now - t > 1000);
        return rightClicks.size();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (this.client.currentScreen == null) {
            if (this.client.options.attackKey.wasPressed()) {
                leftClicks.add(System.currentTimeMillis());
            }
            if (this.client.options.useKey.wasPressed()) {
                rightClicks.add(System.currentTimeMillis());
            }
        }
    }
}
