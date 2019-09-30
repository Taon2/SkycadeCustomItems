package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.ItemBuilder;
import net.skycade.SkycadeEnchants.enchant.common.EnchantmentManager;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static net.skycade.prisons.util.Messages.REPAIRED;

public abstract class MendingScarabItem extends CustomItem {

    protected static ItemStack mendingScarab;

    static {
        mendingScarab = new ItemBuilder(Material.EMERALD)
                .setDisplayName(ChatColor.GREEN + "Mending Scarab")
                .setLore(getLore())
                .build();

        Map<org.bukkit.enchantments.Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        EnchantmentManager.getInstance().enchantUnsafe(mendingScarab, enchants);
    }

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

        event.getWhoClicked().setItemOnCursor(null);
        clickedItem.setDurability((short) 0);

        ((Player) event.getWhoClicked()).updateInventory();
        event.setCancelled(true);

        REPAIRED.msg(event.getWhoClicked());
    }

    public static List<String> getLore() {
        int random = ThreadLocalRandom.current().nextInt();
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Fixes any damaged item you wish!",
                Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR)),
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Drag onto the item to use!"
        );
    }
}
