package net.skycade.skycadecustomitems.customitems.commands;

import net.skycade.SkycadeCore.Localization;
import net.skycade.SkycadeCore.Localization.Message;
import net.skycade.SkycadeCore.utility.command.SkycadeCommand;
import net.skycade.SkycadeCore.utility.command.addons.Permissible;
import net.skycade.skycadecustomitems.customitems.CustomItemManager;
import net.skycade.skycadecustomitems.customitems.items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.skycade.SkycadeCore.Localization.Global.INVALID_INTEGER;
import static net.skycade.SkycadeCore.Localization.Global.PLAYER_NOT_FOUND;

@Permissible("skycade.customitems.custom-item-command")
public class CustomItemCommand extends SkycadeCommand {

    private static final Message USAGE = new Message("custom-item.usage", "&b/customitem &7<player> &6<item-name> &a[amount] &6(optional){duration}");
    private static final Message INVALID_ITEM = new Message("custom-item.invalid-item", "&cThat is not a valid custom item!");
    private static final Message NOT_ENOUGH_SPACE = new Message("custom-item.not-enough-space", "&cThe player does not have enough room in their inventory!");
    private static final Message DONE = new Message("custom-item.done", "&aDone!");

    public CustomItemCommand() {
        super("customitem");

        Localization.getInstance().registerMessages("skycade.customitems",
                USAGE,
                INVALID_ITEM,
                NOT_ENOUGH_SPACE,
                DONE
        );
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            USAGE.msg(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            PLAYER_NOT_FOUND.msg(sender);
            return;
        }

        CustomItem item = CustomItemManager.getTypeFromString(args[1].toUpperCase());
        if (item == null) {
            INVALID_ITEM.msg(sender);
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            INVALID_INTEGER.msg(sender);
            return;
        }

        if (amount < 1) {
            INVALID_INTEGER.msg(sender);
            return;
        }

        int duration = 1;
        if (args.length > 3) {
            try {
                if (args[3] != null) {
                    duration = Integer.parseInt(args[3]);
                }
            } catch (NumberFormatException e) {
                INVALID_INTEGER.msg(sender);
                return;
            }
        }

        if (duration < 1) {
            INVALID_INTEGER.msg(sender);
            return;
        }

        if (duration > 1){
            item.giveItem(target, duration, amount);
        } else {
            item.giveItem(target, amount);
        }

        DONE.msg(sender);

    }
}
