package net.skycade.skycadecustomitems.customitems.items.pouch;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.skycade.SkycadeCore.CoreSettings;
import net.skycade.SkycadeCore.utility.AsyncScheduler;
import net.skycade.SkycadeCore.utility.CoreUtil;
import net.skycade.SkycadeCore.utility.ItemBuilder;
import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class PouchData {
    private static LoadingCache<Integer, PouchData> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Integer, PouchData>() {
                @Override
                public PouchData load(@Nonnull Integer id) {
                    try (Connection connection = CoreSettings.getInstance().getConnection()) {
                        String sql = "SELECT * FROM skycade_pouches WHERE id = ? AND instance = ?";
                        try (PreparedStatement statement = connection.prepareStatement(sql)) {

                            statement.setInt(1, id);
                            statement.setString(2, CoreSettings.getInstance().getThisInstance());

                            ResultSet resultSet = statement.executeQuery();
                            if (resultSet.next()) {
                                PouchData pouch = new PouchData(id);
                                pouch.setLevel(resultSet.getInt("level"));
                                pouch.setContents(CoreUtil.itemStackArrayFromBase64(resultSet.getString("contents")));
                                return pouch;
                            }

                            return new PouchData(id);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

    public static boolean isPresent(int id) {
        return cache.getIfPresent(id) != null;
    }

    public static ItemStack newPouch() {
        int id = ThreadLocalRandom.current().nextInt();
        ItemStack item = getItem(id);
        AsyncScheduler.runTask(SkycadeCustomItemsPlugin.getInstance(), () -> cache.getUnchecked(id).persist());
        return item;
    }

    public static PouchData getData(ItemStack item) {
        Integer id;
        if ((id = getPouchId(item)) == null) return null;

        return cache.getIfPresent(id);
    }

    public static void loadData(ItemStack item) {
        Integer id;
        if ((id = getPouchId(item)) == null) return;

        AsyncScheduler.runTask(SkycadeCustomItemsPlugin.getInstance(), () -> cache.getUnchecked(id));
    }

    public static Integer getPouchId(ItemStack item) {
        if (item == null) return null;
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        if (itemStack == null || !itemStack.hasTag()) return null;
        if (!itemStack.getTag().hasKey("pouch_id")) {
            return null;
        }

        return ((NBTTagInt) itemStack.getTag().get("pouch_id")).d();
    }

    public static ItemStack getItem(int id) {
        ItemStack item = new ItemBuilder(Material.INK_SACK)
                .setDisplayName(ChatColor.GOLD + "Pouch")
                .build();

        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = itemStack.getTag();
        nbt.set("pouch_id", new NBTTagInt(id));
        itemStack.setTag(nbt);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    private final int id;
    private int level = 1;
    private ItemStack[] contents = new ItemStack[]{};

    public PouchData(int id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        AsyncScheduler.runTask(SkycadeCustomItemsPlugin.getInstance(), this::persist);
    }

    public int getId() {
        return id;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
        AsyncScheduler.runTask(SkycadeCustomItemsPlugin.getInstance(), this::persist);
    }

    public void persist() {
        try (Connection connection = CoreSettings.getInstance().getConnection()) {
            String sql = "INSERT INTO skycade_pouches (id, level, contents, instance) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE level = VALUES(level), contents = VALUES(contents)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.setInt(2, level);
                statement.setString(3, CoreUtil.itemStackArrayToBase64(contents));
                statement.setString(4, CoreSettings.getInstance().getThisInstance());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
