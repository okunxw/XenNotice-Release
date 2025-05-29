package net.xenvision.xennotice.listener;

import net.xenvision.xennotice.XenNotice;
import net.xenvision.xennotice.gui.NoticeMenuHolder;
import net.xenvision.xennotice.notice.Notice;
import net.xenvision.xennotice.util.ColorUtil;
import org.bukkit.Bukkit;
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

        // Проверка: клик по слоту с объявлением
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

        // Только автор может удалять
        if (!player.getUniqueId().toString().equals(notice.getAuthorUuid())) {
            player.sendMessage(ColorUtil.colorize(plugin.getLang().get("no-permission")));
            return;
        }
        // Удаляем и обновляем меню
        plugin.getNoticeManager().removeNotice(noticeId);
        player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-removed")));
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getNoticeMenu().open(player), 2L);
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof NoticeMenuHolder) {
            event.setCancelled(true);
        }
    }
}