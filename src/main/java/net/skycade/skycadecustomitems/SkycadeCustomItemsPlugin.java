package net.skycade.skycadecustomitems;

import net.skycade.SkycadeCore.SkycadePlugin;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import net.skycade.skycadecustomitems.customitems.listeners.ProtectionListener;
import net.skycade.skycadecustomitems.customitems.listeners.ProtectionListener1_12;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;
import java.util.TreeMap;

public class SkycadeCustomItemsPlugin extends SkycadePlugin {

    private static SkycadeCustomItemsPlugin instance;

    public static boolean v18;
    public static boolean v112;

    public SkycadeCustomItemsPlugin() {
        instance = this;
    }

    public static SkycadeCustomItemsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        v18 = Bukkit.getServer().getClass().getPackage().getName().contains("1_8");
        v112 = Bukkit.getServer().getClass().getPackage().getName().contains("1_12");

        registerListeners(new ProtectionListener());
        if (!v18) registerListeners(new ProtectionListener1_12());

        Map<String, Object> defaults = new TreeMap<>();
        defaults.put("disabled-items", new YamlConfiguration());

        setConfigDefaults(defaults);
        loadDefaultConfig();

        Messages.init();

        new CustomItemManager(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
