package net.devscape.mlr.listeners;

import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.handlers.GameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.PlayerInventory;

import static net.devscape.mlr.utils.Utils.format;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        if (game.getGameState() ==  GameState.IN_PROGRESS) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_FULL);
            e.setKickMessage(format("This game is in progress."));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        game.include(player);
        e.setJoinMessage("");

        if (game.getGameState() !=  GameState.IN_PROGRESS) {
            TextComponent component = Component.text(format("&f侮 &7" + player.getName() + " &8(&f" + Bukkit.getOnlinePlayers().size() + "&a/8&8)"));
            for (Player all : Bukkit.getOnlinePlayers()) {
                all.sendMessage(component);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        game.exclude(player);
        e.setQuitMessage("");

        TextComponent component = Component.text(format("&f侯 &7" + player.getName()));
        for (Player all : Bukkit.getOnlinePlayers()) {
            all.sendMessage(component);
        }

        if (game.getGameState() ==  GameState.IN_PROGRESS) {
            MineraveLaserTag.getMlt().getGameManager().checkWin();
        }
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();
        Location from = e.getFrom().getBlock().getLocation();
        Location to = e.getTo().getBlock().getLocation();

        if (game.getGameState() != GameState.IN_PROGRESS) {
            if (!from.equals(to)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevel(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBuild(BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getEntity();

            arrow.setTicksLived(arrow.getTicksLived() + 6000);
        }
    }

    @EventHandler
    public void onInventory(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent e) {
        if (e.getInitiator() instanceof PlayerInventory) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (player.getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }
}