package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Mine;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.skycade.prisons.util.SpigotUtil.wrap;
import static net.skycade.skycadecustomitems.Messages.ACTIVATED;
import static net.skycade.skycadecustomitems.Messages.ONLY_ONE_ALLOWED;

public class ExperienceBoosterItem extends CustomItem implements Listener {
    public ExperienceBoosterItem() {
        super("EXPERIENCE_BOOSTER", ChatColor.BLUE + "Experience Booster", Material.EYE_OF_ENDER);
    }

    private Map<UUID, Long> activeExpBoost = new HashMap<>();
    private List<UUID> canGainXP = new ArrayList<>();

    @Override
    public void giveItem(Player p, int amount) {
        giveItem(p, 10, amount);
    }

    @Override
    public void giveItem(Player p, int duration, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = getItem();
            if (is == null) return;

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getRawLore());
            is.setItemMeta(meta);

            setNum(is, is.getItemMeta().getLore(), "Duration", duration);


            InventoryUtil.giveItems(p, is);
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        //Specific case for throwing eye of ender
        Player player = event.getPlayer();

        if (player.getItemInHand().getType() == (Material.EYE_OF_ENDER)) event.setCancelled(true);
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (player == null || !player.isSneaking()) return;
        if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasLore() || !event.getItem().getItemMeta().getLore().contains(CustomItemManager.MAGIC)) return;
        if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasDisplayName() || !event.getItem().getItemMeta().getDisplayName().equals(getName())) return;
        if (event.getItem().getAmount() > 1)  {
            ONLY_ONE_ALLOWED.msg(player);
            return;
        }

        activeExpBoost.put(player.getUniqueId(), System.currentTimeMillis() + (getCurrentNum(event.getItem(), "Duration")*60)*1000);
        ACTIVATED.msg(player);
        player.getInventory().removeItem(event.getItem());

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer() == null) return;
        Player player = event.getPlayer();

        int experience = event.getExpToDrop();

        event.setExpToDrop(0);

        //Verifies the player is in a mine where they can gain experience
        for (Mine mine : PrisonMines.getInstance().getMines()) {
            if (mine.isInMine(wrap(player.getLocation()))) {
                //Removes the player from the list if its been more than the allocated amount of time already
                if (activeExpBoost.containsKey(player.getUniqueId()) && activeExpBoost.get(player.getUniqueId()) <= System.currentTimeMillis()) {
                    activeExpBoost.remove(player.getUniqueId());
                }

                //If the player has a boost active, double the experience
                if (activeExpBoost.containsKey(player.getUniqueId())) {
                    experience *= 2;
                }

                canGainXP.add(player.getUniqueId());
                player.giveExp(experience);
            }
        }
    }

    //Disables all ways of gaining Exp except what we give them manually
    @EventHandler (priority = EventPriority.LOW)
    public void onExpChange(PlayerExpChangeEvent event) {
        if (canGainXP.contains(event.getPlayer().getUniqueId())) {
            canGainXP.remove(event.getPlayer().getUniqueId());
            return;
        }

        if (event.getAmount() > 0) {
            event.setAmount(0);
        }
    }

    public List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        String makeUnstackable = Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR));
        makeUnstackable = makeUnstackable.substring(0, makeUnstackable.length() - 1);
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Duration: " + ChatColor.WHITE + "%current% Minutes",
                makeUnstackable,
                ChatColor.GRAY + "Gain double experience while mining!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Shift + Right Click to activate!"
        );
    }
}
