package net.skycade.skycadecustomitems.customitems.listeners;

import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class ProtectionListener1_12 implements Listener {

    //Stops custom items in off hand
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSwapHandsEvent(PlayerSwapHandItemsEvent event) {
        ItemStack swapped = event.getOffHandItem();
        if (swapped != null && swapped.hasItemMeta() && swapped.getItemMeta().hasLore() && swapped.getItemMeta().getLore().contains(CustomItemManager.MAGIC))
            event.setCancelled(true);
    }
}
