package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCombat.data.CombatData;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.skycade.skycadecustomitems.Messages.*;

public abstract class MendingScarabItem extends CustomItem {
    public MendingScarabItem() {
        super("MENDING_SCARAB", ChatColor.GREEN + "Mending Scarab", Material.EMERALD);
    }

    private boolean matches(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(CustomItemManager.MAGIC) &&
                item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(getName());
    }

    @Override
    public void giveItem(Player p, int duration, int amount) {
        giveItem(p, amount);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCurrentItem() == null) return;
        ItemStack hoveredItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();
        if (event.getWhoClicked().getType() != EntityType.PLAYER) return;

        short maxDurability = clickedItem.getType().getMaxDurability();
        if (maxDurability <= 0 || !matches(hoveredItem) || clickedItem.getDurability() <= 0) return;
        if (hoveredItem.getAmount() > 1 || clickedItem.getAmount() > 1)  {
            ONLY_ONE_ALLOWED.msg(event.getWhoClicked());
            return;
        }

        // Disallow if in combat
        CombatData.Combat combat = CombatData.getCombat((Player) event.getWhoClicked());
        if (combat != null && combat.isInCombat()) {
            IN_COMBAT.msg(event.getWhoClicked());
            return;
        }


        event.getWhoClicked().setItemOnCursor(null);
        clickedItem.setDurability((short) 0);

        ((Player) event.getWhoClicked()).updateInventory();
        event.setCancelled(true);

        REPAIRED.msg(event.getWhoClicked());
    }

    public List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        String makeUnstackable = Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR));
        makeUnstackable = makeUnstackable.substring(0, makeUnstackable.length() - 1);
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Fixes any damaged item you wish!",
                makeUnstackable,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Drag onto the item to use!"
        );
    }
}
