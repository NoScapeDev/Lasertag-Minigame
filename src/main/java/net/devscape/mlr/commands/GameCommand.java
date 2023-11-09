package net.devscape.mlr.commands;

import net.devscape.mlr.MineraveLaserTag;
import net.devscape.mlr.utils.Utils;
import net.devscape.mlr.utils.command.BaseCommand;
import net.devscape.mlr.utils.command.Command;
import net.devscape.mlr.utils.command.CommandArguments;
import org.bukkit.entity.Player;

public class GameCommand extends BaseCommand {

    private MineraveLaserTag main = MineraveLaserTag.getMlt();

    @Command(name = "game", permission = "game.admin", usage = "&cUsage: /game help")
    @Override
    public void executeAs(CommandArguments command) {
        Player player = command.getPlayer();

        String[] args = command.getArgs();

        if (args.length == 0) {
            player.sendMessage(Utils.format(command.getCommand().getUsage()));
        } else {
            switch (args[0]) {
                case "help":
                    Utils.msgPlayer(player, "&c&l[GAME] &7Admin Commands", "&c/game setlocation <mapname> <green/pink/control-1/control-2>", "&c/game setmode <mapname> <te/ctp>", "&c/game reload");

                    break;
                case "setlocation":
                    String mapName = args[1];
                    String option = args[2];

                    switch (option) {
                        case "green":
                            Utils.setLocation(mapName, "green", player);
                            Utils.msgPlayer(player, "&c&l[GAME] &7Location &f&lGREEN &7saved.");
                            break;
                        case "pink":
                            Utils.setLocation(mapName, "pink", player);
                            Utils.msgPlayer(player, "&c&l[GAME] &7Location &f&lPINK &7saved.");
                            break;
                        case "control-1":
                            Utils.setLocation(mapName, "control-pos1", player);
                            Utils.msgPlayer(player, "&c&l[GAME] &7Location &f&lCONTROL-POS-1 &7saved.");
                            break;
                        case "control-2":
                            Utils.setLocation(mapName, "control-pos2", player);
                            Utils.msgPlayer(player, "&c&l[GAME] &7Location &f&lCONTROL-POS-2 &7saved.");
                            break;
                    }
                case "setmode":
                    String map = args[1];
                    String mode = args[2];

                    switch (mode) {
                        case "te":
                            Utils.setMode(map, "TEAM_ELIMINATION");
                            Utils.msgPlayer(player, "&c&l[GAME] &7Mode &f&lTEAM_ELIMINATION &7saved.");
                            break;
                        case "ctp":
                            Utils.setMode(map, "CONTROL_THE_POINT");
                            Utils.msgPlayer(player, "&c&l[GAME] &7Mode &f&lCONTROL_THE_POINT &7saved.");
                            break;
                    }

                    break;
                case "reload":
                    this.main.reload();

                    Utils.msgPlayer(player, "&c&l[GAME] &7Game reloaded.");

                    break;
            }
        }
    }
}
