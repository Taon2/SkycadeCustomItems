package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.SkycadeEnchants.enchant.common.Enchantment;
import net.skycade.SkycadeEnchants.enchant.common.EnchantmentManager;
import net.skycade.prisons.util.EnchantmentTypes;
import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LegendaryVialItem extends CustomItem implements Listener {
    public LegendaryVialItem() {
        super("LEGENDARY_VIAL", ChatColor.GOLD + "Legendary Enchantment Vial", Material.ENCHANTMENT_TABLE);
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

            setMaxNum(is, is.getItemMeta().getLore(), 100);
            setNum(is, getLore(), "Charges", 0);

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

        if (getCurrentNum(event.getItem(), "Charges") >= getMaxNum(event.getItem(), "Charges")) {
            Enchantment enchantment = (Enchantment) EnchantmentManager.getInstance().getEnchantmentByName(EnchantmentTypes.getLegendary().get(ThreadLocalRandom.current().nextInt(0, EnchantmentTypes.getLegendary().size())));
            ItemStack book = EnchantmentManager.getInstance().getEnchantedBook(enchantment, enchantment.getMinLevel());

            InventoryUtil.giveItems(event.getPlayer(), book);

            event.getPlayer().getInventory().removeItem(event.getItem());
            event.getPlayer().updateInventory();
            event.getPlayer().sendMessage(ChatColor.GREEN + "Success!");
        } else {
            event.getPlayer().sendMessage(ChatColor.RED + "You need " + (getMaxNum(event.getItem(), "Charges") - getCurrentNum(event.getItem(), "Charges") + " more charges to use this item!"));
        }

        event.setCancelled(true);
    }

    public static List<String> getLore() {
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Charges: " + ChatColor.WHITE + "%current%/%max%",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Awards you with one Legendary enchantment book!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Shift + Right Click to use!"
        );
    }
}
