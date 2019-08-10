package net.skycade.skycadecustomitems.customitems.items.nms;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.items.MendingScarabItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class MendingScarabItem1_8 extends MendingScarabItem {

    @Override
    public void giveItem(Player p, int amount) {
        for (int i = amount; i > 0; i--) {

            ItemStack item = mendingScarab.clone();
            setMaxStackSize(item, org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item), 1);
            net.minecraft.server.v1_8_R3.ItemStack itemStack = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(item);
            net.minecraft.server.v1_8_R3.NBTTagCompound nbt = itemStack.getTag();
            nbt.set("mending", new net.minecraft.server.v1_8_R3.NBTTagInt(ThreadLocalRandom.current().nextInt()));
            itemStack.setTag(nbt);

            InventoryUtil.giveItems(p, org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asBukkitCopy(itemStack));

        }
    }
}
