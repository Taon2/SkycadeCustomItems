package net.skycade.skycadecustomitems.customitems.items.nms;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.items.MendingScarabItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class MendingScarabItem1_12 extends MendingScarabItem {


    @Override
    public void giveItem(Player p, int amount) {
        for (int i = amount; i > 0; i--) {

            ItemStack item = mendingScarab.clone();

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
