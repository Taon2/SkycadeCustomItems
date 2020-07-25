package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.*;

public class GodCreeperEggItem extends CustomItem implements Listener {
    public GodCreeperEggItem() {
        super("GOD_CREEPER_EGG", CustomItemManager.MAGIC + ChatColor.GREEN + "God Creeper Egg", Material.MONSTER_EGG, 50);
    }

    private final List<Material> DO_NOT_EXPLODE = Arrays.stream(Material.values()).filter(
            e -> e.name().contains("WATER") ||
                    e.name().contains("LAVA") ||
                    e == Material.AIR ||
                    e == Material.BEDROCK ||
                    e == Material.BARRIER ||
                    e == Material.COMMAND ||
                    e == Material.ENDER_PORTAL ||
                    e == Material.ENDER_PORTAL_FRAME ||
                    e == Material.OBSIDIAN ||
                    e == Material.GOLD_BLOCK ||
                    e == Material.DIAMOND_BLOCK ||
                    e == Material.EMERALD_BLOCK ||
                    e == Material.ANVIL ||
                    e == Material.ENCHANTMENT_TABLE ||
                    e == Material.ENDER_CHEST
    ).collect(Collectors.toList());

    @Override
    public void giveItem(Player p, int amount) {
        giveItem(p, 10, amount);
    }

    @Override
    public void giveItem(Player p, int duration, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = getItem();
            if (is == null) return;

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getRawLore());
            is.setItemMeta(meta);

            InventoryUtil.giveItems(p, is);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {

    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity creeper = event.getEntity();
        if (!(creeper instanceof Creeper)) return;

        String customName = creeper.getCustomName();
        if (customName != null && customName.equals(getName())) {

            Location location = creeper.getLocation().clone().add(0.5, 1.5, 0.5);

            Set<Block> uniqueBlocks = new HashSet<>();

            for (double rho = 0; rho <= 3; rho += 0.5) {
                for (double theta = 0; theta < PI; theta += PI / 8) {
                    for (double phi = 0; phi < 2 * PI; phi += PI / 8) {
                        Vector vector = new Vector(
                                rho * sin(theta) * cos(phi),
                                rho * sin(theta) * sin(phi),
                                rho * cos(theta)
                        );

                        Block block = location.clone().add(vector).getBlock();

                        if (block != null && !DO_NOT_EXPLODE.contains(block.getType()))
                            uniqueBlocks.add(block);
                    }
                }
            }

            List<Block> blocks = event.blockList();
            blocks.clear();

            blocks.addAll(uniqueBlocks);
        }
    }

    public List<String> getRawLore() {
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.GRAY + "Destroys blocks, even while under water!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Right click on a block to summon!"
        );
    }
}
