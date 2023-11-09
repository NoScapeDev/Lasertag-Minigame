package net.devscape.mlr.listeners;

import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.handlers.Game;
import net.devscape.mlr.handlers.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static net.devscape.mlr.utils.Utils.format;
import static net.devscape.mlr.utils.Utils.msgPlayer;

public class ItemListener implements Listener {

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        Game game = MineraveLaserTag.getMlt().getGameManager().getGame();

        if (item != null && item.getItemMeta() != null) {
            String displayName = item.getItemMeta().getDisplayName();

            if (displayName.equalsIgnoreCase(format(MineraveLaserTag.getMlt().getConfig().getString("items.back-to-hub.displayname")))) {
                if (game.getGameState() == GameState.IN_PROGRESS) {
                    msgPlayer(player, "&f侵 &7This can't be used whilst in-game.");
                    return;
                }

                MineraveLaserTag.getMlt().getBungeeUtils().sendPlayerToServer(player, "hub");
            }
        }
    }
}