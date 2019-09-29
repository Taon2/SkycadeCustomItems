package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCore.CoreSettings;
import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.SkycadeEnchants.SkycadeEnchantsPlugin;
import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static net.skycade.prisons.util.SpigotUtil.wrap;

public class ExperienceBoosterItem extends CustomItem implements Listener {
    public ExperienceBoosterItem() {
        super("EXPERIENCE_BOOSTER", ChatColor.BLUE + "Experience Booster", Material.EYE_OF_ENDER);
    }

    private String itemTable;
    private Map<Material, Integer> itemMap;
    private Map<UUID, Long> activeExpBoost = new HashMap<>();
    private List<UUID> canGainXP = new ArrayList<>();

    @Override
    public void postLoad() {
        itemTable = SkycadeEnchantsPlugin.getInstance().getConfig().getString("database.prisons-items-table");
        itemMap = getAllItems();
    }

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
            meta.setLore(getLore());
            is.setItemMeta(meta);

            if (SkycadeCustomItemsPlugin.v18)
                setMaxStackSize(is, org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(is), 1);
            else
                setMaxStackSize(is, org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(is), 1);

            setNum(is, getLore(), "Duration", duration);


            InventoryUtil.giveItems(p, is);
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        //Specific case for throwing eye of ender
        if (event.getPlayer().getItemInHand().getType() == (Material.EYE_OF_ENDER)) event.setCancelled(true);
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getPlayer() == null || !event.getPlayer().isSneaking()) return;
        if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasLore() || !event.getItem().getItemMeta().getLore().contains(CustomItemManager.MAGIC)) return;
        if (event.getItem() == null || !event.getItem().hasItemMeta() || !event.getItem().getItemMeta().hasDisplayName() || !event.getItem().getItemMeta().getDisplayName().equals(getName())) return;

        activeExpBoost.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + (getCurrentNum(event.getItem(), "Duration")*60)*1000);
        event.getPlayer().sendMessage(ChatColor.GREEN + "Activated!");
        event.getPlayer().getInventory().removeItem(event.getItem());

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer() == null) return;
        Player player = event.getPlayer();

        if (event.getExpToDrop() > 0) {
            event.setExpToDrop(0);
        }

        //Verifies the player is in a mine where they can gain experience
        for (Mine mine : PrisonMines.getInstance().getMines()) {
            if (PrisonMines.getInstance().getMineManager().getMine(mine.getName()).get().isInMine(wrap(player.getLocation()))) {
                //Removes the player from the list if its been more than the allocated amount of time already
                if (activeExpBoost.containsKey(player.getUniqueId()) && activeExpBoost.get(player.getUniqueId()) <= System.currentTimeMillis()) {
                    activeExpBoost.remove(player.getUniqueId());
                }
                int experience = 0;

                //Calculates the amount of experience that the player should default get per block mined
                if (itemMap.containsKey(event.getBlock().getType())) {
                    experience = itemMap.get(event.getBlock().getType());
                }

                //If the player has a boost active, double the experience
                if (activeExpBoost.containsKey(player.getUniqueId()) && experience > 0) {
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

    private HashMap<Material, Integer> getAllItems() {
        try (Connection connection = CoreSettings.getInstance().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet rs = statement.executeQuery("SELECT * FROM " + itemTable);
                HashMap<Material, Integer> map = new HashMap<>();
                while(rs.next())
                    map.put(Material.valueOf(rs.getString("InitialItem").toUpperCase()), Integer.valueOf(rs.getString("ExpValue")));

                return map;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getLore() {
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Duration: " + ChatColor.WHITE + "%current% Minutes",
                "",
                ChatColor.GRAY + "Gain double experience while mining!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Shift + Right Click to activate!"
        );
    }
}
