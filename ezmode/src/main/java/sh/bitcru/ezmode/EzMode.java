package sh.bitcru.ezmode;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EzMode extends JavaPlugin implements Listener  {    
    private FileConfiguration config = null;
    private HashMap<UUID, WorldData> worldMap = new HashMap<>();
    
    private class WorldData {
        public int num;
        public final Difficulty initialDiff;
        WorldData(int num, Difficulty initialDiff) {
            this.num = num;
            this.initialDiff = initialDiff;
        }
    }

    private void playerEnterWorld(World w) {
        UUID id = w.getUID();
        WorldData data = worldMap.get(id);
        if (data != null) {
            data.num += 1;
        } else {
            // Add to world map
            WorldData wd = new WorldData(1, w.getDifficulty());
            worldMap.put(id, wd);

            // Change difficulty
            Difficulty newDiff = Difficulty.valueOf(config.getString("difficulty").toUpperCase());
            w.setDifficulty(newDiff);

            this.getLogger().info("Set " + w.getName() + " diff to " + newDiff.toString());
        }
    }

    private void playerExitWorld(World w) {
        UUID id = w.getUID();
        WorldData data = worldMap.get(id);
        if (data != null) {
            data.num -= 1;
            if (data.num <= 0) {
                // Remove when last player leaves
                worldMap.remove(id);

                // Reset difficulty
                w.setDifficulty(data.initialDiff);

                this.getLogger().info("Reset " + w.getName() + " diff to " + data.initialDiff.toString());
            }
        }
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.config = this.getConfig();

        // Initialize difficulty based on online players
        List<String> monitoredPlayers = config.getStringList("players");
        for (String name: monitoredPlayers) {
            Player p = this.getServer().getPlayer(name);
            if (p == null || !p.isOnline()) {
                continue;
            }
            playerEnterWorld(p.getWorld());
        }

        this.getServer().getPluginManager().registerEvents(this, this);

        this.getLogger().info("EzMode enabled.");
    }

    @Override
    public void onDisable() {
        this.config = null;

        // Reset difficulty state
        for (UUID id: worldMap.keySet()) {
            WorldData wd = worldMap.remove(id);
            this.getServer().getWorld(id).setDifficulty(wd.initialDiff);
        }

        this.getLogger().info("EzMode disabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String name = e.getPlayer().getName();
        if (!config.getStringList("players").contains(name)) {
            return;
        }
        this.playerEnterWorld(e.getPlayer().getWorld());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        String name = e.getPlayer().getName();
        if (!config.getStringList("players").contains(name)) {
            return;
        }
        this.playerExitWorld(e.getPlayer().getWorld());
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        String name = e.getPlayer().getName();
        if (!config.getStringList("players").contains(name)) {
            return;
        }
        this.playerExitWorld(e.getFrom());
        this.playerEnterWorld(e.getPlayer().getWorld());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

}