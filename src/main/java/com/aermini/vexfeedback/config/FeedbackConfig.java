package com.aermini.vexfeedback.config;

import com.aermini.vexfeedback.AerVexFeedback;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeedbackConfig {

    private String bgUrl;
    private int bgX, bgY, bgW, bgH;
    private String btnUrl;
    private int btnX, btnY, btnW, btnH;
    private String closeUrl;
    private int closeX, closeY, closeW, closeH;
    private int input1X, input1Y, input1W, input1H, input1Max;
    private int input2X, input2Y, input2W, input2H, input2Max;
    private int invSlot;
    private String invTitle;
    private Map<Integer, InvItem> invItems = new LinkedHashMap<>();
    private String posturl;
    private String input1tip;
    private String input2tip;
    private String successMsg;

    public FeedbackConfig(AerVexFeedback plugin) {
        plugin.reloadConfig();
        org.bukkit.configuration.Configuration cfg = plugin.getConfig();

        bgUrl = cfg.getString("bg", "");
        int[] bgs = parseIntArray(cfg.getString("bgs", "0,0,256,256"));
        bgX = bgs[0]; bgY = bgs[1]; bgW = bgs[2]; bgH = bgs[3];

        btnUrl = cfg.getString("btn", "");
        int[] btns = parseIntArray(cfg.getString("btns", "0,0,64,32"));
        btnX = btns[0]; btnY = btns[1]; btnW = btns[2]; btnH = btns[3];

        closeUrl = cfg.getString("close", "");
        int[] closes = parseIntArray(cfg.getString("closes", "0,0,32,32"));
        closeX = closes[0]; closeY = closes[1]; closeW = closes[2]; closeH = closes[3];

        int[] i1 = parseIntArray(cfg.getString("input1", "0,0,200,20,100"));
        input1X = i1[0]; input1Y = i1[1]; input1W = i1[2]; input1H = i1[3]; input1Max = i1[4];

        int[] i2 = parseIntArray(cfg.getString("input2", "0,0,200,20,20"));
        input2X = i2[0]; input2Y = i2[1]; input2W = i2[2]; input2H = i2[3]; input2Max = i2[4];

        invSlot = cfg.getInt("inv.slot", 54);
        invTitle = colorize(cfg.getString("inv.title", "请选择要反馈的玩法"));
        ConfigurationSection itemsSection = cfg.getConfigurationSection("inv.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSec = itemsSection.getConfigurationSection(key);
                if (itemSec == null) continue;
                int slot = Integer.parseInt(key);
                if (slot >= invSlot) continue;

                String materialName = itemSec.getString("material", "STONE");
                int data = itemSec.getInt("data", 0);
                String title = colorize(itemSec.getString("title", ""));
                List<String> lore = new ArrayList<>();
                if (itemSec.contains("lore")) {
                    for (String line : itemSec.getStringList("lore")) {
                        lore.add(colorize(line));
                    }
                }
                invItems.put(slot, new InvItem(materialName, data, title, lore));
            }
        }

        posturl = cfg.getString("posturl", "none");
        input1tip = colorize(cfg.getString("input1tip", "请输入你的意见或建议~ (在聊天中发送)"));
        input2tip = colorize(cfg.getString("input2tip", "请输入相关玩家名或自己的玩家名~ (在聊天中发送)"));
        successMsg = colorize(cfg.getString("success", "&a反馈成功！"));
    }

    private int[] parseIntArray(String s) {
        String[] parts = s.split(",");
        int[] result = new int[5];
        for (int i = 0; i < parts.length && i < 5; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String getBgUrl() { return bgUrl; }
    public int getBgX() { return bgX; }
    public int getBgY() { return bgY; }
    public int getBgW() { return bgW; }
    public int getBgH() { return bgH; }

    public String getBtnUrl() { return btnUrl; }
    public int getBtnX() { return btnX; }
    public int getBtnY() { return btnY; }
    public int getBtnW() { return btnW; }
    public int getBtnH() { return btnH; }

    public String getCloseUrl() { return closeUrl; }
    public int getCloseX() { return closeX; }
    public int getCloseY() { return closeY; }
    public int getCloseW() { return closeW; }
    public int getCloseH() { return closeH; }

    public int getInput1X() { return input1X; }
    public int getInput1Y() { return input1Y; }
    public int getInput1W() { return input1W; }
    public int getInput1H() { return input1H; }
    public int getInput1Max() { return input1Max; }

    public int getInput2X() { return input2X; }
    public int getInput2Y() { return input2Y; }
    public int getInput2W() { return input2W; }
    public int getInput2H() { return input2H; }
    public int getInput2Max() { return input2Max; }

    public int getInvSlot() { return invSlot; }
    public String getInvTitle() { return invTitle; }
    public Map<Integer, InvItem> getInvItems() { return invItems; }

    public String getPosturl() { return posturl; }
    public String getInput1tip() { return input1tip; }
    public String getInput2tip() { return input2tip; }
    public String getSuccessMsg() { return successMsg; }

    public static class InvItem {
        private final String material;
        private final int data;
        private final String title;
        private final List<String> lore;

        public InvItem(String material, int data, String title, List<String> lore) {
            this.material = material;
            this.data = data;
            this.title = title;
            this.lore = lore;
        }

        public ItemStack toItemStack() {
            Material mat = Material.matchMaterial(material);
            if (mat == null) mat = Material.STONE;
            ItemStack item = new ItemStack(mat, 1, (short) data);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (title != null && !title.isEmpty()) meta.setDisplayName(title);
                if (lore != null && !lore.isEmpty()) meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        }

        public String getMaterial() { return material; }
        public int getData() { return data; }
        public String getTitle() { return title; }
        public List<String> getLore() { return lore; }
    }
}
