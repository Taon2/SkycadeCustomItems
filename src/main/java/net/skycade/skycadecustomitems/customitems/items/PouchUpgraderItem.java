package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import net.skycade.skycadecustomitems.customitems.items.pouch.PouchData;
import net.skycade.skycadecustomitems.customitems.items.pouch.PouchInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.skycade.prisons.util.Messages.POUCH_UPGRADER_MAX_LEVEL;
import static net.skycade.prisons.util.Messages.POUCH_UPGRADER_SUCCESS;

public class PouchUpgraderItem extends CustomItem {

    public PouchUpgraderItem() {
        super("POUCH_UPGRADER", ChatColor.GREEN + "Pouch Upgrader", "Tier", Material.BEACON);
    }

    @Override
    public void giveItem(Player p, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = getItem();
            if (is == null) return;

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getRawLore());
            is.setItemMeta(meta);

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
        if (hoveredItem.getAmount() > 1 || clickedItem.getAmount() > 1)  {
            event.getWhoClicked().sendMessage(ChatColor.RED + "This item can only be used with a stack size of 1!");
            return;
        }

        int currentNum = getCurrentNum(clickedItem, getCounted());

        if (currentNum >= 4) {
            POUCH_UPGRADER_MAX_LEVEL.msg(event.getWhoClicked());
        } else {
            data.setLevel(data.getLevel() + 1);

            int size = 18 + data.getLevel() * 9;
            ItemStack[] contents = new ItemStack[size];
            System.arraycopy(data.getContents(), 0, contents, 0, Math.min(data.getContents().length, size));

            PouchInventoryHolder holder = new PouchInventoryHolder(data);
            Inventory inventory = Bukkit.createInventory(holder, size, "Pouch");
            holder.setInventory(inventory);
            inventory.setContents(contents);

            PouchData.addInventory(data.getId(), inventory);

            CustomItem item = CustomItemManager.getTypeFromString("POUCH");
            if (item == null) return;

            setNum(event.getCurrentItem(), item.getRawLore(), getCounted(), data.getLevel());
            event.getWhoClicked().setItemOnCursor(null);
            ((Player) event.getWhoClicked()).updateInventory();
            event.setCancelled(true);

            POUCH_UPGRADER_SUCCESS.msg(event.getWhoClicked(), "%level%", data.getLevel() + "");
        }

        event.setCancelled(true);
    }

    public List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        String makeUnstackable = Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR));
        makeUnstackable = makeUnstackable.substring(0, makeUnstackable.length() - 1);
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Upgrades your Pouch by one tier!",
                makeUnstackable,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Click onto a Pouch to upgrade it!"
        );
    }
}
