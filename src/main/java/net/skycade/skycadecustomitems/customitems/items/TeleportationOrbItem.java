package net.skycade.skycadecustomitems.customitems.items;

import com.google.common.eventbus.Subscribe;
import net.skycade.SkycadeCore.ConfigEntry;
import net.skycade.SkycadeCore.CoreSettings;
import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tech.mcprison.prison.Prison;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Mine;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.skycade.prisons.util.SpigotUtil.unwrap;
import static net.skycade.prisons.util.SpigotUtil.wrap;

public class TeleportationOrbItem extends CustomItem implements Listener {
    public static final ConfigEntry<Integer> TELEPORTATION_ORB_USES = new ConfigEntry<>("prisons", "teleportation-orb-uses", 10);

    public TeleportationOrbItem() {
        super("TELEPORTATION_ORB", ChatColor.DARK_AQUA + "Teleportation Orb", "Uses", getRawLore(), Material.SLIME_BALL);
        CoreSettings.getInstance().registerSetting(TELEPORTATION_ORB_USES);
    }

    @Override
    public void postLoad() {
        Prison.get().getEventBus().register(this);
    }

    @Override
    public void giveItem(Player p, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = getItem();
            if (is == null) return;

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getLore());
            is.setItemMeta(meta);

            setNum(is, getLore(), getCounted(), TELEPORTATION_ORB_USES.getValue());

            InventoryUtil.giveItems(p, is);
        }
    }

    @Override
    public void giveItem(Player p, int duration, int amount) {
        giveItem(p, amount);
    }

    @Subscribe
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (player == null || !player.isSneaking()) return;
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore() || !item.getItemMeta().getLore().contains(CustomItemManager.MAGIC)) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().equals(getName())) return;

        if (getCurrentNum(item, getCounted()) > 0) {
            for (Mine mine : PrisonMines.getInstance().getMines()) {
                if (mine.isInMine(wrap(player.getLocation()))) {
                    mine.getSpawn().ifPresent(s -> player.teleport(unwrap(s)));

                    setNum(item, getLore(), getCounted(), getCurrentNum(item, getCounted()) - 1);
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Poof!");

                    if (getCurrentNum(item, getCounted()) <= 0) {
                        event.getPlayer().getInventory().removeItem(item);
                    }
                    event.setCancelled(true);
                    return;
                }
            }

            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You are not in a valid mine!");
            return;
        } else {
            event.getPlayer().getInventory().removeItem(item);
        }
    }

    public static List<String> getRawLore() {
        int random = ThreadLocalRandom.current().nextInt();
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Uses: " + ChatColor.WHITE + "%current%",
                Integer.toString(random).replaceAll("", Character.toString(ChatColor.COLOR_CHAR)),
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Teleports you to the beginning of the mine you are in!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Shift + Right Click to use!"
        );
    }
}
