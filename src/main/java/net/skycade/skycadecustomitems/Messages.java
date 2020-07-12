package net.skycade.skycadecustomitems;

import net.skycade.SkycadeCore.Localization;
import net.skycade.SkycadeCore.Localization.Message;

public class Messages {
    // poches
    public static final Message POUCH_UPGRADER_MAX_LEVEL = new Message("pouchupgrader.max-level", "&cYou have reached the maximum level for this pouch!");
    public static final Message POUCH_UPGRADER_NOT_POUCH = new Message("pouchupgrader.not-a-pouch", "&cThat's not a pouch!");
    public static final Message POUCH_UPGRADER_SUCCESS = new Message("pouchupgrader.success", "&aSuccess! &bYou upgraded your pouch to level &6%level%&b!");
    public static final Message TOO_MANY_POUCHES = new Message("pouch.too-many", "&cYou have too many pouches with you! Maximum of 2. Get rid of some to be able to use this pouch.");

    // tnt wand
    public static final Message TNTWAND_SENT_TNT = new Message("tntwand.sent-tnt", "&aSuccess! &bYou sent &6%amount% &bTNT to your faction bank with your wand!");
    // general
    public static final Message REPAIRED = new Message("repaired", "&aItem repaired!");

    // koth
    public static final Message KOTH_IN_PROGRESS = new Message("koth-in-progress", "&cKOTH in progress! You cannot use a protection orb.");
    public static final Message KOTH_STARTED = new Message("koth-started", "&cKOTH started! Your protection orb has been disabled.");

    public static final Message IN_COMBAT = new Message("in-combat", "&cThis item can only be used outside of combat!");
    public static final Message ONLY_ONE_ALLOWED = new Message("only-one-allowed", "&cThis item can only be used with a stack size of 1!");
    public static final Message ACTIVATED = new Message("activated", "&aActivated!");

    public static void init() {
        Localization.getInstance().registerMessages("skycade.customitems",
                POUCH_UPGRADER_MAX_LEVEL,
                POUCH_UPGRADER_NOT_POUCH,
                POUCH_UPGRADER_SUCCESS,
                TOO_MANY_POUCHES,
                IN_COMBAT,
                ONLY_ONE_ALLOWED,
                ACTIVATED,
                REPAIRED,
                KOTH_IN_PROGRESS,
                KOTH_STARTED,
                TNTWAND_SENT_TNT
        );
    }
}
