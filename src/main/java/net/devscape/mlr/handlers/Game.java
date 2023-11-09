package net.devscape.mlr.handlers;

import lombok.Getter;
import lombok.Setter;
import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.modes.Modes;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.devscape.mlr.utils.Utils.*;

@Getter
@Setter
public class Game {

    private String name;
    private List<UUID> pink = new ArrayList<>();
    private List<UUID> green = new ArrayList<>();

    private Location greenLoc;
    private Location pinkLoc;

    private boolean countdownStarted = false;

    private GameState gameState;
    private int countdown = 17;

    private Modes mode;

    public Game(String name, Location greenLoc, Location pinkLoc, Modes mode) {
        this.name = name;
        this.greenLoc = greenLoc;
        this.pinkLoc = pinkLoc;
        this.mode = mode;
        this.gameState = GameState.WAITING;
    }

    public void include(Player player) {
        giveWaitingItems(player);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        MineraveLaserTag.getMlt().getGunListeners().getShotCounts().remove(player.getUniqueId());
        MineraveLaserTag.getMlt().getGunListeners().getShotCounts().put(player.getUniqueId(), 5);

        MineraveLaserTag.getMlt().getGameTasks().getBossbar().addPlayer(player);

        int pinkSize = pink.size();
        int greenSize = green.size();

        if (pinkSize == 4 && greenSize == 4) {
            // teams are full
            return;
        }

        titlePlayer(player, "&a&lLaser Tag", "&eMode: &6" + mode.name(), 20, 20 * 2, 20);
        soundPlayer(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        if (pinkSize > greenSize) {
            selectTeam(player, "green");
        } else if (greenSize > pinkSize) {
            selectTeam(player, "pink");
        } else {
            if (Math.random() < 0.5) {
                selectTeam(player, "pink");
            } else {
                selectTeam(player, "green");
            }
        }

        List<String> tip_info = new ArrayList<>();
        if (mode == Modes.TEAM_ELIMINATION) {
            tip_info.add("&#007bff&m-----------------------------------------------------");
            tip_info.add("&#00ff7b&l» &fTeam Elimination");
            tip_info.add("&#abe3ffFirst Team to " + MineraveLaserTag.getMlt().getModeManager().getTeamElimination().getMaxWinningKills() + " kills wins!");
            tip_info.add("");
            tip_info.add("&f&o&lRight-Click &f&oyour &lLaser Gun &f&oto shoot.");
            tip_info.add("&#007bff&m-----------------------------------------------------");
        } else if (mode == Modes.CONTROL_THE_POINT) {
            tip_info.add("&#007bff&m-----------------------------------------------------");
            tip_info.add("&#00ff7b&l» &fControl The Point");
            tip_info.add("&#abe3ffControl the center point and get to &L100% &#abe3ffto win!");
            tip_info.add("");
            tip_info.add("&f&o&lRight-Click &f&oyour &lLaser Gun &f&oto shoot.");
            tip_info.add("&#007bff&m-----------------------------------------------------");
        }

        for (String str : tip_info) {
            player.sendMessage(format(str));
        }
    }

    public void exclude(Player player) {
        getPink().remove(player.getUniqueId());
        getGreen().remove(player.getUniqueId());

        MineraveLaserTag.getMlt().getGameTasks().getBossbar().removePlayer(player);
    }

    public void selectTeam(Player player, String team) {
        if (team.equalsIgnoreCase("pink")) {
            pink.add(player.getUniqueId());
            player.teleport(pinkLoc);

            for (UUID uuid : getPink()) {
                Player pTeam = Bukkit.getPlayer(uuid);

                if (pTeam != null) {
                    try {
                        MineraveLaserTag.getMlt().getGlowingEntities().setGlowing(player, pTeam, ChatColor.LIGHT_PURPLE);
                        MineraveLaserTag.getMlt().getGlowingEntities().setGlowing(pTeam, player, ChatColor.LIGHT_PURPLE);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else if (team.equalsIgnoreCase("green")) {
            green.add(player.getUniqueId());
            player.teleport(greenLoc);

            for (UUID uuid : getGreen()) {
                Player pTeam = Bukkit.getPlayer(uuid);

                if (pTeam != null) {
                    try {
                        MineraveLaserTag.getMlt().getGlowingEntities().setGlowing(player, pTeam, ChatColor.GREEN);
                        MineraveLaserTag.getMlt().getGlowingEntities().setGlowing(pTeam, player, ChatColor.GREEN);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        int pinkSize = pink.size();
        int greenSize = green.size();

        if (!isCountdownStarted()) {
            if (pinkSize > 0 && greenSize > 0) {
                startCountdown();
            }
        }
    }

    public void endGame() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(MineraveLaserTag.getMlt(), () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        getGreen().remove(player.getUniqueId());
                        getPink().remove(player.getUniqueId());
                        MineraveLaserTag.getMlt().getBungeeUtils().sendPlayerToServer(player, "hub");
                    }
                });
            }
        }.runTaskLaterAsynchronously(MineraveLaserTag.getMlt(), 20L * 3);

        gameState = GameState.WAITING;
        countdown = 17;
        countdownStarted = false;
        getGreen().clear();
        getPink().clear();
        MineraveLaserTag.getMlt().getGunListeners().getShotCounts().clear();
        MineraveLaserTag.getMlt().getGunListeners().getCooldowns().clear();

        MineraveLaserTag.getMlt().getGameManager().changeGame();
    }

    public void startCountdown() {
        countdownStarted = true;
        gameState = GameState.STARTING;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getWorld().playSound(player.getLocation(), "minecraft:countdown", SoundCategory.MASTER, 0.15F, 1);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (pink.size() == 0 || green.size() == 0) {
                    Bukkit.broadcastMessage(format("&f侵 &7Not enough players to start.."));
                    countdownStarted = false;
                    gameState = GameState.WAITING;
                    countdown = 17;
                    cancel();
                }

                if (countdown <= 0) {
                    start();
                    countdownStarted = false;
                    countdown = 17;
                    cancel();
                }

                countdown--;
            }
        }.runTaskTimer(MineraveLaserTag.getMlt(), 0, 20L);
    }

    public void start() {
        gameState = GameState.IN_PROGRESS;
        Bukkit.broadcastMessage(format("&f係 &7The game has started! FIGHT...."));

        for (UUID uuid : getGreen()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            if (player.getFallDistance() > 0.0F) {
                EntityDamageEvent damageEvent = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, player.getFallDistance());
                Bukkit.getPluginManager().callEvent(damageEvent);
                if (!damageEvent.isCancelled()) {
                    player.setFallDistance(0.0F);
                }
            }

            giveGun(player);
        }

        for (UUID uuid : getPink()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            if (player.getFallDistance() > 0.0F) {
                EntityDamageEvent damageEvent = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, player.getFallDistance());
                Bukkit.getPluginManager().callEvent(damageEvent);
                if (!damageEvent.isCancelled()) {
                    player.setFallDistance(0.0F);
                }
            }

            giveGun(player);
        }
    }

    public String getTeam(Player player) {
        if (green.contains(player.getUniqueId())) {
            return "green";
        } else if (pink.contains(player.getUniqueId())) {
            return "pink";

        }

        return "";
    }
}