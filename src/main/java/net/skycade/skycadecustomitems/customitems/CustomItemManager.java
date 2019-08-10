package net.skycade.skycadecustomitems.customitems;

import net.skycade.skycadecustomitems.SkycadeCustomItemsPlugin;
import net.skycade.skycadecustomitems.customitems.commands.CustomItemCommand;
import net.skycade.skycadecustomitems.customitems.items.*;
import net.skycade.skycadecustomitems.customitems.items.nms.MendingScarabItem1_12;
import net.skycade.skycadecustomitems.customitems.items.nms.MendingScarabItem1_8;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomItemManager {

    private final SkycadeCustomItemsPlugin plugin;
    private static final Map<String, CustomItem> customItems = new LinkedHashMap<>();

    public CustomItemManager(SkycadeCustomItemsPlugin plugin) {
        this.plugin = plugin;

        new CustomItemCommand();

        registerCustomItems();
    }

    private void registerCustomItems(){
        registerCustomItem(new LegendaryVialItem());
        registerCustomItem(new ChargerItem());
        registerCustomItem(new RenameTagItem());
        registerCustomItem(new TeleportationOrbItem());
        registerCustomItem(new ExperienceBoosterItem());
        registerCustomItem(new ProtectionOrbItem());
        registerCustomItem(new PouchItem());
        registerCustomItem(new PouchUpgraderItem());
        if (SkycadeCustomItemsPlugin.v18) {
            registerCustomItem(new MendingScarabItem1_8());
        } else {
            registerCustomItem(new MendingScarabItem1_12());
        }
    }

    private void registerCustomItem(CustomItem item) {
        String node = "disabled-items." + item.getHandle();
        if (!SkycadeCustomItemsPlugin.getInstance().getConfig().contains(node)) {
            //If node is not in the config, automatically load.
            if (item.autoLoad()) {
                customItems.put(item.getHandle().toUpperCase(), item);
                Bukkit.getPluginManager().registerEvents(item, plugin);
                item.postLoad();
            }
        } else if (!SkycadeCustomItemsPlugin.getInstance().getConfig().getBoolean(node)) {
            //If node exists and is false, load. If true, do not load.
            customItems.put(item.getHandle().toUpperCase(), item);
            Bukkit.getPluginManager().registerEvents(item, plugin);
            item.postLoad();
        }
    }

    public static Map<String, CustomItem> getAllCustomItems() {
        return customItems;
    }

    public Plugin getPlugin() {
        if (plugin == null)
            return SkycadeCustomItemsPlugin.getInstance();
        return plugin;
    }

    public static CustomItem getTypeFromString(String handle) {
        if (customItems.containsKey(handle))
            return customItems.get(handle);
        return null;
    }


}
