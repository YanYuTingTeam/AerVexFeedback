package com.aermini.vexfeedback;

import com.aermini.vexfeedback.command.FeedbackCommand;
import com.aermini.vexfeedback.config.FeedbackConfig;
import com.aermini.vexfeedback.database.DatabaseManager;
import com.aermini.vexfeedback.listener.FeedbackListener;
import org.bukkit.plugin.java.JavaPlugin;

public class AerVexFeedback extends JavaPlugin {

    private static AerVexFeedback instance;
    private FeedbackConfig feedbackConfig;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        feedbackConfig = new FeedbackConfig(this);
        databaseManager = new DatabaseManager(this);

        getCommand("aervexfeedback").setExecutor(new FeedbackCommand(this));
        getServer().getPluginManager().registerEvents(new FeedbackListener(this), this);

        getLogger().info("AerVexFeedback has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("AerVexFeedback has been disabled!");
    }

    public static AerVexFeedback getInstance() {
        return instance;
    }

    public FeedbackConfig getFeedbackConfig() {
        return feedbackConfig;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void reload() {
        reloadConfig();
        feedbackConfig = new FeedbackConfig(this);
        try {
            databaseManager.close();
        } catch (Exception ignored) {}
        databaseManager = new DatabaseManager(this);
    }
}
