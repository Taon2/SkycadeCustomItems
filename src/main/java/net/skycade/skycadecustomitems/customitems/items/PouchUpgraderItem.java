package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import net.skycade.skycadecustomitems.customitems.items.pouch.PouchData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

import static net.skycade.prisons.util.Messages.POUCH_UPGRADER_MAX_LEVEL;
import static net.skycade.prisons.util.Messages.POUCH_UPGRADER_SUCCESS;

public class PouchUpgraderItem extends CustomItem {

    public PouchUpgraderItem() {
        super("POUCH_UPGRADER", ChatColor.GREEN + "Pouch Upgrader", Material.BEACON);
    }

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

            InventoryUtil.giveItems(p, is);
        }
    }

    @Override
    public void giveItem(Player p, int level, int amount) {
        giveItem(p, amount);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCurrentItem() == null) return;
        ItemStack hoveredItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();
        PouchData data = PouchData.getData(clickedItem);
        if (event.getWhoClicked().getType() != EntityType.PLAYER) return;
        if (hoveredItem.getType() == null || hoveredItem.getType() == Material.AIR || !hoveredItem.hasItemMeta() || !hoveredItem.getItemMeta().hasLore() || !hoveredItem.getItemMeta().getLore().contains(CustomItemManager.MAGIC)) return;
        if (hoveredItem.getType() == null || !hoveredItem.hasItemMeta() || !hoveredItem.getItemMeta().hasDisplayName() || !hoveredItem.getItemMeta().getDisplayName().equals(getName())) return;
        if (clickedItem.getType() == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName() || data == null) return;

        int currentNum = getCurrentNum(clickedItem, "Tier");

        if (currentNum >= 4) {
            POUCH_UPGRADER_MAX_LEVEL.msg(event.getWhoClicked());
        } else {
            data.setLevel(data.getLevel() + 1);
            setNum(event.getCurrentItem(), PouchItem.getLore(), "Tier", data.getLevel());
            event.getWhoClicked().setItemOnCursor(null);
            ((Player) event.getWhoClicked()).updateInventory();
            event.setCancelled(true);

            POUCH_UPGRADER_SUCCESS.msg(event.getWhoClicked(), "%level%", data.getLevel() + "");
        }

        event.setCancelled(true);
    }

    public static List<String> getLore() {
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Upgrades your Pouch by one tier!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Click onto a Pouch to upgrade it!"
        );
    }
}
