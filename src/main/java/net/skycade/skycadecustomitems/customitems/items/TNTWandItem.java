package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.skycade.skycadecustomitems.Messages.TNTWAND_CRAFTED_TNT;

public class TNTWandItem extends CustomItem implements Listener {

    // recipe (if changes are needed later)
    private static final int GUNPOWDER_AMOUNT = 9;
    private static final int SAND_AMOUNT = 0;

    public TNTWandItem() {
        super("TNT_WAND", ChatColor.RED + "TNT Wand", "Uses", Material.STICK);
    }

    @Override
    public void giveItem(Player p, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = getItem();
            if (is == null) return;

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getRawLore());
            is.setItemMeta(meta);

            setMaxNum(is, is.getItemMeta().getLore(), 100);
            setNum(is, is.getItemMeta().getLore(), getCounted(), 100);

            InventoryUtil.giveItems(p, is);
        }
    }

    @Override
    public void giveItem(Player p, int duration, int amount) {
        giveItem(p, amount);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !event.getPlayer().isSneaking()) return;

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().equals(getName()))
            return;

        Block clickedBlock = event.getClickedBlock();
        if (!clickedBlock.getType().equals(Material.CHEST) && !clickedBlock.getType().equals(Material.TRAPPED_CHEST)) return; // not a chest or trapped chest, return

        Inventory chestInv = ((Chest) clickedBlock.getState()).getInventory();

        // cancel event
        event.setCancelled(true);

        /* --------------------- CALCULATIONS --------------------- */

        /* AMOUNT OF GUNPOWDER */
        int availableGunpowder = 0;
        for (ItemStack itemStack : chestInv.getContents()) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            if (itemStack.getType().equals(Material.SULPHUR)) {
                availableGunpowder += itemStack.getAmount();
            }
        }

        /* AMOUNT OF SAND */
        int availableSand = 0;
        for (ItemStack itemStack : chestInv.getContents()) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            if (itemStack.getType().equals(Material.SAND)) {
                availableSand += itemStack.getAmount();
            }
        }

        // gets the highest amount of tnt that can be made with the available resources
        int maxTnt = availableGunpowder / GUNPOWDER_AMOUNT; // we expect integer division. You can't have a partial TNT item

        // DOES THE CHEST HAVE ENOUGH ROOM FOR THE ITEMS?
        int maxAvailableSlots = chestInv.getMaxStackSize() - chestInv.getContents().length;

        if (((maxTnt / 64) + 1) > maxAvailableSlots) {
            // return / cancel ---> NOT ENOUGH ROOM
            return;
        }

        // enough room! Now we will remove the items that are creating our TNT

        // SAND
        removeItems(chestInv, new ItemStack(Material.SAND), maxTnt * SAND_AMOUNT);
        // GUNPOWDER
        removeItems(chestInv, new ItemStack(Material.SULPHUR), maxTnt * GUNPOWDER_AMOUNT);

        // add the TNT to the chest
        for (int i = 0; i < maxTnt; i++) {
            chestInv.addItem(new ItemStack(Material.TNT));
        }
        // done!

        // now decrement the uses by one
        int currentCharges = getCurrentNum(item, getCounted());
        int maxCharges = getMaxNum(item, getCounted());

        setNum(item, getRawLore(), getCounted(), currentCharges - 1);
        setMaxNum(item, item.getItemMeta().getLore(), maxCharges);

        if (getCurrentNum(item, getCounted()) <= 0) {
            event.getPlayer().getInventory().removeItem(item);
            event.getPlayer().updateInventory();
        }

        TNTWAND_CRAFTED_TNT.msg(event.getPlayer(), "%amount%", Integer.toString(maxTnt));
    }

    public List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        String makeUnstackable = Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR));
        makeUnstackable = makeUnstackable.substring(0, makeUnstackable.length() - 1);
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Uses: " + ChatColor.WHITE + "%current%/%max%",
                makeUnstackable,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Craft 9 gunpowder into TNT!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Shift + Click onto a chest to craft your TNT!"
        );
    }

    private void removeItems(Inventory inventory, ItemStack item, int toRemove) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack loopItem = inventory.getItem(i);
            if (loopItem == null || !item.isSimilar(loopItem)) {
                continue;
            }
            if (toRemove <= 0) {
                return;
            }
            if (toRemove < loopItem.getAmount()) {
                loopItem.setAmount(loopItem.getAmount() - toRemove);
                return;
            }
            inventory.clear(i);
            toRemove -= loopItem.getAmount();
        }
    }
}
