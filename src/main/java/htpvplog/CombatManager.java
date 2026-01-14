package htpvplog;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CombatManager {

    private final HTPVPLog plugin;
    private final Map<UUID, Long> combatTimes = new HashMap<>();
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();
    private int cooldownSeconds;

    public CombatManager(HTPVPLog plugin) {
        this.plugin = plugin;
        this.cooldownSeconds = plugin.getConfig().getInt("cooldown", 15);
        startTask();
    }

    public void tagPlayer(Player player) {
        boolean wasInCombat = isInCombat(player);
        long endTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        combatTimes.put(player.getUniqueId(), endTime);

        if (!wasInCombat) {
            player.sendMessage(ColorUtils.colorize(plugin.getLangMessage("combat-start")));
            
            // BossBar oluştur
            if (plugin.getConfig().getBoolean("settings.bossbar.enabled")) {
                createBossBar(player);
            }
        }
    }

    public void untagPlayer(Player player) {
        if (combatTimes.remove(player.getUniqueId()) != null) {
            player.sendMessage(ColorUtils.colorize(plugin.getLangMessage("combat-end")));
            removeBossBar(player);
        }
    }

    public boolean isInCombat(Player player) {
        if (!combatTimes.containsKey(player.getUniqueId())) return false;
        return combatTimes.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public long getRemainingTime(Player player) {
        if (!isInCombat(player)) return 0;
        return (combatTimes.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000L;
    }
    
    // Milisaniye hassasiyeti için (Bossbar progress bar doluluğu için)
    private double getProgress(Player player) {
        if (!isInCombat(player)) return 0.0;
        long remainingMillis = combatTimes.get(player.getUniqueId()) - System.currentTimeMillis();
        double totalMillis = cooldownSeconds * 1000.0;
        double progress = remainingMillis / totalMillis;
        return Math.max(0.0, Math.min(1.0, progress));
    }

    private void createBossBar(Player player) {
        String colorStr = plugin.getConfig().getString("settings.bossbar.color", "RED");
        String styleStr = plugin.getConfig().getString("settings.bossbar.style", "SOLID");
        
        BarColor color;
        BarStyle style;
        
        try { color = BarColor.valueOf(colorStr); } catch (Exception e) { color = BarColor.RED; }
        try { style = BarStyle.valueOf(styleStr); } catch (Exception e) { style = BarStyle.SOLID; }

        BossBar bossBar = Bukkit.createBossBar(
                ColorUtils.colorize(plugin.getLangMessage("bossbar-title").replace("{time}", String.valueOf(cooldownSeconds))),
                color,
                style
        );
        bossBar.addPlayer(player);
        activeBossBars.put(player.getUniqueId(), bossBar);
    }

    private void removeBossBar(Player player) {
        BossBar bossBar = activeBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void removePlayerImmediately(Player player) {
        combatTimes.remove(player.getUniqueId());
        removeBossBar(player);
    }

    public void cleanup() {
        for (BossBar bar : activeBossBars.values()) {
            bar.removeAll();
        }
        activeBossBars.clear();
        combatTimes.clear();
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, Long>> iterator = combatTimes.entrySet().iterator();
                
                while (iterator.hasNext()) {
                    Map.Entry<UUID, Long> entry = iterator.next();
                    UUID uuid = entry.getKey();
                    long endTime = entry.getValue();
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null) {
                        iterator.remove();
                        activeBossBars.remove(uuid); // Player yoksa bardan da sil
                        continue;
                    }

                    if (System.currentTimeMillis() > endTime) {
                        // Süre bitti
                        iterator.remove();
                        player.sendMessage(ColorUtils.colorize(plugin.getLangMessage("combat-end")));
                        removeBossBar(player);
                    } else {
                        // Savaş devam ediyor, güncellemeleri yap
                        long remainingSeconds = (endTime - System.currentTimeMillis()) / 1000L;
                        String timeStr = String.valueOf(remainingSeconds + 1); // 0 göstermemek için +1 opsiyonel ama 15..1 sayar

                        // ActionBar Güncelle
                        if (plugin.getConfig().getBoolean("settings.actionbar.enabled")) {
                            String msg = plugin.getLangMessage("actionbar-message").replace("{time}", timeStr);
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ColorUtils.colorize(msg)));
                        }

                        // BossBar Güncelle
                        if (plugin.getConfig().getBoolean("settings.bossbar.enabled") && activeBossBars.containsKey(uuid)) {
                            BossBar bar = activeBossBars.get(uuid);
                            String title = plugin.getLangMessage("bossbar-title").replace("{time}", timeStr);
                            bar.setTitle(ColorUtils.colorize(title));
                            bar.setProgress(getProgress(player));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Her 2 tickte bir güncelle (Akıcı olması için)
    }
}
