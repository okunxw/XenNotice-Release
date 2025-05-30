package net.xenvision.xennotice.gui;

import net.xenvision.xennotice.notice.Notice;
import net.xenvision.xennotice.notice.NoticeManager;
import net.xenvision.xennotice.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.xenvision.xennotice.XenNotice;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class NoticeMenu {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final XenNotice plugin;
    private final NoticeManager noticeManager;
    // Храним страницу для каждого игрока
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public NoticeMenu(XenNotice plugin, NoticeManager noticeManager) {
        this.plugin = plugin;
        this.noticeManager = noticeManager;
    }

    public void open(Player player) {
        open(player, getPage(player));
    }

    public void open(Player player, int page) {
        int size = plugin.getConfig().getInt("menu.size", 54);
        String title = ColorUtil.colorize(plugin.getConfig().getString("menu.title", "&a&lОбъявления"));

        String nameTemplate = plugin.getConfig().getString("menu.display-name", "&e%author%: &f%message%");
        List<String> loreTemplate = plugin.getConfig().getStringList("menu.lore");
        List<Integer> slots = plugin.getConfig().getIntegerList("menu.slots");

        Inventory inv = Bukkit.createInventory(new NoticeMenuHolder(), size, title);

        List<Notice> notices = noticeManager.getAllNotices();
        int noticesPerPage = (slots == null || slots.isEmpty()) ? size : slots.size();
        int maxPage = Math.max(1, (int) Math.ceil((double) notices.size() / noticesPerPage));

        // Корректируем номер страницы
        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;
        setPage(player, page);

        int startIdx = (page - 1) * noticesPerPage;
        int endIdx = Math.min(startIdx + noticesPerPage, notices.size());
        List<Notice> pageNotices = notices.subList(startIdx, endIdx);

        // Вывод объявлений по слотам
        if (slots == null || slots.isEmpty()) {
            for (int i = 0; i < pageNotices.size(); i++) {
                Notice notice = pageNotices.get(i);
                inv.setItem(i, buildItem(notice, nameTemplate, loreTemplate));
            }
        } else {
            for (int i = 0; i < pageNotices.size(); i++) {
                int slot = slots.get(i);
                Notice notice = pageNotices.get(i);
                inv.setItem(slot, buildItem(notice, nameTemplate, loreTemplate));
            }
        }

        // Кнопки из config
        if (plugin.getConfig().isConfigurationSection("menu.buttons")) {
            ConfigurationSection buttonsSection = plugin.getConfig().getConfigurationSection("menu.buttons");
            for (String key : buttonsSection.getKeys(false)) {
                ConfigurationSection btn = buttonsSection.getConfigurationSection(key);
                int slot = btn.getInt("slot");
                Material mat = Material.matchMaterial(btn.getString("material", "BARRIER"));
                if (mat == null) mat = Material.BARRIER;
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ColorUtil.colorize(btn.getString("name", "&cКнопка")));
                List<String> lore = new ArrayList<>();
                if (btn.isList("lore")) {
                    for (String l : btn.getStringList("lore")) lore.add(ColorUtil.colorize(l));
                }
                // Добавляем отображение страницы (например, на кнопку next)
                if (key.equals("next") || key.equals("prev")) {
                    lore.add(ColorUtil.colorize("&7Страница: &f" + page + " / " + maxPage));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    private ItemStack buildItem(Notice notice, String nameTemplate, List<String> loreTemplate) {
        Material material = Material.matchMaterial(notice.getIconMaterial());
        if (material == null) material = Material.PAPER;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String displayName = nameTemplate
                .replace("%author%", notice.getAuthor())
                .replace("%message%", notice.getMessage());

        List<String> lore = new ArrayList<>();
        for (String line : loreTemplate) {
            lore.add(line
                    .replace("%created%", DATE_FORMAT.format(notice.getCreated()))
                    .replace("%expiry%", DATE_FORMAT.format(notice.getExpiry()))
                    .replace("%id%", notice.getId().toString())
                    .replace("%author%", notice.getAuthor())
                    .replace("%message%", notice.getMessage())
            );
        }
        meta.setDisplayName(ColorUtil.colorize(displayName));
        List<String> coloredLore = new ArrayList<>();
        for (String l : lore) coloredLore.add(ColorUtil.colorize(l));
        meta.setLore(coloredLore);

        item.setItemMeta(meta);
        return item;
    }

    // --- Пагинация ---

    public int getPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 1);
    }

    public void setPage(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
    }

    public void resetPage(Player player) {
        playerPages.remove(player.getUniqueId());
    }

    public int getMaxPage() {
        List<Integer> slots = plugin.getConfig().getIntegerList("menu.slots");
        List<Notice> notices = noticeManager.getAllNotices();
        int perPage = (slots == null || slots.isEmpty()) ? plugin.getConfig().getInt("menu.size", 54) : slots.size();
        return Math.max(1, (int) Math.ceil((double) notices.size() / perPage));
    }
}