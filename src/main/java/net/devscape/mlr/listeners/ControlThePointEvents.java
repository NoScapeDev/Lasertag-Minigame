package net.devscape.mlr.listeners;

import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.handlers.GameState;
import net.devscape.mlr.modes.Modes;
import net.devscape.mlr.modes.settings.ControlThePoint;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ControlThePointEvents implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        if (game.getMode() == Modes.CONTROL_THE_POINT) {
            if (game.getGameState() == GameState.IN_PROGRESS) {
                ControlThePoint ctp = MineraveLaserTag.getMlt().getModeManager().getControlThePoint();
                if (ctp != null && ctp.isPlayerOnControl(event.getPlayer())) {
                    ctp.run();
                }
            }
        }
    }
}
