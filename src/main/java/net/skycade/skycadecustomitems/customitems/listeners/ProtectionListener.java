package net.skycade.skycadecustomitems.customitems.listeners;

import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import net.skycade.skycadecustomitems.customitems.items.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ProtectionListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        //Turns items without lore into ones with lore
        CustomItemManager.getAllCustomItems().forEach((s, customItem) -> {
            if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName() && clicked.getItemMeta().getDisplayName().equals(customItem.getName())) {
                if (clicked.getItemMeta().hasLore()) {
                    int count = CustomItem.getCurrentNum(clicked, customItem.getCounted());
                    int max = CustomItem.getMaxNum(clicked, customItem.getCounted());

                    CustomItem.setNum(clicked, customItem.getRawLore(), customItem.getCounted(), count);
                    CustomItem.setMaxNum(clicked, clicked.getItemMeta().getLore(), max);
                }
            }

            if (cursor != null && cursor.hasItemMeta() && cursor.getItemMeta().hasDisplayName() && cursor.getItemMeta().getDisplayName().equals(customItem.getName())) {
                if (cursor.getItemMeta().hasLore()) {
                    int count = CustomItem.getCurrentNum(cursor, customItem.getCounted());
                    int max = CustomItem.getMaxNum(cursor, customItem.getCounted());

                    CustomItem.setNum(cursor, customItem.getRawLore(), customItem.getCounted(), count);
                    CustomItem.setMaxNum(cursor, cursor.getItemMeta().getLore(), max);
                }
            }
        });

        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        InventoryType topType = top.getType();

        ClickType clickType = event.getClick();
        InventoryType.SlotType slotType = event.getSlotType();

        //Stops custom items in off hand
        if (event.getRawSlot() == 45) {
            if (cursor != null && cursor.hasItemMeta() && cursor.getItemMeta().hasLore() && cursor.getItemMeta().getLore().contains(CustomItemManager.MAGIC)) {
                event.setCancelled(true);
                ((Player) event.getWhoClicked()).updateInventory();
                return;
            }
        }

        //Stops custom items in crafting menu
        if ((topType == InventoryType.WORKBENCH || topType == InventoryType.CRAFTING) && slotType == InventoryType.SlotType.CRAFTING && (clickType == ClickType.NUMBER_KEY || clickType == ClickType.LEFT)) {
            if (cursor != null && cursor.hasItemMeta() && cursor.getItemMeta().hasLore() && cursor.getItemMeta().getLore().contains(CustomItemManager.MAGIC)) {
                event.setCancelled(true);
                ((Player) event.getWhoClicked()).updateInventory();
            }
        }
    }

    //Stops placing of custom items
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack placed = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && placed != null && placed.getType().isBlock()){
            if (placed.hasItemMeta() && placed.getItemMeta().hasLore() && placed.getItemMeta().getLore().contains(CustomItemManager.MAGIC))
                event.setCancelled(true);
        }
    }
}
