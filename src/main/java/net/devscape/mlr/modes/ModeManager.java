package net.devscape.mlr.modes;

import jdk.internal.icu.text.NormalizerBase;
import lombok.Getter;
import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.modes.settings.ControlThePoint;
import net.devscape.mlr.modes.settings.TeamElimination;
import org.bukkit.Location;

import static net.devscape.mlr.utils.Utils.getLocation;

@Getter
public class ModeManager {

    /// mode settings
    private ControlThePoint controlThePoint;
    private TeamElimination teamElimination;

    public ModeManager() {
        load();
    }

    public void load() {
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        teamElimination = new TeamElimination();

        Location controlLocationPos1 = getLocation(game.getName(), "control-pos1");
        Location controlLocationPos2 = getLocation(game.getName(), "control-pos2");

        if (controlLocationPos1 == null && controlLocationPos2 == null) {
            return;
        }

        controlThePoint = new ControlThePoint(controlLocationPos1, controlLocationPos2);
    }
}