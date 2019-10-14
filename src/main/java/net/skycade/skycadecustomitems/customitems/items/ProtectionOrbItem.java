package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.ConfigEntry;
import net.skycade.SkycadeCore.CoreSettings;
import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProtectionOrbItem extends CustomItem implements Listener {
    public static final ConfigEntry<Integer> PROTECTION_ORB_DURATION = new ConfigEntry<>("prisons", "protection-orb-duration", 10);

    public ProtectionOrbItem() {
        super("PROTECTION_ORB", ChatColor.DARK_AQUA + "Protection Orb", "Duration", getRawLore(), Material.MAGMA_CREAM);
        CoreSettings.getInstance().registerSetting(PROTECTION_ORB_DURATION);
    }

    private Map<UUID, Long> activeOrbs = new HashMap<>();

    @Override
    public void giveItem(Player p, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = getItem();
            if (is == null) return;

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getLore());
            is.setItemMeta(meta);

            setNum(is, getLore(), getCounted(), PROTECTION_ORB_DURATION.getValue());

            InventoryUtil.giveItems(p, is);
        }
    }

    @Override
    public void giveItem(Player p, int duration, int amount) {
        giveItem(p, amount);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != (Action.RIGHT_CLICK_AIR) && event.getAction() != (Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getPlayer() == null || !event.getPlayer().isSneaking()) return;
        if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasLore() || !event.getItem().getItemMeta().getLore().contains(CustomItemManager.MAGIC)) return;
        if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasDisplayName() || !event.getItem().getItemMeta().getDisplayName().equals(getName())) return;

        activeOrbs.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + (PROTECTION_ORB_DURATION.getValue()*60)*1000);
        event.getPlayer().sendMessage(ChatColor.GREEN + "Activated!");
        event.getPlayer().getInventory().removeItem(event.getItem());

        event.setCancelled(true);
    }

    private static List<EntityType> disabledTypes;

    static {
        disabledTypes = Arrays.asList(
                EntityType.ARROW,
                //EntityType.SPECTRAL_ARROW,
                //EntityType.TIPPED_ARROW,
                EntityType.SPLASH_POTION,
               // EntityType.LINGERING_POTION,
               // EntityType.AREA_EFFECT_CLOUD,
                EntityType.SNOWBALL,
                EntityType.PLAYER
        );
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;

        if (disabledTypes.contains(event.getDamager().getType())) {
            Entity attacker = null;
            switch (event.getCause()) {
                case MAGIC:
                    // Stops all damage potions and stuff
                    event.setCancelled(true);
                    break;
                case ENTITY_ATTACK:
                    attacker = event.getDamager();
                    break;
                case PROJECTILE:
                    // Only certain projectiles are player originating
                    if (event.getDamager().getType() == EntityType.ARROW) {
                        Arrow arrow = (Arrow) event.getDamager();
                        if (arrow.getShooter() instanceof Player){
                            attacker = (Entity) arrow.getShooter();
                        }
                    } else if (event.getDamager().getType() == EntityType.SNOWBALL) {
                        Snowball snowball = (Snowball) event.getDamager();
                        if (snowball.getShooter() instanceof Player){
                            attacker = (Entity) snowball.getShooter();
                        }
                    } else {
                        return;
                    }
                    break;
            }

            if (attacker != null && attacker.getType() != EntityType.PLAYER) return;

            //Removes the player from the list if its been more than the allocated amount of time already
            if (activeOrbs.containsKey(event.getEntity().getUniqueId()) && activeOrbs.get(event.getEntity().getUniqueId()) <= System.currentTimeMillis()) {
                activeOrbs.remove(event.getEntity().getUniqueId());
            }
            if (activeOrbs.containsKey(event.getDamager().getUniqueId()) && activeOrbs.get(event.getDamager().getUniqueId()) <= System.currentTimeMillis()) {
                activeOrbs.remove(event.getDamager().getUniqueId());
            }

            //Stops damage from both parties if one of them has the orb active
            if (activeOrbs.containsKey(event.getEntity().getUniqueId()) || activeOrbs.containsKey(event.getDamager().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().getType() != EntityType.PLAYER) return;

        if (!event.getMessage().equalsIgnoreCase("/entercombat")) return;

        if (activeOrbs.containsKey(event.getPlayer().getUniqueId())) {
            activeOrbs.remove(event.getPlayer().getUniqueId());
            event.setCancelled(true);
        }
    }

    public static List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Duration: " + ChatColor.WHITE + "%current% Minutes",
                Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR)),
                ChatColor.GRAY + "Prevents you from attacking, and anyone from attacking you!",
                "",
                ChatColor.GRAY + "Use " + ChatColor.WHITE + "/entercombat" + ChatColor.GRAY + " to leave",
                ChatColor.GRAY + "this protective state early.",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Shift + Right Click to activate!"
        );
    }
}
