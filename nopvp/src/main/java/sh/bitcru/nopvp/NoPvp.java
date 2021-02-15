package sh.bitcru.nopvp;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public class NoPvp extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getLogger().log(Level.INFO, "Enabled NoPVP.");
        getServer().getPluginManager().registerEvents(new PvpListener(), this);
        this.getCommand("nopvp").setExecutor(new HealthStat());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        getServer().getLogger().log(Level.INFO, "Disabled NoPVP.");
        super.onDisable();
    }
}

