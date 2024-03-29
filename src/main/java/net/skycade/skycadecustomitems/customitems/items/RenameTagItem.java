package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

import static net.skycade.skycadecustomitems.Messages.ONLY_ONE_ALLOWED;

public class RenameTagItem extends CustomItem implements Listener {
    public RenameTagItem() {
        super("RENAME_TAG", ChatColor.DARK_PURPLE + "Rename Tag", Material.NAME_TAG);
    }

    private static List<Material> bannedTypes;

    static {
        bannedTypes = Arrays.asList(
                Material.MOB_SPAWNER,
                Material.MONSTER_EGG,
                Material.MONSTER_EGGS,
                Material.HOPPER
        );
    }

    private static Map<UUID, BiConsumer<Player, String>> listenForInput = new HashMap<>();

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
    public void giveItem(Player p, int duration, int amount) {
        giveItem(p, amount);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack hoveredItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getWhoClicked().getType() != EntityType.PLAYER) return;
        if (hoveredItem == null || hoveredItem.getType() == Material.AIR || !hoveredItem.hasItemMeta() || !hoveredItem.getItemMeta().hasLore() || !hoveredItem.getItemMeta().getLore().contains(CustomItemManager.MAGIC)) return;
        if (!hoveredItem.hasItemMeta() || !hoveredItem.getItemMeta().hasDisplayName() || !hoveredItem.getItemMeta().getDisplayName().equals(getName())) return;
        if (clickedItem == null || clickedItem.getType() == Material.AIR || (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore() && clickedItem.getItemMeta().getLore().contains(CustomItemManager.MAGIC))) return;
        if (hoveredItem.getAmount() > 1 || clickedItem.getAmount() > 1)  {
            ONLY_ONE_ALLOWED.msg(event.getWhoClicked());
            return;
        }

        //Stop certain items from being renamed
        if (bannedTypes.contains(clickedItem.getType())) return;

        //Removes tag
        event.setCancelled(true);
        player.setItemOnCursor(null);
        player.updateInventory();
        player.closeInventory();

        player.sendMessage(ChatColor.GREEN + "Type the new name for this item in chat, using '&' for color codes. Type " + ChatColor.RED + "cancel" + ChatColor.GREEN + " to cancel and keep your tag.");

        listenForInput(player.getUniqueId(), (p, v) -> {
            //Gives tag back if canceled
            if (v.equalsIgnoreCase("cancel")) {
                giveItem(player, 1);
                player.updateInventory();
                return;
            }

            for (Map.Entry<String, CustomItem> entry : CustomItemManager.getAllCustomItems().entrySet()) {
                CustomItem customItem = entry.getValue();
                if (v.equalsIgnoreCase(customItem.getName()) || v.length() > 50) {
                    player.sendMessage(ChatColor.RED + "That name is either too long or not allowed. Maximum name length is 30 characters.");
                    giveItem(player, 1);
                    player.updateInventory();
                    return;
                }
            }


            //Renames item if not canceled
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', v));
            event.getCurrentItem().setItemMeta(meta);
            player.updateInventory();
            player.sendMessage(ChatColor.GREEN + "Done!");
        });
    }

    private static void listenForInput(UUID uuid, BiConsumer<Player, String> consumer) {
        listenForInput.put(uuid, consumer);
    }

    @EventHandler (ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        handleInput(event, event.getPlayer(), event.getMessage());
    }

    private void handleInput(Cancellable event, Player player, String message) {
        UUID uuid = player.getUniqueId();
        if (listenForInput.containsKey(uuid)) {
            BiConsumer<Player, String> consumer = listenForInput.get(uuid);
            try {
                event.setCancelled(true);
                consumer.accept(player, message);
            } catch (Exception ignored) {} finally {
                listenForInput.remove(uuid, consumer);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (Map<UUID, ? extends BiConsumer<Player, ? extends Serializable>> map :
                Collections.singletonList(listenForInput)) {
            map.remove(event.getPlayer().getUniqueId());
        }
    }

    public List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        String makeUnstackable = Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR));
        makeUnstackable = makeUnstackable.substring(0, makeUnstackable.length() - 1);
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Allows you to rename an item with colors.",
                makeUnstackable,
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Click onto an item to rename it!"
        );
    }
}