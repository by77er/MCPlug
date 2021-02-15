package sh.bitcru.nopvp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PvpListener implements Listener {
    private final double MIN_HEALTH = 4.0;

    // Death punishment
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player killed = e.getEntity();
        Player killer = e.getEntity().getKiller();
        if (killer == null || killer == killed) {
            return;
        }
        double prevHealth = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        killer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 10, 1));
        killer.getWorld().playSound(killer.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10.0f, 0.5f);
        if (prevHealth > MIN_HEALTH) {
            // Kill health reduction
            double newHealth = prevHealth - 4;
            Bukkit.broadcastMessage(killer.getDisplayName() + "'s health has been set to " + (int)(newHealth / 2) + " hearts.");
            killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
        } else {
            Bukkit.broadcastMessage(killer.getDisplayName() + "'s health is already at the minimum value (" + (int)(MIN_HEALTH / 2) + " hearts).");
        }
    }

    // Health restore
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent e) {
        Player eater = e.getPlayer();
        ItemStack item = e.getItem();
        if (item.getType() == Material.GOLDEN_APPLE) {
            double prevHealth = eater.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            if (prevHealth < 20.0) {
                // Golden apple health boost
                double newHealth = prevHealth + 2.0;
                eater.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
                eater.getWorld().playSound(eater.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 10.0f, 0.5f);
                eater.sendMessage("Your health has increased to " + (int)(newHealth / 2) + " hearts.");
            }
        }
    }
}