package sh.bitcru.nopvp;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class HealthStat implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch(command.getName()) {
            case "nopvp": return noPvp(sender, args);
            default: return false;
        }
    }

    private boolean noPvp(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage("Invalid player.");
            return false;
        }
        if (args[0].equalsIgnoreCase("get")) { // Get player health
            double health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            sender.sendMessage(player.getDisplayName() + "'s health is " + health + ".");
        } else if (args[0].equalsIgnoreCase("set")) { // Set player health
            if (args.length < 3) {
                return false;
            }
            double newHealth = 0.0;
            try {
                newHealth = Double.parseDouble(args[2]);
            } catch (Exception e) {
                return false;
            }
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
            sender.sendMessage(player.getDisplayName() + "'s health is now " + newHealth + ".");
        } else if (args[0].equalsIgnoreCase("reset")) { // Reset player health to 20
            AttributeInstance ai = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            ai.setBaseValue(ai.getDefaultValue());
            sender.sendMessage("Reset " + player.getDisplayName() + "'s health.");
        } else if (args[0].equalsIgnoreCase("sim")) {

        } else {
            return false;
        }
        return true;
    }
}
