package net.skycade.skycadecustomitems.customitems.items;

import net.skycade.SkycadeCombat.data.CombatData;
import net.skycade.SkycadeCore.utility.AsyncScheduler;
import net.skycade.SkycadeCore.utility.command.InventoryUtil;
import net.skycade.prisons.util.Pair;
import net.skycade.prisons.util.drop.BlockBreakFullInventoryEvent;
import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import net.skycade.skycadecustomitems.customitems.items.pouch.PouchData;
import net.skycade.skycadecustomitems.customitems.items.pouch.PouchInventoryHolder;
import net.skycade.skycadeshop.SkycadeShopPlugin;
import net.skycade.skycadeshop.impl.skycade.SkycadeShop;
import net.skycade.skycadeshop.impl.skycade.SkycadeShoppable;
import net.skycade.skycadeshop.impl.skycade.event.PreSellTransactionEvent;
import net.skycade.skycadeshop.impl.skycade.event.SellAllEvent;
import net.skycade.skycadeshop.impl.skycade.shoppable.Item;
import net.skycade.skycadeshop.shop.Shoppable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static net.skycade.prisons.util.Messages.TOO_MANY_POUCHES;
import static net.skycade.skycadecustomitems.Messages.IN_COMBAT;

public class PouchItem extends CustomItem {
    public PouchItem() {
        super("POUCH", ChatColor.GOLD + "Pouch", "Tier", Material.INK_SACK);

        Bukkit.getPluginManager().registerEvents(new ShopListener(), SkycadeCustomItemsPlugin.getInstance()); //Don't suicide if shop plugin is not loaded
        Bukkit.getPluginManager().registerEvents(new BlockBreakFullInventoryListener(), SkycadeCustomItemsPlugin.getInstance()); //Don't suicide if prisons plugin is not loaded
    }

    @Override
    public void giveItem(Player p, int num) {
        for (int i = num; i > 0; i--) {
            ItemStack is = PouchData.newPouch();

            ItemMeta meta = is.getItemMeta();
            meta.setLore(getRawLore());
            is.setItemMeta(meta);

            PouchData data = new PouchData(PouchData.getPouchId(is));
            data.setLevel(1);
            setNum(is, is.getItemMeta().getLore(), getCounted(), data.getLevel());

            int size = 18 + data.getLevel() * 9;
            ItemStack[] contents = new ItemStack[size];
            System.arraycopy(data.getContents(), 0, contents, 0, Math.min(data.getContents().length, size));

            PouchInventoryHolder holder = new PouchInventoryHolder(data);
            Inventory inventory = Bukkit.createInventory(holder, size, "Pouch");
            holder.setInventory(inventory);
            inventory.setContents(contents);

            PouchData.addInventory(data.getId(), inventory);

            InventoryUtil.giveItems(p, is);
        }
    }

    @Override
    public void giveItem(Player p, int duration, int amount) {
        giveItem(p, amount);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        ItemStack[] contents = event.getPlayer().getInventory().getContents();
        Arrays.stream(contents).forEach(PouchData::loadData);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        ItemStack[] contents = event.getPlayer().getInventory().getContents();
        AsyncScheduler.runTask(SkycadeCustomItemsPlugin.getInstance(), () -> Arrays.stream(contents).forEach(d -> {
            PouchData data = PouchData.getData(d);
            if (data != null) data.persist();
        }));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        PouchData data = PouchData.getData(event.getItem());
        if (data == null) return;

        if (hasTooManyPouches(event.getPlayer())) {
            TOO_MANY_POUCHES.msg(event.getPlayer());
            return;
        }

        // Disallow if in combat
        CombatData.Combat combat = CombatData.getCombat(event.getPlayer());
        if (combat != null && combat.isInCombat()) {
            IN_COMBAT.msg(event.getPlayer());
            return;
        }

        event.getPlayer().openInventory(PouchData.getInventory(data.getId()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerClickItem(InventoryClickEvent event) {
        PouchData pouchData;

        /* only one of these will be called, so it's okay in this format */
        if (!event.getCursor().getType().equals(Material.AIR)) {
            pouchData = PouchData.getData(event.getCursor());

            if (pouchData == null)
                return;

            // if the player is moving the pouch into itself
            if (event.getClickedInventory().equals(PouchData.getInventory(pouchData.getId()))) {
                Bukkit.getConsoleSender().sendMessage("000");
                // nope! can't do that
                event.setCancelled(true);
            }
        }
        if (event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR)) {
            pouchData = PouchData.getData(event.getCurrentItem());

            if (pouchData == null)
                return;

            // if the player is moving the pouch into itself
            if (event.getInventory().equals(PouchData.getInventory(pouchData.getId()))) {
                Bukkit.getConsoleSender().sendMessage("000");
                // nope! can't do that
                event.setCancelled(true);
            }
        }
        if (event.getClick().equals(ClickType.NUMBER_KEY)) {
            if (event.getInventory().getName().contains("Pouch") || event.getClickedInventory().getName().contains("Pouch")) { // need to substitute with name, no way to check item with this
                event.setCancelled(true); // it will block all items from being used with num key, but that's fine
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getView().getTopInventory();

        if (!(inventory.getHolder() instanceof PouchInventoryHolder)) return;

        PouchData data = ((PouchInventoryHolder) inventory.getHolder()).getPouchData();
        //PouchData.getInventory(data.getId()).setContents(inventory.getContents());
        data.setContents(inventory.getContents());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        PouchData.loadData(event.getCurrentItem());
        PouchData.loadData(event.getCursor());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSheepColoring(PlayerInteractEntityEvent event) {
        boolean isSheep = (event.getRightClicked().getType() != null && event.getRightClicked().getType() == EntityType.SHEEP);
        boolean isDye = (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.INK_SACK);
        PouchData data = PouchData.getData(event.getPlayer().getItemInHand());
        boolean isPouch = isDye && data != null;
        if (isSheep && isPouch) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    public class BlockBreakFullInventoryListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onBlockBreakFullInventory(BlockBreakFullInventoryEvent event) {
            List<ItemStack> remaining = event.getRemaining();

            Player player = event.getPlayer();
            PlayerInventory inventory = player.getInventory();

            if (hasTooManyPouches(event.getPlayer())) {
                TOO_MANY_POUCHES.msg(event.getPlayer());
                return;
            }

            int pouchCount = 0;

            List<Pair<Inventory, Integer>> emptySlots = new ArrayList<>();

            for (ItemStack itemStack : inventory.getContents()) {
                PouchData data = PouchData.getData(itemStack);
                if (data == null) continue;
                ++pouchCount;

                if (pouchCount > 2) break;

                int max = Math.min(18 + (data.getLevel() * 9), 54);

                Inventory pouchInventory = PouchData.getInventory(data.getId());
                ItemStack[] contents = pouchInventory.getContents();
                int k = -1;
                for (ItemStack pouchItem : contents) {
                    ++k;
                    if (k > max - 1) break;

                    if (pouchItem == null) {
                        emptySlots.add(new Pair<>(pouchInventory, k));
                        continue;
                    }

                    Iterator<ItemStack> i = remaining.iterator();

                    while (i.hasNext()) {
                        ItemStack remainingItem = i.next();

                        if (remainingItem.isSimilar(pouchItem)) {
                            int maxAdd = pouchItem.getMaxStackSize() - pouchItem.getAmount();

                            if (remainingItem.getAmount() > maxAdd) {
                                remainingItem.setAmount(remainingItem.getAmount() - maxAdd);
                                pouchItem.setAmount(pouchItem.getMaxStackSize());
                            } else {
                                pouchItem.setAmount(pouchItem.getAmount() + remainingItem.getAmount());
                                i.remove();
                            }
                        }
                    }
                }
                pouchInventory.setContents(contents);
                data.setContents(contents); // not needed? maybe. is it for free? yes.
            }

            for (Iterator<ItemStack> i = remaining.iterator(); i.hasNext(); ) {
                ItemStack item = i.next();
                Pair<Inventory, Integer> pair = emptySlots.stream().findFirst().orElse(null);

                if (pair == null) return;
                ItemStack[] contents = pair.getLeft().getContents();

                contents[pair.getRight()] = item;
                pair.getLeft().setContents(contents);

                emptySlots.remove(0);
                i.remove();
            }
        }
    }

    public class ShopListener implements Listener {

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onSellAll(SellAllEvent event) {
            //If pouches are disabled, don't run
            if (SkycadeCustomItemsPlugin.getInstance().getConfig().getBoolean("disabled-items.POUCH")) return;

            SkycadeShop shop = event.getShop();
            Player player = event.getPlayer();

            for (ItemStack itemStack : player.getInventory().getContents()) {
                PouchData data = PouchData.getData(itemStack);
                if (data == null) continue;

                Inventory pouchInventory = PouchData.getInventory(data.getId());
                ItemStack[] contents = pouchInventory.getContents();
                for (int i = 0; i < contents.length; ++i) {
                    ItemStack pouchItem = contents[i];
                    if (pouchItem == null) continue;

                    SkycadeShoppable shoppable = (SkycadeShoppable) shop.get("item:" + pouchItem.getType().name() + ":" + pouchItem.getDurability())
                            .filter(Shoppable::isSellable).orElse(null);

                    if (shoppable != null) {
                        int amount = pouchItem.getAmount();
                        contents[i] = null;
                        double unitSellPrice = shoppable.getUnitSellPrice();
                        if (shoppable.isIncreasable())
                            unitSellPrice = (1 + ((SkycadeShop) SkycadeShopPlugin.getInstance().getShop()).getIncreaseFor(player)) * unitSellPrice;

                        event.setPreSold(event.getPreSold() + unitSellPrice * amount);
                    }
                }

                pouchInventory.setContents(contents);
                data.setContents(contents);
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onPreSellTransaction(PreSellTransactionEvent event) {
            //If pouches are disabled, don't run
            if (SkycadeCustomItemsPlugin.getInstance().getConfig().getBoolean("disabled-items.POUCH")) return;

            Player player = event.getPlayer();
            SkycadeShoppable shoppable = event.getShoppable();
            if (!(shoppable instanceof Item)) return;

            int initial = event.getAmount();
            int amount = initial;

            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (amount == 0) break;

                if (hasTooManyPouches(event.getPlayer())) {
                    TOO_MANY_POUCHES.msg(event.getPlayer());
                    return;
                }

                PouchData data = PouchData.getData(itemStack);
                if (data == null) continue;

                Inventory pouchInventory = PouchData.getInventory(data.getId());
                ItemStack[] contents = pouchInventory.getContents();
                for (int i = 0; i < contents.length; i++) {
                    if (amount == 0) break;
                    ItemStack pouchItem = contents[i];
                    if (pouchItem == null) continue;
                    if (shoppable.matches(pouchItem.getType().name() + ":" + pouchItem.getDurability())) {
                        if (pouchItem.getAmount() > amount) {
                            pouchItem.setAmount(pouchItem.getAmount() - amount);
                            amount = 0;
                        } else {
                            contents[i] = null;
                            amount -= pouchItem.getAmount();
                        }
                    }
                }

                pouchInventory.setContents(contents);
                data.setContents(contents);
            }

            event.setAmount(amount);
            double unitSellPrice = event.getShoppable().getUnitSellPrice();
            if (shoppable.isIncreasable())
                unitSellPrice = (1 + ((SkycadeShop) SkycadeShopPlugin.getInstance().getShop()).getIncreaseFor(player)) * unitSellPrice;

            event.addCost((initial - amount) * unitSellPrice);
        }
    }

    private boolean hasTooManyPouches(Player p) {
        Inventory inventory = p.getInventory();

        int numPouches = 0;

        for (ItemStack item : inventory.getContents()) {
            PouchData data = PouchData.getData(item);
            if (data == null) continue;

            numPouches++;
        }

        return numPouches >= 3;
    }

    public List<String> getRawLore() {
        return Arrays.asList(
                CustomItemManager.MAGIC,
                ChatColor.AQUA + "Tier: " + ChatColor.WHITE + "%current%",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Stores more loot once your inventory is full!",
                "",
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Shift + Right Click to open!"
        );
    }
}
