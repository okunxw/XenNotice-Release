package net.xenvision.xennotice.listener;

import net.xenvision.xennotice.XenNotice;
import net.xenvision.xennotice.gui.NoticeMenuHolder;
import net.xenvision.xennotice.notice.Notice;
import net.xenvision.xennotice.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class MenuListener implements Listener {

    private final XenNotice plugin;

    public MenuListener(XenNotice plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof NoticeMenuHolder)) return;
        event.setCancelled(true);

        int clickedSlot = event.getRawSlot();

        // Кнопки меню — проверка прав
        ConfigurationSection buttonsSection = plugin.getConfig().getConfigurationSection("menu.buttons");
        if (buttonsSection != null) {
            for (String key : buttonsSection.getKeys(false)) {
                int buttonSlot = buttonsSection.getConfigurationSection(key).getInt("slot");
                if (clickedSlot == buttonSlot) {
                    switch (key) {
                        case "next":
                            if (!player.hasPermission("xennotice.menu.next")) {
                                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("menu-next-no-perm")));
                                return;
                            }
                            int currentPage = plugin.getNoticeMenu().getPage(player);
                            int maxPage = plugin.getNoticeMenu().getMaxPage();
                            if (currentPage < maxPage) {
                                plugin.getNoticeMenu().open(player, currentPage + 1);
                            } else {
                                player.sendMessage(ColorUtil.colorize("&7Это последняя страница."));
                            }
                            return;
                        case "prev":
                            if (!player.hasPermission("xennotice.menu.prev")) {
                                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("menu-prev-no-perm")));
                                return;
                            }
                            currentPage = plugin.getNoticeMenu().getPage(player);
                            if (currentPage > 1) {
                                plugin.getNoticeMenu().open(player, currentPage - 1);
                            } else {
                                player.sendMessage(ColorUtil.colorize("&7Это первая страница."));
                            }
                            return;
                        case "add":
                            if (!player.hasPermission("xennotice.add")) {
                                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("menu-add-no-perm")));
                                return;
                            }
                            player.closeInventory();
                            player.performCommand("notice add");
                            return;
                        default:
                            break;
                    }
                }
            }
        }

        // Удаление объявлений — только если есть права
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasLore()) return;

        String idLine = meta.getLore().stream()
                .filter(line -> ColorUtil.stripColor(line).startsWith("ID: "))
                .findFirst()
                .orElse(null);
        if (idLine == null) return;

        String idStr = ColorUtil.stripColor(idLine).replace("ID: ", "");
        UUID noticeId;
        try {
            noticeId = UUID.fromString(idStr);
        } catch (Exception e) {
            return;
        }

        Notice notice = plugin.getNoticeManager().getNotice(noticeId).orElse(null);
        if (notice == null) return;

        boolean isOwn = player.getUniqueId().toString().equals(notice.getAuthorUuid());
        if (isOwn) {
            if (!player.hasPermission("xennotice.remove")) {
                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-delete-denied")));
                return;
            }
        } else {
            if (!player.hasPermission("xennotice.remove.others")) {
                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-delete-other-denied")));
                return;
            }
        }

        // Подтверждение через /notice remove <id>
        player.sendMessage(ColorUtil.colorize("&eИспользуйте &a/notice remove " + noticeId + " &eили кликните по объявлению ещё раз для подтверждения!"));
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof NoticeMenuHolder) {
            event.setCancelled(true);
        }
    }
}