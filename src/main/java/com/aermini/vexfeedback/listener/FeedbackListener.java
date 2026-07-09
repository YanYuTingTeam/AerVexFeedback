package com.aermini.vexfeedback.listener;

import com.aermini.vexfeedback.AerVexFeedback;
import com.aermini.vexfeedback.config.FeedbackConfig;
import lk.vexview.api.VexViewAPI;
import lk.vexview.gui.OpenedVexGui;
import lk.vexview.gui.VexGui;
import lk.vexview.gui.components.VexButton;
import lk.vexview.gui.components.VexTextField;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeedbackListener implements Listener {

    private final AerVexFeedback plugin;
    private final Map<UUID, Integer> selectedGame = new HashMap<>();

    private final Map<UUID, Integer> waitingChatStep = new HashMap<>();
    private final Map<UUID, Integer> chatGameSlot = new HashMap<>();
    private final Map<UUID, String> chatContent = new HashMap<>();

    public FeedbackListener(AerVexFeedback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        FeedbackConfig cfg = plugin.getFeedbackConfig();
        String title = cfg.getInvTitle();

        if (event.getView().getTitle().equals(title)) {
            event.setCancelled(true);

            int slot = event.getRawSlot();
            if (slot < 0 || slot >= cfg.getInvSlot()) return;
            if (!cfg.getInvItems().containsKey(slot)) return;

            selectedGame.put(player.getUniqueId(), slot);

            player.closeInventory();
            openVexGui(player, slot);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uid = player.getUniqueId();

        if (!waitingChatStep.containsKey(uid)) return;
        event.setCancelled(true);

        int step = waitingChatStep.get(uid);
        String msg = event.getMessage();

        if (step == 1) {
            chatContent.put(uid, msg);
            waitingChatStep.put(uid, 2);
            player.sendMessage(plugin.getFeedbackConfig().getInput2tip());
        } else if (step == 2) {
            int gameSlot = chatGameSlot.getOrDefault(uid, 0);
            String content = chatContent.getOrDefault(uid, "");
            long time = System.currentTimeMillis();

            waitingChatStep.remove(uid);
            chatGameSlot.remove(uid);
            chatContent.remove(uid);

            plugin.getDatabaseManager().saveFeedback(time, gameSlot, content, msg, () -> {
                player.sendMessage(plugin.getFeedbackConfig().getSuccessMsg());
            });
        }
    }

    private void openVexGui(Player player, int gameSlot) {
        FeedbackConfig cfg = plugin.getFeedbackConfig();

        try {
            VexGui gui = new VexGui(
                    cfg.getBgUrl(),
                    cfg.getBgX(),
                    cfg.getBgY(),
                    cfg.getBgW(),
                    cfg.getBgH()
            );

            VexTextField input1 = new VexTextField(
                    cfg.getInput1X(),
                    cfg.getInput1Y(),
                    cfg.getInput1W(),
                    cfg.getInput1H(),
                    cfg.getInput1Max(),
                    1
            );

            VexTextField input2 = new VexTextField(
                    cfg.getInput2X(),
                    cfg.getInput2Y(),
                    cfg.getInput2W(),
                    cfg.getInput2H(),
                    cfg.getInput2Max(),
                    2,
                    ""
            );

            VexButton submitBtn = new VexButton(
                    "submit",
                    "",
                    cfg.getBtnUrl(),
                    cfg.getBtnUrl(),
                    cfg.getBtnX(),
                    cfg.getBtnY(),
                    cfg.getBtnW(),
                    cfg.getBtnH(),
                    p -> handleSubmit(p, gameSlot)
            );

            VexButton closeBtn = new VexButton(
                    "close",
                    "",
                    cfg.getCloseUrl(),
                    cfg.getCloseUrl(),
                    cfg.getCloseX(),
                    cfg.getCloseY(),
                    cfg.getCloseW(),
                    cfg.getCloseH(),
                    p -> p.closeInventory()
            );

            gui.addComponent(input1);
            gui.addComponent(input2);
            gui.addComponent(submitBtn);
            gui.addComponent(closeBtn);

            VexViewAPI.openGui(player, gui);
        } catch (Exception e) {
            startChatFallback(player, gameSlot);
        }
    }

    private void startChatFallback(Player player, int gameSlot) {
        UUID uid = player.getUniqueId();
        chatGameSlot.put(uid, gameSlot);
        waitingChatStep.put(uid, 1);
        player.sendMessage(plugin.getFeedbackConfig().getInput1tip());
    }

    private void handleSubmit(Player player, int gameSlot) {
        OpenedVexGui openedGui = VexViewAPI.getPlayerCurrentGui(player);
        if (openedGui == null) return;

        VexTextField tf1 = openedGui.getVexGui().getTextField(1);
        VexTextField tf2 = openedGui.getVexGui().getTextField(2);

        String content = (tf1 != null) ? tf1.getTypedText() : "";
        String playerName = (tf2 != null) ? tf2.getTypedText() : "";

        if (content == null || content.trim().isEmpty()) {
            player.sendMessage(ChatColor.RED + "请输入反馈内容!");
            return;
        }

        long time = System.currentTimeMillis();

        plugin.getDatabaseManager().saveFeedback(time, gameSlot, content, playerName, () -> {
            player.closeInventory();
            player.sendMessage(plugin.getFeedbackConfig().getSuccessMsg());
        });
    }
}
