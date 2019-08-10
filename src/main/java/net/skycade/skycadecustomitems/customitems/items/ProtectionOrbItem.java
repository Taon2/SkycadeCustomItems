package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.ConfigEntry;
import net.skycade.SkycadeCore.CoreSettings;
import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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

public class ProtectionOrbItem extends CustomItem implements Listener {
    public static final ConfigEntry<Integer> PROTECTION_ORB_DURATION = new ConfigEntry<>("prisons", "protection-orb-duration", 10);

    public ProtectionOrbItem() {
        super("PROTECTION_ORB", ChatColor.DARK_AQUA + "Protection Orb", Material.MAGMA_CREAM);
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

            if (SkycadeCustomItemsPlugin.v18)
                setMaxStackSize(is, org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(is), 1);
            else
                setMaxStackSize(is, org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(is), 1);

            setNum(is, getLore(), "Duration", PROTECTION_ORB_DURATION.getValue());

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
        if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasDisplayName() || !event.getItem().getItemMeta().getDisplayName().equals(getName())) return;

        activeOrbs.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + (PROTECTION_ORB_DURATION.getValue()*60)*1000);
        event.getPlayer().sendMessage(ChatColor.GREEN + "Activated!");
        event.getPlayer().getInventory().removeItem(event.getItem());

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER || event.getDamager().getType() != EntityType.PLAYER) return;

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

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().getType() != EntityType.PLAYER) return;

        if (!event.getMessage().equalsIgnoreCase("/entercombat")) return;

        if (activeOrbs.containsKey(event.getPlayer().getUniqueId())) {
            activeOrbs.remove(event.getPlayer().getUniqueId());
            event.setCancelled(true);
        }
    }


    public static List<String> getLore() {
        return Arrays.asList(
                "",
                ChatColor.AQUA + "Duration: " + ChatColor.WHITE + "%current% Minutes",
                "",
                ChatColor.GRAY + "Prevents you from attacking, and anyone from attacking you!",
                "",
                ChatColor.GRAY + "Use " + ChatColor.WHITE + "/entercombat" + ChatColor.GRAY + " to leave",
                ChatColor.GRAY + "this protective state early.",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Shift + Right Click to activate!"
        );
    }
}
