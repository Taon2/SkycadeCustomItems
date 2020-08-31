package net.skycade.skycadecustomitems.customitems.items.nms;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.SkycadeEnchants.enchant.common.EnchantmentManager;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import net.skycade.skycadecustomitems.customitems.items.CustomItem;
import net.skycade.skycadecustomitems.customitems.items.MendingScarabItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MendingScarabItem1_16 extends MendingScarabItem {
    @Override
    public void giveItem(Player p, int amount) {
        CustomItem customItem = CustomItemManager.getTypeFromString("MENDING_SCARAB");
        if (customItem == null) return;

        for (int i = amount; i > 0; i--) {
            ItemStack item = customItem.getItem().clone();

            ItemMeta meta = item.getItemMeta();
            meta.setLore(getRawLore());
            item.setItemMeta(meta);

            Map<Enchantment, Integer> enchants = new HashMap<>();
            enchants.put(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
            EnchantmentManager.getInstance().enchantUnsafe(item, enchants);

            net.minecraft.server.v1_16_R2.ItemStack itemStack = org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack.asNMSCopy(item);
            net.minecraft.server.v1_16_R2.NBTTagCompound nbt = itemStack.getTag();
            if (nbt != null) {
                nbt.set("mending", new net.minecraft.server.v1_16_R2.NBTTagInt(ThreadLocalRandom.current().nextInt()));
            }
            itemStack.setTag(nbt);

            InventoryUtil.giveItems(p, org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack.asBukkitCopy(itemStack));
        }
    }
}
