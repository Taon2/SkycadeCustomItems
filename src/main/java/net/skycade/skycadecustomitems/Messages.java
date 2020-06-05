package net.skycade.skycadecustomitems;

import net.skycade.SkycadeCore.Localization;

public class Messages {
    // poches
    public static final Localization.Message POUCH_UPGRADER_MAX_LEVEL = new Localization.Message("pouchupgrader.max-level", "&cYou have reached the maximum level for this pouch!");
    public static final Localization.Message POUCH_UPGRADER_NOT_POUCH = new Localization.Message("pouchupgrader.not-a-pouch", "&cThat's not a pouch!");
    public static final Localization.Message POUCH_UPGRADER_SUCCESS = new Localization.Message("pouchupgrader.success", "&aSuccess! &bYou upgraded your pouch to level &6%level%&b!");
    public static final Localization.Message TOO_MANY_POUCHES = new Localization.Message("pouch.too-many", "&cYou have too many pouches with you! Maximum of 2. Get rid of some to be able to use this pouch.");


    // tnt wand
    public static final Localization.Message TNTWAND_CRAFTED_TNT = new Localization.Message("tntwand.crafted-tnt", "&aSuccess! &bYou crafted &6%amount% &bTNT with your wand!");

    // general
    public static final Localization.Message IN_COMBAT = new Localization.Message("in-combat", "&cThis item can only be used outside of combat!");
    public static final Localization.Message ONLY_ONE_ALLOWED = new Localization.Message("only-one-allowed", "&cThis item can only be used with a stack size of 1!");
    public static final Localization.Message REPAIRED = new Localization.Message("repaired", "&aItem repaired!");
    public static final Localization.Message ACTIVATED = new Localization.Message("activated", "&aActivated!");

    public static void init() {
        Localization.getInstance().registerMessages("skycade.customitems",
                POUCH_UPGRADER_MAX_LEVEL,
                POUCH_UPGRADER_NOT_POUCH,
                POUCH_UPGRADER_SUCCESS,
                TOO_MANY_POUCHES,
                REPAIRED,
                IN_COMBAT,
                ONLY_ONE_ALLOWED,
                ACTIVATED
        );
    }
}
