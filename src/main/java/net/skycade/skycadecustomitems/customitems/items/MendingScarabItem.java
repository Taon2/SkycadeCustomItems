package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.ItemBuilder;
import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.SkycadeEnchants.enchant.common.EnchantmentManager;
import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
import org.bukkit.Bukkit;
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

public class MendingScarabItem extends CustomItem {

    private static ItemStack mendingScarab;

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
        Bukkit.getLogger().info("running");
    }

    @Override
    public void giveItem(Player p, int amount) {
        for (int i = amount; i > 0; i--) {

            ItemStack item = mendingScarab.clone();

            if (SkycadeCustomItemsPlugin.v18) {
                setMaxStackSize(item, org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item), 1);
                net.minecraft.server.v1_8_R3.ItemStack itemStack = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item);
                net.minecraft.server.v1_8_R3.NBTTagCompound nbt = itemStack.getTag();
                nbt.set("mending", new net.minecraft.server.v1_8_R3.NBTTagInt(ThreadLocalRandom.current().nextInt()));
                itemStack.setTag(nbt);

                InventoryUtil.giveItems(p, org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asBukkitCopy(itemStack));
            }
            else {
                setMaxStackSize(item, org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(item), 1);
                net.minecraft.server.v1_12_R1.ItemStack itemStack = org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(item);
                net.minecraft.server.v1_12_R1.NBTTagCompound nbt = itemStack.getTag();
                if (nbt != null) {
                    nbt.set("mending", new net.minecraft.server.v1_12_R1.NBTTagInt(ThreadLocalRandom.current().nextInt()));
                }
                itemStack.setTag(nbt);

                InventoryUtil.giveItems(p, org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asBukkitCopy(itemStack));
            }
        }
    }

    private boolean matches(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().equals(mendingScarab.getItemMeta().getLore());
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
        return Arrays.asList(
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Fixes any damaged item you wish!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Drag onto the item to use!"
        );
    }
}
