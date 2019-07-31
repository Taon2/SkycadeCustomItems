package net.skycade.skycadecustomitems;

import net.skycade.SkycadeCore.SkycadePlugin;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;
import java.util.TreeMap;

public class SkycadeCustomItemsPlugin extends SkycadePlugin {

    private static SkycadeCustomItemsPlugin instance;

    public SkycadeCustomItemsPlugin() {
        instance = this;
    }

    public static SkycadeCustomItemsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Map<String, Object> defaults = new TreeMap<>();
        defaults.put("disabled-items", new YamlConfiguration());

        setConfigDefaults(defaults);
        loadDefaultConfig();

        new CustomItemManager(instance);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
