package net.devscape.mlr.listeners;

import lombok.Getter;
import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.modes.Modes;
import net.devscape.mlr.modes.settings.TeamElimination;
import net.devscape.mlr.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static net.devscape.mlr.utils.Utils.*;

@Getter
public class GunListener implements Listener {

    private final Map<UUID, Integer> shotCounts = new HashMap<>();
    private final Map<UUID, Cooldown> cooldowns = new HashMap<>();
    private final int maxShots = 5;
    private final int cooldownTime = 5; // In seconds

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (event.getMaterial() == Material.STICK && event.getAction().toString().contains("RIGHT_CLICK")) {
            if (isOnCooldown(playerUUID)) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 1);
                return;
            }

            if (player.getGameMode() == GameMode.SPECTATOR) {
                return;
            }

            decrementShotCount(playerUUID);
            Vector direction = player.getEyeLocation().getDirection().normalize();
            Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

            org.bukkit.Particle particleType = org.bukkit.Particle.REDSTONE;
            player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1, 1);

            new BukkitRunnable() {
                int maxDistance = 150; // Adjust this value for the maximum range
                int particlesSpawned = 0; // Track the number of particles spawned

                Location particleLocation = player.getEyeLocation();
                Vector particleDirection = direction.clone().multiply(2.6); // Adjust particle speed

                @Override
                public void run() {
                    // Check if the particles have reached the maximum distance or exceeded the particle count
                    if (particleLocation.distance(player.getEyeLocation()) >= maxDistance
                            || particlesSpawned >= 30 // Adjust the maximum number of particles per tick
                            || hasBlockCollision(particleLocation.getBlock())) {
                        this.cancel(); // Stop the task
                        return;
                    }

                    if (game.getPink().contains(playerUUID)) {
                        player.getWorld().spawnParticle(particleType, particleLocation, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(230, 59, 176), 1));
                    } else if (game.getGreen().contains(playerUUID)) {
                        player.getWorld().spawnParticle(particleType, particleLocation, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(59, 230, 162), 1));
                    }

                    for (Entity entity : particleLocation.getWorld().getNearbyEntities(particleLocation, 1.0, 1.0, 1.0)) {
                        if (entity instanceof Player && entity != player) {
                            Player target = (Player) entity;

                            if (target.getGameMode() == GameMode.SPECTATOR) {
                                cancel();
                                return;
                            }

                            if (game.getPink().contains(target.getUniqueId()) && game.getPink().contains(playerUUID)) {
                                cancel();
                                return;
                            }

                            if (game.getGreen().contains(target.getUniqueId()) && game.getGreen().contains(playerUUID)) {
                                cancel();
                                return;
                            }

                            // Calculate the hit location based on the player's eye height
                            double eyeHeight = target.getEyeHeight();
                            double distance = target.getLocation().distance(particleLocation);

                            if (distance < eyeHeight * 0.6) { // Head hitbox
                                double damage = 7.9;
                                target.damage(damage);
                                checkDamage(player, target, damage);
                            } else if (distance < eyeHeight * 2.5) { // Chest hitbox
                                double damage = 7.3;
                                target.damage(damage);
                                checkDamage(player, target, damage);
                            } else { // Legs hitbox
                                double damage = 4.5;
                                target.damage(damage);
                                checkDamage(player, target, damage);
                            }

                            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
                        }
                    }

                    particlesSpawned++; // Increment the particle count

                    // Move particles along the line of sight
                    particleLocation.add(particleDirection);
                }
            }.runTaskTimer(MineraveLaserTag.getMlt(), 0L, 1L); // Run the task repeatedly with a 1 tick interval

            int shotCount = getShotCount(playerUUID);

            if (shotCount <= 0) {
                cooldowns.put(playerUUID, new Cooldown(cooldownTime));
                msgPlayer(Bukkit.getPlayer(playerUUID), "&7Gun Reloading....");

                Bukkit.getScheduler().runTaskLater(MineraveLaserTag.getMlt(), () -> {
                    reloadGun(playerUUID);
                    msgPlayer(Bukkit.getPlayer(playerUUID), "&a&lGun Reloaded!");
                }, 20 * cooldownTime);
            }
        }
    }

    private boolean hasBlockCollision(Block block) {
        return !block.getType().isAir();
    }

    private boolean isOnCooldown(UUID playerUUID) {
        return cooldowns.containsKey(playerUUID) && cooldowns.get(playerUUID).isOnCooldown();
    }

    private void reloadGun(UUID playerUUID) {
        cooldowns.remove(playerUUID);
        shotCounts.remove(playerUUID);
        shotCounts.put(playerUUID, 5);
    }

    public int getShotCount(UUID playerUUID) {
        return shotCounts.getOrDefault(playerUUID, maxShots);
    }

    private void decrementShotCount(UUID playerUUID) {
        int currentShots = getShotCount(playerUUID);
        shotCounts.put(playerUUID, currentShots - 1);
    }


    public void checkDamage(Player damager, Player target, double damage) {
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        if (damage >= target.getHealth()) {
            for (Player all : Bukkit.getOnlinePlayers()) {
                msgPlayer(all, "&f" + damager.getName() + " &7killed &f" + target.getName());
            }

            if (game.getMode() == Modes.TEAM_ELIMINATION) {
                MineraveLaserTag.getMlt().getGameManager().checkWin();

                TeamElimination te = MineraveLaserTag.getMlt().getModeManager().getTeamElimination();

                if ("green".equalsIgnoreCase(game.getTeam(damager))) {
                    te.setGreenKills(te.getGreenKills() + 1);
                } else if ("pink".equalsIgnoreCase(game.getTeam(damager))) {
                    te.setPinkKills(te.getPinkKills() + 1);
                }

                target.setHealth(20);
                target.setFoodLevel(20);
                target.setGameMode(GameMode.SPECTATOR);

                titlePlayer(target, "&c&lDIED", "&7Respawning", 0, 20 * 2, 0);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getScheduler().runTask(MineraveLaserTag.getMlt(), () -> {
                            String team = game.getTeam(target);

                            if ("green".equalsIgnoreCase(team)) {
                                target.teleport(game.getGreenLoc());
                            } else if ("pink".equalsIgnoreCase(team)) {
                                target.teleport(game.getPinkLoc());
                            }

                            target.setGameMode(GameMode.SURVIVAL);
                            reloadGun(target.getUniqueId());
                            giveGun(target);
                        });
                    }
                }.runTaskLaterAsynchronously(MineraveLaserTag.getMlt(), 20L * 2);
            }

            if (game.getMode() == Modes.CONTROL_THE_POINT) {
                target.setHealth(20);
                target.setFoodLevel(20);
                target.setGameMode(GameMode.SPECTATOR);

                titlePlayer(target, "&c&lDIED", "&7Respawning", 0, 20 * 2, 0);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getScheduler().runTask(MineraveLaserTag.getMlt(), () -> {
                            String team = game.getTeam(target);

                            if ("green".equalsIgnoreCase(team)) {
                                target.teleport(game.getGreenLoc());
                            } else if ("pink".equalsIgnoreCase(team)) {
                                target.teleport(game.getPinkLoc());
                            }

                            target.setGameMode(GameMode.SURVIVAL);
                            reloadGun(target.getUniqueId());
                            giveGun(target);
                        });
                    }
                }.runTaskLaterAsynchronously(MineraveLaserTag.getMlt(), 20L * 2);
            }
        }
    }

    public void processAction() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(MineraveLaserTag.getMlt(), () -> {
                    if (Bukkit.getOnlinePlayers().size() > 0) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            double healthDouble = player.getHealth();
                            int health = (int) Math.round(healthDouble);
                            int shots = shotCounts.get(player.getUniqueId());
                            Utils.sendActionBar(player, format("&f" + health + " &c❤        &f" + shots + " &b➼"));
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(MineraveLaserTag.getMlt(), 0, 1);
    }

    public static class Cooldown {
        private int remainingTime;

        public Cooldown(int cooldownTime) {
            this.remainingTime = cooldownTime;
        }

        public int getRemainingTime() {
            return remainingTime;
        }

        public boolean isOnCooldown() {
            return remainingTime > 0;
        }
    }
}