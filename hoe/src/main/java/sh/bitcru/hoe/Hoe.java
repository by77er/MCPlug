package sh.bitcru.hoe;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import static org.bukkit.Material.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Hoe extends JavaPlugin implements Listener {
    private static final HashSet<Material> validHoes = arrSet(
            new Material[] { WOODEN_HOE, STONE_HOE, IRON_HOE, GOLDEN_HOE, DIAMOND_HOE, NETHERITE_HOE });
    private static final HashSet<Material> validCrops = arrSet(
            new Material[] { SWEET_BERRY_BUSH, BEETROOTS, POTATOES, CARROTS, NETHER_WART, WHEAT, COCOA });

    private static <T> HashSet<T> arrSet(final T[] arr) {
        List<T> l = Arrays.asList(arr);
        return new HashSet<T>(l);
    }

    private Random random = new Random();

    private static Collection<ItemStack> dropConversion(Material yieldType, Material seedType, Block block,
            ItemStack tool) {
        Collection<ItemStack> origDrops = block.getDrops();
        Collection<ItemStack> realDrops = block.getDrops(tool);

        // Number of seeds is based on base game value
        int numSeeds = 0;
        for (ItemStack i : origDrops) {
            if (i.getType() == seedType) {
                numSeeds = Math.max(i.getAmount() - 1, 0);
            }
        }
        // Number of yield is based on
        int numYield = 0;
        for (ItemStack i : realDrops) {
            if (i.getType() == seedType) {
                numYield = Math.max(i.getAmount(), 0);
            }
        }

        // New droplist
        Collection<ItemStack> ret = new ArrayList<>();
        if (numYield > 0) {
            ret.add(new ItemStack(yieldType, numYield));
        }
        if (numSeeds > 0) {
            ret.add(new ItemStack(seedType, numSeeds));
        }

        return ret;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBreakBlock(final BlockBreakEvent e) {
        // Check if in valid blocks
        Block b = e.getBlock();
        Material bMaterial = b.getType();
        if (!validCrops.contains(bMaterial)) {
            return;
        }
        // Check if player is holding a valid farming tool
        Player p = e.getPlayer();
        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        if (!validHoes.contains(itemInHand.getType())) {
            return;
        }
        // Check if the crop is fully grown & if not, don't allow it to break
        Ageable plant = (Ageable) b.getBlockData();
        if (plant.getAge() < plant.getMaximumAge()) {
            e.setCancelled(true);
            return;
        }

        Collection<ItemStack> drops = null;
        switch (bMaterial) {
            case WHEAT:
                drops = dropConversion(WHEAT, WHEAT_SEEDS, b, itemInHand);
                break;
            case BEETROOTS:
                drops = dropConversion(BEETROOT, BEETROOT_SEEDS, b, itemInHand);
                break;
            default:
                Collection<ItemStack> intermediateDrops = b.getDrops(itemInHand);
                ArrayList<ItemStack> subDrops = new ArrayList<>(intermediateDrops);
                int amt = subDrops.get(0).getAmount() - 1;
                if (amt <= 0) {
                    subDrops.remove(0);
                } else {
                    subDrops.get(0).setAmount(amt);
                }
                drops = subDrops;
                break;
        }

        plant.setAge(0);
        b.setBlockData(plant);
        e.setCancelled(true);

        // Set durability w/ unbreaking calculation
        ItemMeta meta = itemInHand.getItemMeta();
        Damageable d = (Damageable) meta;
        int unbreaking = meta.getEnchantLevel(Enchantment.DURABILITY);
        int damage = d.getDamage();
        double damageThreshold = 1d / ((double)unbreaking + 1d);
        double damageChance = random.nextDouble();
        if (damageChance > 1d - damageThreshold) {
            d.setDamage(damage + 1);
            itemInHand.setItemMeta(meta);
        }
        // Drop custom drops
        e.setDropItems(false);
        for (ItemStack drop : drops) {
            b.getWorld().dropItemNaturally(b.getLocation(), drop);
        }
    }
}