package net.skycade.skycadecustomitems;

import net.skycade.SkycadeCore.Localization;

public class Messages {
    public static final Localization.Message POUCH_UPGRADER_MAX_LEVEL = new Localization.Message("pouchupgrader.max-level", "&cYou have reached the maximum level for this pouch!");
    public static final Localization.Message POUCH_UPGRADER_NOT_POUCH = new Localization.Message("pouchupgrader.not-a-pouch", "&cThat's not a pouch!");
    public static final Localization.Message POUCH_UPGRADER_SUCCESS = new Localization.Message("pouchupgrader.success", "&aSuccess! &bYou upgraded your pouch to level &6%level%&b!");
    public static final Localization.Message TOO_MANY_POUCHES = new Localization.Message("pouch.too-many", "&cYou have too many pouches with you! Maximum of 2. Get rid of some to be able to use this pouch.");

    public static final Localization.Message REPAIRED = new Localization.Message("repaired", "&aItem repaired!");

    public static void init() {
        Localization.getInstance().registerMessages("skycade.customitems",
                POUCH_UPGRADER_MAX_LEVEL,
                POUCH_UPGRADER_NOT_POUCH,
                POUCH_UPGRADER_SUCCESS,
                TOO_MANY_POUCHES,
                REPAIRED
        );
    }
}
