package net.skycade.skycadecustomitems.util;

import net.skycade.koth.SkycadeKoth;
import net.skycade.koth.game.GamePhase;
import net.skycade.koth.game.KOTHGame;

import java.util.Optional;

public class KothHook {

    public static boolean isKothRunning() {
        return Optional.ofNullable(SkycadeKoth.getInstance().getGameManager().getActiveKOTHGame())
                .map(KOTHGame::getCurrentPhase).map(p -> p == GamePhase.IN_PROGRESS).orElse(false);
    }
}
