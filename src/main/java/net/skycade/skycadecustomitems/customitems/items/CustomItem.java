package net.skycade.skycadecustomitems.customitems.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class CustomItem implements Listener {

    private final String handle;
    private final String name;
    private final List<String> lore;
    private String counted = "None";
    private ItemStack item;

    CustomItem(String handle, String name, List<String> lore, Material type) {
        this.handle = handle;
        this.lore = lore;
        this.name = name;

        ItemStack is = new ItemStack(type);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        is.setItemMeta(meta);

        item = is.clone();
    }

    CustomItem(String handle, String name, String counted, List<String> lore, Material type) {
        this.handle = handle;
        this.counted = counted;
        this.lore = lore;
        this.name = name;

        ItemStack is = new ItemStack(type);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        is.setItemMeta(meta);

        item = is.clone();
    }

    public void postLoad() {
    }

    public boolean autoLoad() {
        return true;
    }

    public String getHandle() {
        return handle;
    }

    public String getName() {
        return name;
    }

    public String getCounted() {
        return counted;
    }

    ItemStack getItem() {
        if (item == null) {
            item = new ItemStack(Material.WOOD_SWORD);
        }

        return item;
    }

    public abstract void giveItem(Player p, int amount);

    public abstract void giveItem(Player p, int duration, int amount);

    public static int getCurrentNum(ItemStack item, String toGet) {
        int current = 0;

        for (String line : item.getItemMeta().getLore()) {
            if (line.contains(toGet + ": ")) {
                String onlyNum = line.replace(toGet + ": ", "");
                if (onlyNum.contains("/")) {
                    onlyNum = onlyNum.substring(0, onlyNum.indexOf("/"));
                }
                if (onlyNum.contains(" ")) {
                    onlyNum = onlyNum.substring(0, onlyNum.indexOf(" "));
                }
                onlyNum = ChatColor.stripColor(onlyNum);
                current = (Integer.parseInt(onlyNum));
            }
        }
        return current;
    }

    public static void setNum(ItemStack item, List<String> newLore, String toSet, int value) {
        int maxNum = getMaxNum(item, toSet);

        ItemMeta meta = item.getItemMeta();
        if (newLore != null) {
            for (String line : newLore) {
                String editedLine = null;

                if (line != null && line.contains("%current%"))
                    editedLine = line.replace("%current%", Integer.toString(value));

                if (editedLine != null)
                    newLore.set(newLore.indexOf(line), editedLine);
            }
            meta.setLore(newLore);
        }

        item.setItemMeta(meta);

        if (maxNum != -1) {
            setMaxNum(item, item.getItemMeta().getLore(), maxNum);
        }
    }

    public static int getMaxNum(ItemStack item, String toGet) {
        int charges = -1;

        for (String line : item.getItemMeta().getLore()) {
            if (line.contains(toGet + ": ") && line.contains("/") && !line.contains("%max%")) {
                String onlyNum = line.replace(toGet + ": ", "");
                onlyNum = onlyNum.substring(onlyNum.indexOf("/") + 1);
                onlyNum = ChatColor.stripColor(onlyNum);
                charges = (Integer.parseInt(onlyNum));
            }
        }
        return charges;
    }

    public static void setMaxNum(ItemStack item, List<String> newLore, int maxNum) {
        ItemMeta meta = item.getItemMeta();
        if (newLore != null) {
            for (String line : newLore) {
                String editedLine = null;

                if (line != null && line.contains("%max%"))
                    editedLine = line.replace("%max%", Integer.toString(maxNum));

                if (editedLine != null)
                    newLore.set(newLore.indexOf(line), editedLine);
            }
            meta.setLore(newLore);
        }

        item.setItemMeta(meta);

        item.setItemMeta(meta);
    }

    public List<String> getLore() {
        return lore;
    }

    //Unused due to this setting the stack size for all items, not just custom ones.
//    protected void setMaxStackSize(ItemStack original, net.minecraft.server.v1_8_R3.ItemStack nmsItem, int amount){
//        try {
//            Field field = net.minecraft.server.v1_8_R3.Item.class.getDeclaredField("maxStackSize");
//            field.setAccessible(true);
//            field.setInt(nmsItem.getItem(), amount);
//        } catch (Exception ignored) {}
//
//        original.setItemMeta(org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.getItemMeta(nmsItem));
//    }
//
//    protected void setMaxStackSize(ItemStack original, net.minecraft.server.v1_12_R1.ItemStack nmsItem, int amount){
//        try {
//            Field field = net.minecraft.server.v1_12_R1.Item.class.getDeclaredField("maxStackSize");
//            field.setAccessible(true);
//            field.setInt(nmsItem.getItem(), amount);
//        } catch (Exception ignored) {}
//
//        original.setItemMeta(org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.getItemMeta(nmsItem));
//    }
}
