package net.devscape.mlr.modes.settings;

import lombok.Getter;
import lombok.Setter;
import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.modes.Modes;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static net.devscape.mlr.utils.Utils.titlePlayer;

@Getter
@Setter
public class ControlThePoint {

    private Location controlLocationPos1;
    private Location controlLocationPos2;
    private int controlPercentageGreen = 0;
    private int controlPercentagePink = 0;
    private final int controlPercentageMax = 100;
    private boolean contested;
    private String teamContesting;

    private BukkitTask controlTask;

    public ControlThePoint(Location controlLocationPos1, Location controlLocationPos2) {
        this.controlLocationPos1 = controlLocationPos1;
        this.controlLocationPos2 = controlLocationPos2;
    }

    public void checkWin() {
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        if (game.getMode() != Modes.CONTROL_THE_POINT) { return; }

        if (getControlPercentageGreen() >= 100 && getControlPercentagePink() < 100) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                titlePlayer(player, "&a&lGame Over!", "&aGreen Team Wins..", 20, 20 * 3, 20);
            }

            game.endGame();
        }

        if (getControlPercentagePink() >= 100 && getControlPercentageGreen() < 100) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                titlePlayer(player, "&a&lGame Over!", "&dPink Team Wins..", 20, 20 * 3, 20);
            }

            game.endGame();
        }
    }

    public boolean isPlayerOnControl(Player player) {
        Location playerLocation = player.getLocation();

        double minX = Math.min(controlLocationPos1.getX(), controlLocationPos2.getX());
        double minY = Math.min(controlLocationPos1.getY(), controlLocationPos2.getY());
        double minZ = Math.min(controlLocationPos1.getZ(), controlLocationPos2.getZ());
        double maxX = Math.max(controlLocationPos1.getX(), controlLocationPos2.getX());
        double maxY = Math.max(controlLocationPos1.getY(), controlLocationPos2.getY());
        double maxZ = Math.max(controlLocationPos1.getZ(), controlLocationPos2.getZ());

        return playerLocation.getX() >= minX && playerLocation.getX() <= maxX
                && playerLocation.getY() >= minY && playerLocation.getY() <= maxY
                && playerLocation.getZ() >= minZ && playerLocation.getZ() <= maxZ;
    }

    public boolean checkPlayersInsideControl() {
        int greenPlayersOnPoint = 0;
        int pinkPlayersOnPoint = 0;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (isPlayerOnControl(onlinePlayer)) {
                String playerTeam = MineraveLaserTag.getMlt().getGameManager().getGame().getTeam(onlinePlayer);

                if ("green".equalsIgnoreCase(playerTeam)) {
                    greenPlayersOnPoint++;
                } else if ("pink".equalsIgnoreCase(playerTeam)) {
                    pinkPlayersOnPoint++;
                }
            }
        }

        return greenPlayersOnPoint <= 0 && pinkPlayersOnPoint <= 0;
    }

    public void run() {
        if (contested || controlTask != null) {
            return;
        }

        int greenPlayersOnPoint = 0;
        int pinkPlayersOnPoint = 0;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (isPlayerOnControl(onlinePlayer)) {
                String playerTeam = MineraveLaserTag.getMlt().getGameManager().getGame().getTeam(onlinePlayer);

                if ("green".equalsIgnoreCase(playerTeam)) {
                    greenPlayersOnPoint++;
                } else if ("pink".equalsIgnoreCase(playerTeam)) {
                    pinkPlayersOnPoint++;
                }
            }
        }


        if (greenPlayersOnPoint >= 1)  {
            teamContesting = "green";
        } else if (pinkPlayersOnPoint >= 1) {
            teamContesting = "pink";
        }

        contested = true;

        controlTask = new BukkitRunnable() {
            @Override
            public void run() {
                int greenPlayersOnPoint = 0;
                int pinkPlayersOnPoint = 0;

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (isPlayerOnControl(onlinePlayer)) {
                        String playerTeam = MineraveLaserTag.getMlt().getGameManager().getGame().getTeam(onlinePlayer);

                        if ("green".equalsIgnoreCase(playerTeam)) {
                            greenPlayersOnPoint++;
                        } else if ("pink".equalsIgnoreCase(playerTeam)) {
                            pinkPlayersOnPoint++;
                        }
                    }
                }

                if (greenPlayersOnPoint == 0 && pinkPlayersOnPoint == 0) {
                    contested = false;
                    controlTask.cancel();
                    controlTask = null;
                }

                if (greenPlayersOnPoint >= 1)  {
                    teamContesting = "green";
                } else if (pinkPlayersOnPoint >= 1) {
                    teamContesting = "pink";
                } else {
                    teamContesting = null;
                }

                if (greenPlayersOnPoint >= 1 && pinkPlayersOnPoint >= 1) {
                    contested = false;
                    controlTask.cancel();
                    controlTask = null;
                    teamContesting = null;
                } else {
                    handleTeamContesting();
                    displayControlParticles();
                }
            }
        }.runTaskTimer(MineraveLaserTag.getMlt(), 0, 20L);
    }

    private void handleTeamContesting() {
        if (teamContesting.equalsIgnoreCase("green")) {
            controlPercentageGreen++;
            MineraveLaserTag.getMlt().getGameManager().checkWin();
        } else if (teamContesting.equalsIgnoreCase("pink")) {
            controlPercentagePink++;
            MineraveLaserTag.getMlt().getGameManager().checkWin();
        }
    }

    private void displayControlParticles() {
        World world = controlLocationPos1.getWorld();
        double minX = Math.min(controlLocationPos1.getX(), controlLocationPos2.getX());
        double minY = Math.min(controlLocationPos1.getY(), controlLocationPos2.getY());
        double minZ = Math.min(controlLocationPos1.getZ(), controlLocationPos2.getZ());
        double maxX = Math.max(controlLocationPos1.getX(), controlLocationPos2.getX());
        double maxY = Math.max(controlLocationPos1.getY(), controlLocationPos2.getY());
        double maxZ = Math.max(controlLocationPos1.getZ(), controlLocationPos2.getZ());

        for (double x = minX; x <= maxX; x += 0.1) {
            double y = minY - 0.1;
            Location particleLocation1 = new Location(world, x, y, minZ);
            Location particleLocation2 = new Location(world, x, y, maxZ);

            if ("green".equalsIgnoreCase(teamContesting)) {
                world.spawnParticle(Particle.REDSTONE, particleLocation1, 0, 1.0, 0.0, 0.0, new Particle.DustOptions(Color.fromRGB(230, 59, 176), 1));
            } else if ("pink".equalsIgnoreCase(teamContesting)) {
                world.spawnParticle(Particle.REDSTONE, particleLocation2, 0, 1.0, 0.0, 0.0, new Particle.DustOptions(Color.fromRGB(59, 230, 162), 1));
            }
        }

        for (double z = minZ; z <= maxZ; z += 0.1) {
            double y = minY - 0.1;
            Location particleLocation1 = new Location(world, minX, y, z);
            Location particleLocation2 = new Location(world, maxX, y, z);

            if ("green".equalsIgnoreCase(teamContesting)) {
                world.spawnParticle(Particle.REDSTONE, particleLocation1, 0, 1.0, 0.0, 0.0, new Particle.DustOptions(Color.fromRGB(230, 59, 176), 1));
            } else if ("pink".equalsIgnoreCase(teamContesting)) {
                world.spawnParticle(Particle.REDSTONE, particleLocation2, 0, 1.0, 0.0, 0.0, new Particle.DustOptions(Color.fromRGB(59, 230, 162), 1));
            }
        }
    }

    private void removeControlParticles() {
        World world = controlLocationPos1.getWorld();
        double minX = Math.min(controlLocationPos1.getX(), controlLocationPos2.getX());
        double minY = Math.min(controlLocationPos1.getY(), controlLocationPos2.getY());
        double minZ = Math.min(controlLocationPos1.getZ(), controlLocationPos2.getZ());
        double maxX = Math.max(controlLocationPos1.getX(), controlLocationPos2.getX());
        double maxY = Math.max(controlLocationPos1.getY(), controlLocationPos2.getY());
        double maxZ = Math.max(controlLocationPos1.getZ(), controlLocationPos2.getZ());

        for (double x = minX; x <= maxX; x += 0.1) {
            for (double y = minY; y <= maxY; y += 0.1) {
                for (double z = minZ; z <= maxZ; z += 0.1) {
                    Location particleLocation = new Location(world, x, y, z);
                    world.spawnParticle(Particle.SMOKE_NORMAL, particleLocation, 0, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }
}