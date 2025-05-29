package net.xenvision.xennotice.gui;

import net.xenvision.xennotice.notice.Notice;
import net.xenvision.xennotice.notice.NoticeManager;
import net.xenvision.xennotice.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.xenvision.xennotice.XenNotice;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NoticeMenu {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final XenNotice plugin;
    private final NoticeManager noticeManager;

    public NoticeMenu(XenNotice plugin, NoticeManager noticeManager) {
        this.plugin = plugin;
        this.noticeManager = noticeManager;
    }

    public void open(Player player) {
        int size = plugin.getConfig().getInt("menu.size", 54);
        String title = ColorUtil.colorize(plugin.getConfig().getString("menu.title", "&a&lОбъявления"));

        Inventory inv = Bukkit.createInventory(new NoticeMenuHolder(), size, title);

        List<Notice> notices = noticeManager.getAllNotices();
        for (int i = 0; i < Math.min(notices.size(), size); i++) {
            Notice notice = notices.get(i);

            Material material = Material.matchMaterial(notice.getIconMaterial());
            if (material == null) material = Material.PAPER;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            String name = ColorUtil.colorize("&e" + notice.getAuthor() + ": &f" + notice.getMessage());
            meta.setDisplayName(name);

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.colorize("&7Добавлено: &f" + DATE_FORMAT.format(notice.getCreated())));
            lore.add(ColorUtil.colorize("&7Истекает: &f" + DATE_FORMAT.format(notice.getExpiry())));
            lore.add(ColorUtil.colorize("&7ID: " + notice.getId().toString())); // <--- Важно!

            meta.setLore(lore);

            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }
}