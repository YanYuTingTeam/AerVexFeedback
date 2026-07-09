package com.aermini.vexfeedback.command;

import com.aermini.vexfeedback.AerVexFeedback;
import com.aermini.vexfeedback.config.FeedbackConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class FeedbackCommand implements CommandExecutor {

    private final AerVexFeedback plugin;

    public FeedbackCommand(AerVexFeedback plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "用法: /aervexfeedback reload | open [player]");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("aervexfeedback.reload")) return true;
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "AerVexFeedback 已重载");
            return true;
        }
        if (args[0].equalsIgnoreCase("open")) {
            Player target;
            if (args.length >= 2) {
                if (!sender.hasPermission("aervexfeedback.open")) return true;
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "玩家 " + args[1] + " 不在线");
                    return true;
                }
            } else {
                if (!(sender instanceof Player)) return true;
                target = (Player) sender;
            }
            openInventoryGui(target);
            return true;
        }
        return true;
    }

    private void openInventoryGui(Player player) {
        FeedbackConfig cfg = plugin.getFeedbackConfig();
        int slot = cfg.getInvSlot();
        String title = cfg.getInvTitle();
        Inventory inv = Bukkit.createInventory(null, slot, title);
        for (Map.Entry<Integer, FeedbackConfig.InvItem> entry : cfg.getInvItems().entrySet()) {
            int slotIndex = entry.getKey();
            if (slotIndex >= slot) continue;
            inv.setItem(slotIndex, entry.getValue().toItemStack());
        }
        player.openInventory(inv);
    }
}
