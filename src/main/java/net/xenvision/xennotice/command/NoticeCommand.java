package net.xenvision.xennotice.command;

import net.xenvision.xennotice.XenNotice;
import net.xenvision.xennotice.notice.Notice;
import net.xenvision.xennotice.notice.NoticeManager;
import net.xenvision.xennotice.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

public class NoticeCommand implements CommandExecutor {

    private final XenNotice plugin;
    private final NoticeManager noticeManager;

    public NoticeCommand(XenNotice plugin, NoticeManager noticeManager) {
        this.plugin = plugin;
        this.noticeManager = noticeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.colorize(plugin.getLang().get("only-player")));
            return true;
        }

        if (args.length == 0) {
            plugin.getNoticeMenu().open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (!player.hasPermission("xennotice.add")) {
                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("no-permission")));
                return true;
            }

            int maxNotices = plugin.getConfig().getInt("max-notices", 25);
            if (noticeManager.getNoticeCount() >= maxNotices) {
                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("limit-reached")));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ColorUtil.colorize(plugin.getLang().get("help-add")));
                return true;
            }

            String message = String.join(" ", args).substring(4).trim();
            if (message.length() < 2) {
                player.sendMessage(ColorUtil.colorize("&cТекст объявления слишком короткий!"));
                return true;
            }

            // Время жизни объявления
            long days = 1;
            String lifetime = plugin.getConfig().getString("notice-lifetime", "1d");
            if (lifetime.endsWith("d")) {
                try {
                    days = Long.parseLong(lifetime.replace("d", ""));
                } catch (Exception ignored) {}
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiry = now.plusDays(days);

            // Можно добавить выбор иконки через конфиг, пока PAPER
            Notice notice = new Notice(
                    UUID.randomUUID(),
                    player.getName(),
                    player.getUniqueId().toString(),
                    message,
                    now,
                    expiry,
                    "PAPER"
            );

            noticeManager.addNotice(notice);
            player.sendMessage(ColorUtil.colorize(plugin.getLang().get("notice-added")));
            return true;
        }

        // Сообщение-подсказка (help)
        player.sendMessage(ColorUtil.colorize(plugin.getLang().get("help-list")));
        player.sendMessage(ColorUtil.colorize(plugin.getLang().get("help-add")));
        return true;
    }
}