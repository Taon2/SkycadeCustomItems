package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChargerItem extends CustomItem implements Listener {
    public ChargerItem() {
        super("CHARGER", ChatColor.RED + "Charger", "Charges", Material.FIREBALL);
    }

    @Override
    public void giveItem(Player p, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = getItem();
            if (is == null) return;

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getRawLore());
            is.setItemMeta(meta);

            setNum(is, is.getItemMeta().getLore(), getCounted(), ThreadLocalRandom.current().nextInt(5, 26));

            InventoryUtil.giveItems(p, is);
        }
    }

    @Override
    public void giveItem(Player p, int duration, int amount) {
        giveItem(p, amount);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasDisplayName() || !event.getItem().getItemMeta().getDisplayName().equals(getName())) return;

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCurrentItem() == null) return;
        ItemStack hoveredItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();
        if (event.getWhoClicked().getType() != EntityType.PLAYER) return;
        if (hoveredItem.getType() == null || hoveredItem.getType() == Material.AIR || !hoveredItem.hasItemMeta() || !hoveredItem.getItemMeta().hasLore() || !hoveredItem.getItemMeta().getLore().contains(CustomItemManager.MAGIC)) return;
        if (hoveredItem.getType() == null || !hoveredItem.hasItemMeta() || !hoveredItem.getItemMeta().hasDisplayName() || !hoveredItem.getItemMeta().getDisplayName().equals(getName())) return;
        if (clickedItem.getType() == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName() || !clickedItem.getItemMeta().getDisplayName().equals(CustomItemManager.getAllCustomItems().get("LEGENDARY_VIAL").getName())) return;
        if (hoveredItem.getAmount() > 1 || clickedItem.getAmount() > 1)  {
            event.getWhoClicked().sendMessage(ChatColor.RED + "This item can only be used with a stack size of 1!");
            return;
        }

        CustomItem item = CustomItemManager.getTypeFromString("LEGENDARY_VIAL");
        if (item == null) return;

        int addedCharges = getCurrentNum(hoveredItem, getCounted());
        int currentCharges = getCurrentNum(clickedItem, getCounted());
        int maxCharges = getMaxNum(clickedItem, getCounted());

        if (currentCharges == maxCharges) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "That item is already fully charged!");
        } else if (maxCharges != -1 && currentCharges + addedCharges > maxCharges) {
            setNum(clickedItem, item.getRawLore(), getCounted(), maxCharges);
            setMaxNum(clickedItem, clickedItem.getItemMeta().getLore(), maxCharges);
            event.getWhoClicked().setItemOnCursor(null);
            ((Player) event.getWhoClicked()).updateInventory();
        } else {
            setNum(clickedItem, item.getRawLore(), getCounted(), (currentCharges + addedCharges));
            setMaxNum(clickedItem, clickedItem.getItemMeta().getLore(), maxCharges);
            event.getWhoClicked().setItemOnCursor(null);
            ((Player) event.getWhoClicked()).updateInventory();
        }

        event.setCancelled(true);
    }

    public List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        String makeUnstackable = Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR));
        makeUnstackable = makeUnstackable.substring(0, makeUnstackable.length() - 1);
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Charges: " + ChatColor.WHITE + "%current%",
                makeUnstackable,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Chargers are required to use Legendary Vials!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Click onto a Legendary Vial to charge it!"
        );
    }
}
