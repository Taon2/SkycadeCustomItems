package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ThrowableCreeperEggItem extends CustomItem implements Listener {
    public ThrowableCreeperEggItem() {
        super("THROWABLE_CREEPER_EGG", CustomItemManager.MAGIC + ChatColor.DARK_GREEN + "Throwable Creeper Egg", Material.EGG);
    }

    private HashMap<UUID,String> currentEggs = new HashMap<>();

    private boolean matches(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(CustomItemManager.MAGIC) &&
                item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(getName());
    }

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
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if (event.getItem() != null && matches(event.getItem())) {
                if (!currentEggs.containsKey(event.getPlayer().getUniqueId()))
                    currentEggs.put(event.getPlayer().getUniqueId(), event.getItem().getItemMeta().getDisplayName());
                //else if (currentEggs.containsKey(event.getPlayer().getUniqueId())) {
                //    event.getPlayer().sendMessage(ChatColor.RED + "Wait for your first egg to land!");
                //    event.setCancelled(true);
                //}
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        if (currentEggs.containsKey(event.getPlayer().getUniqueId())) {
            Egg egg = event.getEgg();

            event.setHatching(false);
            egg.setCustomName(currentEggs.get(event.getPlayer().getUniqueId()));
        }
    }

    // Spawns an explosion at the landing location of the egg if it is the correct item
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getType() == EntityType.EGG && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals(currentEggs.get(((Player)event.getEntity().getShooter()).getUniqueId()))) {
            Location loc = event.getEntity().getLocation();
            //todo this line throws null exception dur to tacospigot on factions

            //loc.getWorld().createExplosion()
            Creeper creeper = (Creeper) loc.getWorld().spawnEntity(
                    new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()), EntityType.CREEPER
            );
            creeper.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1000));
            creeper.setExplosionRadius(4);
            creeper.setMaxFuseTicks(1);

            currentEggs.remove(((Player) event.getEntity().getShooter()).getUniqueId());
        }
    }

    public List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        String makeUnstackable = Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR));
        makeUnstackable = makeUnstackable.substring(0, makeUnstackable.length() - 1);
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.GRAY + "Immediately explodes upon landing!",
                makeUnstackable,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Right click to throw!"
        );
    }
}
