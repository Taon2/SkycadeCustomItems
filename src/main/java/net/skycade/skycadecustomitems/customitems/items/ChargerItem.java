package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
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
        super("CHARGER", ChatColor.RED + "Charger", Material.FIREBALL);
    }

    @Override
    public void giveItem(Player p, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = getItem();
            if (is == null) return;

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getLore());
            is.setItemMeta(meta);

            setNum(is, getLore(), "Charges", ThreadLocalRandom.current().nextInt(5, 26));

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

        int addedCharges = getCurrentNum(hoveredItem, "Charges");
        int currentCharges = getCurrentNum(clickedItem, "Charges");
        int maxCharges = getMaxNum(clickedItem, "Charges");

        if (currentCharges == maxCharges) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "That item is already fully charged!");
        } else if (maxCharges != -1 && currentCharges + addedCharges > maxCharges) {
            setNum(clickedItem, LegendaryVialItem.getLore(), "Charges", maxCharges);
            event.getWhoClicked().setItemOnCursor(null);
            ((Player) event.getWhoClicked()).updateInventory();
        } else {
            setNum(clickedItem, LegendaryVialItem.getLore(), "Charges", (currentCharges + addedCharges));
            event.getWhoClicked().setItemOnCursor(null);
            ((Player) event.getWhoClicked()).updateInventory();
        }

        event.setCancelled(true);
    }

    private List<String> getLore() {
        int random = ThreadLocalRandom.current().nextInt();
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Charges: " + ChatColor.WHITE + "%current%",
                Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR)),
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Chargers are required to use Legendary Vials!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Click onto a Legendary Vial to charge it!"
        );
    }
}
