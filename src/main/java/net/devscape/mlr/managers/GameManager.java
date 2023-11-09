package net.devscape.mlr.managers;

import lombok.Getter;
import lombok.Setter;
import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.modes.Modes;
import net.devscape.mlr.modes.settings.ControlThePoint;
import net.devscape.mlr.modes.settings.TeamElimination;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.devscape.mlr.utils.Utils.getLocation;

@Getter
@Setter
public class GameManager {

    private List<Game> games = new ArrayList<>();
    private Game game;
    private Random random = new Random();
    private int pink = 4;
    private int green = 4;

    private Modes mode;

    public GameManager() {
        load();
    }

    public void changeGame() {
        if (games.size() > 1) {
            game = getRandomGame(games);
        }
    }

    public void load() {
        for (String str : MineraveLaserTag.getMlt().getConfig().getConfigurationSection("games").getKeys(false)) {
            Game game = new Game(
                    str,
                    getLocation(str, "green"),
                    getLocation(str, "pink"),
                    Modes.valueOf(Objects.requireNonNull(MineraveLaserTag.getMlt().getConfig().getString("games." + str + ".mode")).toUpperCase()));


            games.add(game);
        }

        game = getRandomGame(games);
    }

    private Game getRandomGame(List<Game> newGameMap) {
        if (!newGameMap.isEmpty()) {
            int randomIndex = random.nextInt(newGameMap.size());
            return newGameMap.get(randomIndex);
        }
        return null;
    }

    public void checkWin() {
        if (game.getMode() == Modes.TEAM_ELIMINATION) {
            TeamElimination te = MineraveLaserTag.getMlt().getModeManager().getTeamElimination();
            te.checkWin();
        }

        if (game.getMode() == Modes.CONTROL_THE_POINT) {
            ControlThePoint ctp = MineraveLaserTag.getMlt().getModeManager().getControlThePoint();
            ctp.checkWin();
        }
    }
}