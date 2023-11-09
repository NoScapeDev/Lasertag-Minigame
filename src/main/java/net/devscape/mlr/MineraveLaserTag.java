package net.devscape.mlr;

import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import lombok.Getter;
import net.devscape.mlr.listeners.GunListener;
import net.devscape.mlr.managers.GameManager;
import net.devscape.mlr.managers.GameTasks;
import net.devscape.mlr.modes.ModeManager;
import net.devscape.mlr.utils.BungeeUtils;
import net.devscape.mlr.utils.ClassRegistrationUtils;
import net.devscape.mlr.utils.Utils;
import net.devscape.mlr.utils.command.CommandFramework;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public final class MineraveLaserTag extends JavaPlugin {

    private static MineraveLaserTag mlt;
    private GameManager gameManager;
    private ModeManager modeManager;
    private GunListener gunListeners;
    private BungeeUtils bungeeUtils;

    private GlowingEntities glowingEntities;
    private GlowingBlocks glowingBlocks;

    private GameTasks gameTasks;

    private final CommandFramework commandFramework = new CommandFramework(this);

    public static MineraveLaserTag getMlt() {
        return mlt;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        mlt = this;
        saveDefaultConfig();

        glowingEntities = new GlowingEntities(this);
        glowingBlocks = new GlowingBlocks(this);

        loadManagers();
        loadListeners();
        loadCommands();

        gunListeners = new GunListener();
        gameTasks = new GameTasks();
        bungeeUtils = new BungeeUtils(this);

        // Bukkit.getMessenger().registerOutgoingPluginChannel(this, "laserTagChannel");
        // Bukkit.getMessenger().registerIncomingPluginChannel(this, "laserTagChannel", new YourChannelListener());
    }

    private void loadManagers() {
        gameManager = new GameManager();
        modeManager = new ModeManager();
    }

    private void loadCommands() {
        ClassRegistrationUtils.loadCommands("net.devscape.mlr.commands");
    }

    private void loadListeners() {
        ClassRegistrationUtils.loadListeners("net.devscape.mlr.listeners");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    public void reload() {
        super.reloadConfig();
    }
}