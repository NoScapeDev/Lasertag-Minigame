package net.devscape.mlr.modes.settings;

import lombok.Getter;
import lombok.Setter;
import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.modes.Modes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static net.devscape.mlr.utils.Utils.titlePlayer;

@Getter
@Setter
public class TeamElimination {

    private int greenKills;
    private int pinkKills;

    private int maxWinningKills = 20;

    public void checkWin() {
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        if (game.getMode() != Modes.TEAM_ELIMINATION) { return; }

        if (pinkKills < maxWinningKills && greenKills >= maxWinningKills) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                titlePlayer(player, "&a&lGame Over!", "&aGreen Team Wins..", 20, 20 * 3, 20);
            }

            game.endGame();
        }

        if (greenKills < maxWinningKills && pinkKills >= maxWinningKills) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                titlePlayer(player, "&a&lGame Over!", "&dPink Team Wins..", 20, 20 * 3, 20);
            }

            game.endGame();
        }
    }
}