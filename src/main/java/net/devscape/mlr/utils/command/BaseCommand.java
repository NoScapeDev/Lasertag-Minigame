package net.devscape.mlr.utils.command;


import net.devscape.mlr.MineraveLaserTag;

public abstract class BaseCommand {

    public BaseCommand() {
        MineraveLaserTag.getMlt().getCommandFramework().registerCommands(this, null);
    }

    public abstract void executeAs(CommandArguments command);
}
