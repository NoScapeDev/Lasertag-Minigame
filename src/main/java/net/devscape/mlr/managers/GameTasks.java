package net.devscape.mlr.managers;

import lombok.Getter;
import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.handlers.GameState;
import net.devscape.mlr.modes.Modes;
import net.devscape.mlr.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static net.devscape.mlr.utils.Utils.format;

@Getter
public class GameTasks {

    private BossBar bossbar;
    private Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

    public GameTasks() {
        processTab();
        processBossbar();
        MineraveLaserTag.getMlt().getGunListeners().processAction();
    }

    public void processTab() {

    }

    public void processBossbar() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(MineraveLaserTag.getMlt(), () -> {
                   if (bossbar == null) {
                       bossbar = Bukkit.createBossBar(
                               "Loading...",
                               BarColor.BLUE,
                               BarStyle.SOLID);
                   }

                   if (game.getGameState() == GameState.WAITING) {
                       bossbar.setTitle(format("&f" + Bukkit.getOnlinePlayers().size() + "&7/&a8"));
                   }

                    if (game.getGameState() == GameState.STARTING) {
                        int secondsLeft = getGame().getCountdown();

                        if (secondsLeft <= 15) {
                            // String countdown_action = MineraveLaserTag.getMlt().getConfig().getString("countdown.countdown-" + secondsLeft);
                            bossbar.setTitle(format("&e" + secondsLeft));
                        } if (secondsLeft <= 0) {
                            bossbar.setTitle(format("&f&lFIGHT!"));
                        }
                    }

                   if (game.getGameState() == GameState.IN_PROGRESS && game.getMode() == Modes.TEAM_ELIMINATION) {
                       int green = MineraveLaserTag.getMlt().getModeManager().getTeamElimination().getGreenKills();
                       int pink = MineraveLaserTag.getMlt().getModeManager().getTeamElimination().getPinkKills();

                       bossbar.setTitle(format("&a" + green + "   &f:   &d" + pink));
                   }

                    if (game.getGameState() == GameState.IN_PROGRESS && game.getMode() == Modes.CONTROL_THE_POINT) {
                        int green = MineraveLaserTag.getMlt().getModeManager().getControlThePoint().getControlPercentageGreen();
                        int pink = MineraveLaserTag.getMlt().getModeManager().getControlThePoint().getControlPercentagePink();

                        String contesting = MineraveLaserTag.getMlt().getModeManager().getControlThePoint().getTeamContesting();

                        if (contesting == null) {
                            bossbar.setTitle(format("&7No teams started contesting!"));
                        } else {
                            if ("green".equalsIgnoreCase(contesting)) {
                                bossbar.setTitle(format("&a&l" + green + "   &f:   &7" + pink));
                            } else if ("pink".equalsIgnoreCase(contesting)) {
                                bossbar.setTitle(format("&7" + green + "   &f:   &d&l" + pink));
                            } else {
                                bossbar.setTitle(format("&a" + green + "   &f:   &d" + pink));
                            }
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(MineraveLaserTag.getMlt(), 0, 20L);
    }

    private String formatGameTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}
