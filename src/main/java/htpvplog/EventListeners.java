package htpvplog;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class EventListeners implements Listener {

    private final HTPVPLog plugin;

    public EventListeners(HTPVPLog plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = null;

        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } 
        else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }

        if (attacker != null && !attacker.equals(victim)) {
            plugin.getCombatManager().tagPlayer(victim);
            plugin.getCombatManager().tagPlayer(attacker);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (plugin.getCombatManager().isInCombat(event.getPlayer())) {
            List<String> blacklist = plugin.getConfig().getStringList("blacklist-commands");
            String message = event.getMessage().toLowerCase(); 
            String command = message.split(" ")[0].substring(1); 

            if (blacklist.contains(command)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ColorUtils.colorize(plugin.getLangMessage("command-blocked")));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCombatManager().isInCombat(player)) {
            if (plugin.getConfig().getBoolean("quit-the-lose")) {
                player.setHealth(0.0);
            }
            plugin.getCombatManager().removePlayerImmediately(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (plugin.getCombatManager().isInCombat(player)) {
            plugin.getCombatManager().untagPlayer(player);
        }
    }
}
