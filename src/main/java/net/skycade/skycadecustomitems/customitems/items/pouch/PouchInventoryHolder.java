package net.skycade.skycadecustomitems.customitems.items.pouch;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PouchInventoryHolder implements InventoryHolder {

    private final Player player;
    private final PouchData pouchData;
    private Inventory inventory;

    public PouchInventoryHolder(Player player, PouchData pouchData) {
        this.player = player;
        this.pouchData = pouchData;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public PouchData getPouchData() {
        return pouchData;
    }
}
