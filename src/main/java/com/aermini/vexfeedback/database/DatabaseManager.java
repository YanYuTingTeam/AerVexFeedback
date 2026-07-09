package com.aermini.vexfeedback.database;

import com.aermini.vexfeedback.AerVexFeedback;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final AerVexFeedback plugin;
    private String url;
    private String username;
    private String password;
    private String posturl;

    public DatabaseManager(AerVexFeedback plugin) {
        this.plugin = plugin;
        FileConfiguration cfg = plugin.getConfig();
        String database = cfg.getString("mysql.database", "aervexfeedback");
        this.username = cfg.getString("mysql.username", "root");
        this.password = cfg.getString("mysql.password", "");
        this.url = "jdbc:mysql://localhost:3306/" + database + "?useSSL=false&autoReconnect=true&characterEncoding=utf8";
        this.posturl = cfg.getString("posturl", "none");

        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                if (conn == null) {
                    plugin.getLogger().severe("Failed to connect to MySQL!");
                    return;
                }
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS aervexfeedback (" +
                                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                    "time BIGINT NOT NULL, " +
                                    "game INT NOT NULL, " +
                                    "content TEXT NOT NULL, " +
                                    "player VARCHAR(64) DEFAULT ''" +
                                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                    );
                }
                plugin.getLogger().info("MySQL table initialized successfully.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to initialize MySQL table: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e2) {
                plugin.getLogger().severe("MySQL driver not found!");
                return null;
            }
        }
        return DriverManager.getConnection(url, username, password);
    }

    public void saveFeedback(long time, int game, String content, String playerName, Runnable callback) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                if (conn == null) {
                    plugin.getLogger().severe("Failed to connect to MySQL for saving feedback!");
                    return;
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO aervexfeedback (time, game, content, player) VALUES (?, ?, ?, ?)")) {
                    ps.setLong(1, time);
                    ps.setInt(2, game);
                    ps.setString(3, content);
                    ps.setString(4, playerName);
                    ps.executeUpdate();
                }
                doHttpPost(time, game, content, playerName);
                if (callback != null) {
                    plugin.getServer().getScheduler().runTask(plugin, callback);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save feedback: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void doHttpPost(long time, int game, String content, String playerName) {
        if (posturl == null || posturl.equalsIgnoreCase("none") || posturl.isEmpty()) return;
        try {
            String json = "{\"time\":" + time + ",\"game\":" + game
                    + ",\"content\":\"" + content.replace("\\", "\\\\").replace("\"", "\\\"")
                    + "\",\"player\":\"" + playerName.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            HttpURLConnection conn = (HttpURLConnection) new URL(posturl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to POST feedback: " + e.getMessage());
        }
    }

    public void close() {
    }
}
