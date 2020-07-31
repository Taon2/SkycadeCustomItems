package net.skycade.skycadecustomitems.customitems.items;

import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
        if (event.getEntity().getType() == EntityType.EGG
                && event.getEntity().getCustomName() != null
                && event.getEntity().getCustomName()
                .equals(currentEggs.get(((Player)event.getEntity().getShooter()).getUniqueId()))) {
            Location loc = event.getEntity().getLocation();

            if (loc.getBlock().getType() != Material.AIR) {
                return;
            }

            //loc.getWorld().createExplosion()
//            Creeper creeper = loc.getWorld().spawn(loc, Creeper.class);
//            creeper.setCustomName(ChatColor.RED + "");
//            creeper.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5, 100));
//            creeper.setCustomNameVisible(false);
//            net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) creeper).getHandle();
//            NBTTagCompound tag = new NBTTagCompound();
//            nmsEntity.c(tag);
//            tag.setInt("ignited", 1);
//            tag.setInt("Fuse", 0);
//            tag.setInt("ExplosionRadius", 2);
//            tag.setString("CustomName", "§5§2§r§2Throwable Creeper Egg");
//            EntityLiving el = (EntityLiving) nmsEntity;
//            el.a(tag);
            Creeper creeper = loc.getWorld().spawn(loc, Creeper.class);
            creeper.setCustomNameVisible(true);
            net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) creeper).getHandle();
            NBTTagCompound tag = new NBTTagCompound();
            nmsEntity.c(tag);
            tag.setInt("ignited", 1);
            tag.setInt("Fuse", 1);
            tag.setInt("ExplosionRadius", 2);
            tag.setString("CustomName", "§5§2§r§2Throwable Creeper Egg");
            EntityLiving el = (EntityLiving) nmsEntity;
            el.a(tag);

            currentEggs.remove(((Player) event.getEntity().getShooter()).getUniqueId());
        }
    }

    public List<String> getRawLore() {
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.GRAY + "Immediately explodes upon landing!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Right click to throw!"
        );
    }
}
